package com.demarpsoftware.kidswordplus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.demarpsoftware.kidswordplus.content.*
import com.demarpsoftware.kidswordplus.games.*
import java.time.LocalDate
import android.os.SystemClock
import com.demarpsoftware.kidswordplus.family.*
import com.demarpsoftware.kidswordplus.chess.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

data class AppState(
    val helpEnabled: Boolean = true, val speakingEnabled: Boolean = false,
    val soundEnabled:Boolean=true, val animationsEnabled:Boolean=true,
    val customUndo:List<ChessCustomPosition> = emptyList(),
    val dailyGoal:Int=60, val parentPhotoVersion:Long=0,
    val name: String = "", val ui: String = "tr", val language: String = "tr", val ready: Boolean = false,
    val pack: LearningPack? = null, val entries: List<PackEntry> = emptyList(), val installed: Set<String> = emptySet(),
    val installedDetails:Map<String,com.demarpsoftware.kidswordplus.content.InstalledPackage> = emptyMap(),
    val localCatalog: Boolean = false, val busy: Boolean = false, val operationLanguage: String? = null,
    val progress: Float = 0f, val message: String? = null, val completed: Set<String> = emptySet(),
    val lastLesson: String? = null, val lastConcept: String? = null,
    val gameProgress: GameProgress = GameProgress(), val game: GameSession? = null, val today: LocalDate = LocalDate.now(),
    val companion: String = "piko-default", val earned: Set<String> = Companions.starters,
    val perfectRound: Boolean = false, val finishedLevel: Boolean = false, val parentGate: ParentGate = ParentGate(),
    val chess: ChessGame? = null, val chessStats: ChessStatistics = ChessStatistics(), val chessHint: ChessMove? = null,
    val chessCustom: ChessCustomPosition = ChessCustomPosition(), val chessCustomSaveError: Boolean = false,
    val chessOpeningProgress: ChessOpeningProgress = ChessOpeningProgress(), val chessOpeningSaveError: Boolean = false,
    val chessPuzzleCompleted: Set<String> = emptySet(),
    val chessSetupCount: Int = 0, val chessSafetyCompleted: Set<String> = emptySet(),
    val chessLessonCompleted: Set<String> = emptySet(), val chessSaveError: Boolean = false)

class AppModel(application: Application) : AndroidViewModel(application) {
    var welcomeVisible by androidx.compose.runtime.mutableStateOf(true)
        private set
    fun completeWelcome() {welcomeVisible=false}
    private val prefs = application.getSharedPreferences("profile", 0)
    private val store = PackageStore(File(application.filesDir, "content"), BuildConfig.DEBUG,
        { path ->
            val internal = File(application.filesDir, path)
            val external = File(application.getExternalFilesDir(null), path)
            if (BuildConfig.DEBUG && internal.isFile) internal.inputStream()
            else if (BuildConfig.DEBUG && external.isFile) external.inputStream()
            else application.assets.open(path)
        })
    private val cache = File(application.filesDir, "catalog-cache.json")
    private var progressReadable = true
    private val initialGameProgress = runCatching {
        prefs.getString("games.v1", null)?.let(GameProgress::decode) ?: GameProgress()
    }.getOrElse { progressReadable = false; GameProgress() }
    private val initialCustom = runCatching { prefs.getString("chess.custom.v1", null)?.let(ChessCustomPosition::decode) ?: ChessCustomPosition.standard() }
    private val initialOpenings = runCatching { prefs.getString("chess.openings.v1", null)?.let(ChessOpeningProgress::decode) ?: ChessOpeningProgress() }
    private val mutable = MutableStateFlow(AppState(
        helpEnabled = prefs.getBoolean("help.enabled", true),
        speakingEnabled = prefs.getBoolean("speech.practice.enabled", false),
        soundEnabled=prefs.getBoolean("sound.enabled",true),animationsEnabled=prefs.getBoolean("animations.enabled",true),
        dailyGoal=prefs.getInt("daily.goal",60).takeIf {it in listOf(20,40,60,80,100)} ?: 60,
        name = prefs.getString("name", "") ?: "", ui = prefs.getString("ui", "tr") ?: "tr",
        language = prefs.getString("language", "tr") ?: "tr", ready = prefs.getBoolean("ready", false),
        chessCustom = initialCustom.getOrDefault(ChessCustomPosition()), chessCustomSaveError = initialCustom.isFailure,
        chessOpeningProgress = initialOpenings.getOrDefault(ChessOpeningProgress()), chessOpeningSaveError = initialOpenings.isFailure,
        chessPuzzleCompleted = prefs.getStringSet("chess.puzzles.v1", emptySet())!!.toSet().intersect(ChessPuzzles.ids),
        chessSetupCount = prefs.getInt("chess.setup.v1", 0).takeIf { it in 0..32 } ?: 0,
        chessSafetyCompleted = prefs.getStringSet("chess.safety.v1", emptySet())!!.toSet().intersect(ChessSafety.ids),
        chessLessonCompleted = prefs.getStringSet("chess.lessons.v1", emptySet())!!.toSet().intersect(ChessLessons.ids),
        completed = prefs.getStringSet("completed", emptySet())!!.toSet(), gameProgress = initialGameProgress,
        companion = prefs.getString("companion", "piko-default") ?: "piko-default",
        earned = prefs.getStringSet("companions.earned", emptySet())!!.toSet() + Companions.starters,
        perfectRound = prefs.getBoolean("companions.perfect", false), finishedLevel = prefs.getBoolean("companions.level", false)))
    val state = mutable.asStateFlow()
    private var operation: Job? = null
    private var gameGeneration = 0
    private val catalogUrl = "https://firebasestorage.googleapis.com/v0/b/kids-word-plus-v2.firebasestorage.app/o/language-packs%2Fv2%2F" +
        (if (BuildConfig.DEBUG) "preview%2F" else "") + "catalog.json?alt=media"

    init { runOperation {
        withContext(Dispatchers.IO) { store.clearStaging() }
        try { loadChess(); reload(); refreshDayInternal() } finally { loadCatalog(remote = !BuildConfig.DEBUG) }
    } }
    fun clearMessage() = mutable.update { it.copy(message = null) }
    private fun runOperation(indicateBusy: Boolean = true, block: suspend () -> Unit) {
        if (operation?.isActive == true) return
        operation = viewModelScope.launch {
            if(indicateBusy) mutable.update { it.copy(busy = true, message = null, progress = 0f) }
            try { block(); syncCompanions() }
            catch (_: CancellationException) {
                withContext(NonCancellable) { runCatching { reload() } }
                mutable.update { it.copy(message = localized("İşlem iptal edildi. Yeniden deneyebilirsin.", "Cancelled. You can retry.")) }
            }
            catch (e: Exception) {
                android.util.Log.w("Content", "Content operation failed", e)
                AppTelemetry.operationFailed(e)
                mutable.update { it.copy(message = localized("İşlem tamamlanamadı. Bağlantını ve boş depolama alanını kontrol edip yeniden deneyebilirsin.", "Could not complete. Check your connection and free storage, then try again.")) }
            }
            finally { if(indicateBusy) mutable.update { it.copy(busy = false, operationLanguage = null) } }
        }
    }
    private fun localized(tr: String, en: String) = InterfaceStrings.text(mutable.value.ui,tr,en)
    private suspend fun reload() {
        val language = mutable.value.language
        val result = withContext(Dispatchers.IO) { Triple(store.load(language),store.installedLanguages().toSet(),store.installedDetails()) }
        mutable.update { it.copy(pack = result.first, installed = result.second, installedDetails=result.third, game = it.game?.takeIf { game -> game.language == language },
            lastLesson = result.first?.lessons?.find { lesson -> lesson.words.any { word -> word.id == prefs.getString("concept_$language", null) } }?.id ?: prefs.getString("lesson_$language", null), lastConcept = prefs.getString("concept_$language", null)) }
    }
    private suspend fun loadCatalog(remote: Boolean) {
        val result = withContext(Dispatchers.IO) {
            if (!remote) {
                require(BuildConfig.DEBUG)
                Catalog.parse(getApplication<Application>().assets.open("packages/catalog.json").bufferedReader().use { it.readText() }, true, true) to true
            } else {
                try {
                    val context = currentCoroutineContext()
                    val text = PackageDownloader().catalog(catalogUrl) { context.ensureActive() }
                    val entries = Catalog.parse(text, false, BuildConfig.DEBUG)
                    PackageSafety.atomicWrite(cache, text)
                    entries to false
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    if (!cache.isFile) throw e
                    mutable.update { it.copy(message = localized("Bağlantı kurulamadı; son katalog kullanılıyor.", "Connection failed; using cached catalog.")) }
                    Catalog.parse(cache.readText(), false, BuildConfig.DEBUG) to false
                }
            }
        }
        mutable.update { it.copy(entries = result.first, localCatalog = result.second) }
    }
    fun refreshCatalog(remote: Boolean) = runOperation { requireParentIfReady(); loadCatalog(remote) }
    fun cancel() { operation?.cancel() }
    fun install(language: String) = runOperation {
        requireParentIfReady()
        val entries = mutable.value.entries
        withContext(Dispatchers.IO) {
            val context = currentCoroutineContext()
            var lastUpdate = 0L
            store.install(language, entries, { context.ensureActive() }) { code, fraction ->
                val now = System.nanoTime()
                if (now - lastUpdate > 100_000_000L || fraction >= 1f) {
                    mutable.update { it.copy(operationLanguage = code, progress = fraction) }; lastUpdate = now
                }
            }
        }
        reload()
        AppTelemetry.packageInstalled(language)
    }
    fun remove(language: String) = runOperation {
        requireParentIfReady()
        withContext(Dispatchers.IO) { store.remove(language) }
        reload()
    }
    fun setSpeakingEnabled(enabled: Boolean)=runOperation {
        requireParentIfReady()
        withContext(Dispatchers.IO){check(prefs.edit().putBoolean("speech.practice.enabled",enabled).commit())}
        mutable.update {it.copy(speakingEnabled=enabled)}
    }
    fun completeSpeakingRound(language: String, id: String, matched: Set<String>)=runOperation {
        val current=mutable.value
        check(language==current.language)
        val active=current.pack?.words?.map {it.id}?.toSet() ?: return@runOperation
        require(matched.all {it in active})
        val date=LocalDate.now()
        val progress=current.gameProgress.speakingRound(language,id,matched,date)
        saveGameProgress(progress)
        mutable.update {it.copy(gameProgress=progress,today=date)}
    }
    fun completeRecallRound(language:String,round:com.demarpsoftware.kidswordplus.games.RecallRound,after:()->Unit={})=runOperation {
        val current=mutable.value
        if(language!=current.language || !round.finished)return@runOperation
        val active=current.pack?.words?.map{it.id}?.toSet() ?: return@runOperation
        require(round.reviewed.all{it in active})
        val date=LocalDate.now()
        val progress=current.gameProgress.recallRound(language,round.kind,round.id,round.reviewed,date)
        saveGameProgress(progress)
        mutable.update{it.copy(gameProgress=progress,today=date)}
        after()
    }
    fun setSoundEnabled(enabled:Boolean)=runOperation {
        requireParentIfReady()
        withContext(Dispatchers.IO){check(prefs.edit().putBoolean("sound.enabled",enabled).commit())}
        mutable.update {it.copy(soundEnabled=enabled)}
    }
    fun setAnimationsEnabled(enabled:Boolean)=runOperation {
        requireParentIfReady()
        withContext(Dispatchers.IO){check(prefs.edit().putBoolean("animations.enabled",enabled).commit())}
        mutable.update {it.copy(animationsEnabled=enabled)}
    }
    fun soundOffMessage(forChess:Boolean=false) {mutable.update {it.copy(message=if(forChess)localized(
        "Ses kapalı. Dinlemek için Ayarlar ekranındaki Ses seçeneğini açabilirsin.",
        "Sound is off. Turn on Sound in Settings to listen.")else localized(
        "Ses kapalı. Kelimeleri dinlemek için Ayarlar ekranındaki Ses seçeneğini açabilirsin.",
        "Sound is off. Turn on Sound in Settings to listen to words."))}}
    fun setDailyGoal(goal:Int)=runOperation {
        requireParentIfReady();require(goal in listOf(20,40,60,80,100))
        withContext(Dispatchers.IO){check(prefs.edit().putInt("daily.goal",goal).commit())}
        mutable.update {it.copy(dailyGoal=goal)}
    }
    // Called only by Activity Result callbacks for a picker launched from the unlocked parent area.
    fun saveParentPhoto(uri:android.net.Uri)=runOperation {
        withContext(Dispatchers.IO){ParentPhotoStore.save(getApplication(),uri)}
        mutable.update {it.copy(parentPhotoVersion=it.parentPhotoVersion+1)}
    }
    fun saveParentPhoto(bitmap:android.graphics.Bitmap)=runOperation {
        try {withContext(Dispatchers.IO){ParentPhotoStore.save(getApplication(),bitmap)}}finally {bitmap.recycle()}
        mutable.update {it.copy(parentPhotoVersion=it.parentPhotoVersion+1)}
    }
    fun setHelpEnabled(enabled:Boolean) = runOperation {
        requireParentIfReady()
        withContext(Dispatchers.IO) {check(prefs.edit().putBoolean("help.enabled",enabled).commit())}
        mutable.update {it.copy(helpEnabled=enabled)}
    }
    fun updateDisplayName(name: String) = runOperation {
        val bounded = name.take(30)
        withContext(Dispatchers.IO) { check(prefs.edit().putString("name", bounded).commit()) }
        mutable.update { it.copy(name = bounded) }
    }
    fun saveProfile(name: String, ui: String, language: String, finish: Boolean) = runOperation {
        requireParentIfReady()
        require(language in LearningLanguages.codes && ui in InterfaceStrings.codes)
        if (finish) require(withContext(Dispatchers.IO) { store.load(language) } != null) { localized("Önce dil paketini kur.", "Install the language pack first.") }
        withContext(Dispatchers.IO) {
            check(prefs.edit().putString("name", name.take(30)).putString("ui", ui).putString("language", language)
                .putBoolean("ready", mutable.value.ready || finish).commit()) { "Profile could not be saved" }
        }
        mutable.update { it.copy(name = name.take(30), ui = ui, language = language, ready = it.ready || finish) }
        InterfaceStrings.language=ui
        if (finish && !prefs.getBoolean("analytics.setup_recorded", false)) {
            AppTelemetry.setupCompleted()
            prefs.edit().putBoolean("analytics.setup_recorded", true).apply()
        }
        reload()
    }
    fun visit(lesson: Lesson, word: Word, complete: Boolean, resume: Word = word, after: () -> Unit = {}) = runOperation {
        val current = mutable.value
        check(progressReadable) { "Saved game progress cannot be read" }
        val next = if (complete) LearningRules.complete(current.completed, current.language, word.id) else current.completed
        val today = LocalDate.now()
        var progress = current.gameProgress.prepare(current.language, current.pack!!.words.map { it.id }.toSet(), current.completed, today)
        progress=progress.prepareLessonRewards(current.language,current.pack.lessons.filter { candidate -> candidate.words.all {LearningRules.progressKey(current.language,it.id) in current.completed} }.map {it.id}.toSet())
        if (complete) progress = progress.study(current.language, word.id, LearningRules.progressKey(current.language, word.id) !in current.completed, today)
        if(complete && word.id==lesson.words.last().id && lesson.words.all {LearningRules.progressKey(current.language,it.id) in next}) progress=progress.finishLesson(current.language,lesson.id)
        val encoded = progress.encode()
        withContext(Dispatchers.IO) {
            check(prefs.edit().putString("games.v1", encoded).putStringSet("completed", next).putString("lesson_${current.language}", lesson.id)
                .putString("concept_${current.language}", resume.id).commit()) { "Progress could not be saved" }
        }
        mutable.update { it.copy(completed = next, gameProgress = progress, today = today, lastLesson = lesson.id, lastConcept = resume.id) }
        after()
    }

    private suspend fun saveGameProgress(progress: GameProgress, perfect: Boolean = mutable.value.perfectRound) {
        check(progressReadable) { "Saved game progress cannot be read" }
        withContext(Dispatchers.IO) { check(prefs.edit().putString("games.v1", progress.encode()).putBoolean("companions.perfect", perfect).commit()) { "Progress could not be saved" } }
    }
    private suspend fun refreshDayInternal() {
        val current = mutable.value
        val today = LocalDate.now()
        val pack = current.pack ?: return
        val progress = current.gameProgress.prepare(current.language, pack.words.map { it.id }.toSet(), current.completed, today)
        saveGameProgress(progress)
        mutable.update { it.copy(gameProgress = progress, today = today) }
    }
    fun refreshDay() = runOperation { refreshDayInternal() }
    fun startGame(kind: GameKind, count: Int = 2) = runOperation {
        val generation = ++gameGeneration
        check(progressReadable)
        val current = mutable.value
        val pack = current.pack ?: error("Install a language pack first")
        val targets = if (kind == GameKind.REVIEW) current.gameProgress.reviewIds(current.language, current.completed, pack.words.map { it.id }.toSet()) else null
        val session = withContext(Dispatchers.IO) {
            val pool = GameRules.uniqueWords(pack, current.completed, targets)
            GameRules.start(current.language, kind, pool, count, targets)
        }
        if (generation != gameGeneration) return@runOperation
        if (session != null) AppTelemetry.gameStarted(kind.name)
        mutable.update { it.copy(game = session, message = if (session != null) null else
            if (kind == GameKind.REVIEW && targets.isNullOrEmpty()) localized("Bekleyen tekrarın yok! Yeni kelimeler öğrenebilirsin.", "No words waiting for review! You can learn new words.")
            else localized("Bu oyun için yeterli farklı kelime yok. Önce biraz daha kelime çalış.", "Not enough different words for this game. Study a few more words first.")) }
        refreshDayInternal()
    }
    fun gameAudioStopped() { mutable.update { current ->
        current.copy(game = current.game?.let { if (it.kind == GameKind.MATCHING) it.copy(selected = null) else it })
    } }
    fun endGame() { gameGeneration++; mutable.update { it.copy(game = null) } }
    fun gameHeard(sessionId: String, word: String) { mutable.update { current ->
        current.copy(game = current.game?.let { if (it.id == sessionId) it.hear(word) else it })
    } }
    private fun changeGame(transform: (GameSession) -> GameSession) = runOperation {
        val current = mutable.value
        val before = current.game ?: return@runOperation
        val after = transform(before)
        if (before == after) return@runOperation
        val today = LocalDate.now()
        val prepared = current.gameProgress.prepare(current.language, current.pack!!.words.map { it.id }.toSet(), current.completed, today)
        val progress = prepared.transition(before, after, today)
        val perfect = current.perfectRound || (after.finished && after.kind in listOf(GameKind.LISTENING, GameKind.REVIEW) && after.questions.size >= 4 && after.firstAnswers.values.count { it } == after.questions.size)
        saveGameProgress(progress, perfect)
        mutable.update { it.copy(gameProgress = progress, perfectRound = perfect, today = today, game = if (it.game?.id == before.id) after else it.game) }
    }
    fun gameAnswer(word: String) = changeGame { it.answer(word) }
    fun gameNext() = changeGame { it.next() }
    fun memoryFlip(position: Int) = changeGame { it.flip(position) }
    fun memoryHide() = changeGame { it.hide() }

    private fun requireParentIfReady() { check(mutable.value.parentGate.allowsManagement(mutable.value.ready)) { "Parent gate required" } }
    fun beginParentHold() { mutable.update { it.copy(parentGate = it.parentGate.begin(SystemClock.elapsedRealtime())) } }
    fun tickParentHold() { mutable.update { it.copy(parentGate = it.parentGate.tick(SystemClock.elapsedRealtime())) } }
    fun releaseParentHold() { mutable.update { it.copy(parentGate = it.parentGate.release()) } }
    fun relockParent() { mutable.update { it.copy(parentGate = ParentGate()) } }
    fun answerParentChallenge(a: Int, b: Int, answer: String): Boolean {
        if (a !in 11..19 || b !in 11..19 || answer.trim().toIntOrNull() != a + b) return false
        mutable.update { it.copy(parentGate = ParentGate(unlocked = true)) }; return true
    }
    fun selectCompanion(id: String) = runOperation {
        require(id in mutable.value.earned && Companions.all.any { it.id == id })
        withContext(Dispatchers.IO) { check(prefs.edit().putString("companion", id).commit()) }
        mutable.update { it.copy(companion = id) }
    }
    private suspend fun syncCompanions() {
        if (!progressReadable) return
        val current = mutable.value
        val level = current.finishedLevel || Companions.finishedLevel(current.pack, current.completed)
        val evidence = Companions.evidence(current.completed, current.gameProgress, current.perfectRound, level)
        val earned = Companions.unlocked(evidence, current.earned)
        val selected = current.companion.takeIf { it in earned } ?: "piko-default"
        if (earned != current.earned || level != current.finishedLevel || selected != current.companion) {
            withContext(Dispatchers.IO) { check(prefs.edit().putStringSet("companions.earned", earned).putBoolean("companions.level", level).putString("companion", selected).commit()) }
            mutable.update { it.copy(earned = earned, finishedLevel = level, companion = selected) }
        }
    }

    fun editCustom(index: Int, piece: Piece?) = runOperation { saveCustom(mutable.value.chessCustom.place(index, piece)) }
    fun customTurn(white: Boolean) = runOperation { saveCustom(mutable.value.chessCustom.copy(whiteTurn = white)) }
    fun customCastling(allow: Boolean) = runOperation { saveCustom(mutable.value.chessCustom.copy(allowCastling = allow)) }
    fun resetCustom(standard: Boolean) = runOperation { saveCustom(if (standard) ChessCustomPosition.standard() else mutable.value.chessCustom.copy(board=List(64){null},allowCastling=false)) }
    fun moveCustom(from:Int,to:Int)=runOperation{saveCustom(mutable.value.chessCustom.move(from,to))}
    fun arrangeCustom(white:Boolean,random:Boolean=false)=runOperation {
        val current=mutable.value.chessCustom
        val next=if(random)current.randomize(white)else current.arrange(white)
        if(next!=null)saveCustom(next)else mutable.update{it.copy(message=localized("Diğer tarafın taşlarını kontrol et; dizim için uygun kare bulunamadı.","Check the other side's pieces; no suitable arrangement was found."))}
    }
    fun undoCustom()=runOperation {
        val history=mutable.value.customUndo
        val previous=history.lastOrNull() ?: return@runOperation
        saveCustom(previous,false)
        mutable.update{it.copy(customUndo=history.dropLast(1))}
    }
    private suspend fun saveCustom(custom: ChessCustomPosition,recordUndo:Boolean=true) {
        check(!mutable.value.chessCustomSaveError) { "Unreadable custom draft; existing record preserved" }
        val before=mutable.value.chessCustom
        if(before==custom)return
        withContext(Dispatchers.IO) { check(prefs.edit().putString("chess.custom.v1", custom.encode()).commit()) }
        mutable.update { it.copy(chessCustom=custom,customUndo=if(recordUndo)(it.customUndo+before).takeLast(50)else it.customUndo) }
    }
    fun startCustom(computer: Boolean, playerWhite: Boolean, minutes: Int = 0, difficulty: ChessDifficulty = ChessDifficulty.NORMAL, started: () -> Unit) = runOperation {
        check(!mutable.value.chessSaveError && !mutable.value.chessCustomSaveError)
        val custom = mutable.value.chessCustom
        check(custom.issue == null)
        chessGeneration++
        saveChess(ChessGame(computer = computer, playerWhite = playerWhite, position = custom.position, startFen = custom.fen(), clock = ChessClock.minutes(minutes), difficulty = difficulty))
        chessPendingMillis = 0; chessAnchor = if (chessBoardVisible && chessForeground) SystemClock.elapsedRealtime() else null
        started()
    }

    fun playOpening(id: String, expectedCount: Int, move: ChessMove) = runOperation {
        check(!mutable.value.chessOpeningSaveError) { "Unreadable opening progress; existing record preserved" }
        saveOpeningProgress(mutable.value.chessOpeningProgress.advance(id, expectedCount, move))
    }
    fun rewindOpening(id: String, restart: Boolean = false) = runOperation {
        check(!mutable.value.chessOpeningSaveError) { "Unreadable opening progress; existing record preserved" }
        saveOpeningProgress(mutable.value.chessOpeningProgress.rewind(id, restart))
    }
    private suspend fun saveOpeningProgress(progress: ChessOpeningProgress) {
        if (progress == mutable.value.chessOpeningProgress) return
        withContext(Dispatchers.IO) { check(prefs.edit().putString("chess.openings.v1", progress.encode()).commit()) }
        mutable.update { it.copy(chessOpeningProgress = progress) }
    }

    fun answerChessEnding(id: String, answer: ChessEnding) = runOperation {
        saveChessPuzzleProgress(ChessPuzzles.finishEnding(mutable.value.chessPuzzleCompleted, id, answer))
    }
    fun solveChessMate(id: String, move: ChessMove) = runOperation {
        val completed = withContext(Dispatchers.Default) { ChessPuzzles.finishMate(mutable.value.chessPuzzleCompleted, id, move) }
        saveChessPuzzleProgress(completed)
    }
    private suspend fun saveChessPuzzleProgress(completed: Set<String>) {
        if (completed == mutable.value.chessPuzzleCompleted) return
        withContext(Dispatchers.IO) { check(prefs.edit().putStringSet("chess.puzzles.v1", completed).commit()) }
        mutable.update { it.copy(chessPuzzleCompleted = completed) }
    }

    fun placeChessPiece(expectedCount: Int, kind: PieceKind, target: Int) = runOperation {
        val count = mutable.value.chessSetupCount
        if (count != expectedCount) return@runOperation
        val next = ChessBoardSetup.place(count, kind, target)
        if (next == count) return@runOperation
        withContext(Dispatchers.IO) { check(prefs.edit().putInt("chess.setup.v1", next).commit()) }
        mutable.update { it.copy(chessSetupCount = next) }
    }
    fun restartChessSetup() = runOperation {
        withContext(Dispatchers.IO) { check(prefs.edit().putInt("chess.setup.v1", 0).commit()) }
        mutable.update { it.copy(chessSetupCount = 0) }
    }
    fun finishChessSafety(id: String, from: Int?, to: Int) = runOperation {
        val completed = ChessSafety.finish(mutable.value.chessSafetyCompleted, id, from, to)
        if (completed == mutable.value.chessSafetyCompleted) return@runOperation
        withContext(Dispatchers.IO) { check(prefs.edit().putStringSet("chess.safety.v1", completed).commit()) }
        mutable.update { it.copy(chessSafetyCompleted = completed) }
    }

    fun finishChessLesson(taskId: String, from: Int, to: Int) = runOperation {
        val completed = ChessLessons.finish(mutable.value.chessLessonCompleted, taskId, from, to)
        if (completed == mutable.value.chessLessonCompleted) return@runOperation
        withContext(Dispatchers.IO) { check(prefs.edit().putStringSet("chess.lessons.v1", completed).commit()) }
        mutable.update { it.copy(chessLessonCompleted = completed) }
    }

    private var chessBoardVisible = false
    private var chessForeground = false
    private var chessAnchor: Long? = null
    private var chessPendingMillis = 0L
    private fun accrueChessTime() {
        val now = SystemClock.elapsedRealtime()
        chessAnchor?.let { chessPendingMillis += (now - it).coerceAtLeast(0) }
        chessAnchor = if (chessBoardVisible && chessForeground) now else null
    }
    fun chessBoardVisible(visible: Boolean) {
        accrueChessTime()
        chessBoardVisible = visible
        chessAnchor = if (visible && chessForeground) SystemClock.elapsedRealtime() else null
    }
    fun chessForeground(foreground: Boolean) {
        accrueChessTime()
        chessForeground = foreground
        chessAnchor = if (foreground && chessBoardVisible) SystemClock.elapsedRealtime() else null
    }
    private suspend fun settleChessTime() {
        accrueChessTime()
        val elapsed = chessPendingMillis
        chessPendingMillis = 0
        val game = mutable.value.chess ?: return
        val next = game.elapse(elapsed)
        if (next != game) try { saveChess(next, clearHint = false) } catch (e: Exception) { chessPendingMillis += elapsed; throw e }
    }
    init { viewModelScope.launch {
        while (isActive) {
            delay(1000)
            // Persist clock ticks through the same serialized operation path without flashing
            // the board and every action into their disabled appearance once per second.
            if (((chessAnchor != null && mutable.value.chess?.clock != null && mutable.value.chess?.outcome == null) || chessPendingMillis > 0) && operation?.isActive != true) runOperation(indicateBusy = false) { settleChessTime() }
        }
    } }

    private var chessGeneration = 0
    private suspend fun loadChess() {
        val result = withContext(Dispatchers.IO) { runCatching {
            val game = prefs.getString("chess.game.v1", null)?.let(ChessGame::decode)
            val stats = prefs.getString("chess.stats.v1", null)?.let(ChessStatistics::decode) ?: ChessStatistics()
            game to stats
        } }
        result.onSuccess { pair -> mutable.update { it.copy(chess = pair.first, chessStats = pair.second) } }
            .onFailure { mutable.update { it.copy(chessSaveError = true) } }
    }
    private suspend fun saveChess(game: ChessGame, clearHint: Boolean = true) {
        val stats = withContext(Dispatchers.Default) { mutable.value.chessStats.record(game) }
        withContext(Dispatchers.IO) { check(prefs.edit().putString("chess.game.v1", game.encode()).putString("chess.stats.v1", stats.encode()).commit()) { "Chess could not be saved" } }
        mutable.update { it.copy(chess = game, chessStats = stats, chessHint = if (clearHint) null else it.chessHint, chessSaveError = false) }
    }
    fun newChess(computer: Boolean, playerWhite: Boolean, minutes: Int = 0, difficulty: ChessDifficulty = ChessDifficulty.NORMAL) = runOperation {
        check(!mutable.value.chessSaveError) { "Unreadable chess save; preserve it for recovery" }
        chessGeneration++
        chessPendingMillis = 0; chessAnchor = if (chessBoardVisible && chessForeground) SystemClock.elapsedRealtime() else null
        saveChess(ChessGame(computer = computer, playerWhite = playerWhite, clock = ChessClock.minutes(minutes), difficulty = difficulty))
        AppTelemetry.chessStarted(computer)
    }
    fun configureChessClock(enabled:Boolean,minutes:Int) = runOperation {
        check(!mutable.value.chessSaveError)
        settleChessTime()
        val game=mutable.value.chess ?: return@runOperation
        val next=game.configureClock(enabled,minutes)
        if(next!=game) {
            chessGeneration++
            saveChess(next,clearHint=false)
            chessPendingMillis=0
            chessAnchor=if(chessBoardVisible&&chessForeground)SystemClock.elapsedRealtime()else null
        }
    }
    fun chessMove(move: ChessMove) = runOperation {
        settleChessTime()
        val game = mutable.value.chess ?: return@runOperation
        if (game.computerTurn) return@runOperation
        val next = withContext(Dispatchers.Default) { game.play(move) } ?: return@runOperation
        saveChess(next)
    }
    fun chessComputerMove() = runOperation {
        settleChessTime()
        val before = mutable.value.chess ?: return@runOperation
        if (!before.computerTurn) return@runOperation
        val generation = chessGeneration
        val move = withContext(Dispatchers.Default) {
            val context = currentCoroutineContext()
            before.position.computerMove(difficulty = before.difficulty, checkCancellation = { context.ensureActive() })
        }
        settleChessTime()
        val current = mutable.value.chess
        val next = if (move != null && current?.id == before.id) current.play(move) else null
        if (next != null && generation == chessGeneration) saveChess(next)
    }
    fun chessUndo() = runOperation {
        settleChessTime()
        chessGeneration++
        val game = mutable.value.chess ?: return@runOperation
        val previous = withContext(Dispatchers.Default) { game.undo() } ?: return@runOperation
        saveChess(previous)
    }
    fun chessHint() = runOperation {
        settleChessTime()
        val game = mutable.value.chess ?: return@runOperation
        if (game.computerTurn) return@runOperation
        val hint = withContext(Dispatchers.Default) { game.position.computerMove() }
        mutable.update { it.copy(chessHint = hint) }
    }

    fun recoverChessSave() = runOperation {
        requireParentIfReady()
        check(mutable.value.chessSaveError)
        withContext(Dispatchers.IO) {
            check(prefs.edit().putString("chess.recovery.game", prefs.getString("chess.game.v1", null))
                .putString("chess.recovery.stats", prefs.getString("chess.stats.v1", null))
                .remove("chess.game.v1").remove("chess.stats.v1").commit())
        }
        mutable.update { it.copy(chess = null, chessStats = ChessStatistics(), chessSaveError = false) }
    }
}
