package com.demarpsoftware.kidswordplus

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.demarpsoftware.kidswordplus.chess.*
import kotlinx.coroutines.*

internal val LocalChessAudio = staticCompositionLocalOf<ChessAudioPlayer?> { null }
internal class ChessAudioPlayer(private val context: Context, private val beforePlay: () -> Unit,private val soundEnabled:()->Boolean={true},private val soundDisabled:()->Unit={}) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val catalogs = java.util.concurrent.ConcurrentHashMap<String, ChessSpeechCatalog>()
    private val manager = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { if (it < 0) stop() }.build()
    private var focused = false
    private var player: MediaPlayer? = null
    private var requestJob: Job? = null
    private var generation = 0
    private var language = "tr"
    private data class Batch(val text: String, val paths: List<String>)
    private val pending = ArrayDeque<Batch>()
    private val current = ArrayDeque<String>()
    var playing by mutableStateOf(false); private set
    var transcript by mutableStateOf(""); private set
    var error by mutableStateOf<String?>(null); private set
    var automatic by mutableStateOf(context.getSharedPreferences("profile", 0).getBoolean("chess.narration.v1", true)); private set
    fun automatic(enabled: Boolean) {
        automatic = enabled
        context.getSharedPreferences("profile", 0).edit().putBoolean("chess.narration.v1", enabled).apply()
        if (!enabled) stop()
    }
    fun celebrate(ui: String) {
        stop()
        if (!soundEnabled()) return
        beforePlay()
        language = if (ui == "tr") "tr" else "en"
        val effect = listOf("congratulations", "perfect", "well_done", "youaregreat", "youareperfect", "youknowright").random()
        pending.addLast(Batch("", listOf("effects/effect_${effect}_${language}.mp3")))
        playing = true
        pump()
    }
    fun playKey(language: String, key: String, values: Map<String, String> = emptyMap()) = request(language, false) { it.prompt(key, values) }
    fun playSetup(language:String,kind:PieceKind,square:Int)=request(language,false) {catalog ->
        catalog.prompt("setup.place",mapOf("piece" to kind.name.lowercase(),"square" to squareName(square)))
    }
    fun playMove(language: String, game: ChessGame, enqueue: Boolean) {if(soundEnabled())request(language, enqueue) { it.lastMove(game) ?: error("No move") }}
    private fun request(requestedLanguage: String, enqueue: Boolean, text: (ChessSpeechCatalog) -> String) {
        val language=if(requestedLanguage=="tr")"tr"else "en"
        if(!soundEnabled()) {
            stop();soundDisabled()
            return
        }
        if (!enqueue || this.language != language) stop()
        this.language = language
        beforePlay()
        val token = generation
        val previous = requestJob
        playing = true; error = null
        requestJob = scope.launch {
            try {
                previous?.join()
                val batch = withContext(Dispatchers.IO) {
                    val catalog = catalogs[language] ?: ChessSpeechCatalog(
                        context.assets.open("chess/voices/$language/content.json").bufferedReader().use { it.readText() },
                        context.assets.open("chess/voices/$language/audio-index.json").bufferedReader().use { it.readText() }).also { catalogs[language] = it }
                    val spoken = text(catalog)
                    Batch(spoken, catalog.resolve(spoken).map { "chess/voices/$language/$it" })
                }
                if (token != generation) return@launch
                if (pending.size >= 4) pending.removeFirst()
                pending.addLast(batch)
                pump()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { if (token == generation) fail() }
        }
    }
    private fun pump() {
        if (player != null) return
        if (current.isEmpty() && pending.isNotEmpty()) {
            val batch = pending.removeFirst(); transcript = batch.text; current.addAll(batch.paths)
        }
        if (current.isEmpty()) { playing = false; abandonFocus(); return }
        if (!focused) {
            focused = manager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (!focused) { fail(); return }
        }
        val path = current.removeFirst()
        try {
            val media = MediaPlayer(); player = media
            media.setAudioAttributes(attributes)
            context.assets.openFd(path).use { media.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            media.setOnPreparedListener { if (player === it) runCatching { it.start() }.onFailure { fail() } }
            media.setOnCompletionListener { if (player === it) { it.release(); player = null; pump() } }
            media.setOnErrorListener { source, _, _ -> if (player === source) fail(); true }
            media.prepareAsync()
        } catch (_: Exception) { fail() }
    }
    private fun abandonFocus() { if (focused) manager.abandonAudioFocusRequest(focus); focused = false }
    private fun fail() { stop(); error = if (language == "tr") "Ses oynatılamadı. Yeniden deneyebilirsin." else "Audio could not play. Try again." }
    fun stop() {
        generation++; requestJob?.cancel(); requestJob = null
        pending.clear(); current.clear(); player?.release(); player = null
        playing = false; transcript = ""; error = null; abandonFocus()
    }
    fun release() { stop(); scope.cancel() }
}
@Composable internal fun ChessListenButton(language: String, key: String, values: Map<String, String> = emptyMap(), label: String? = null) {
    val audio = LocalChessAudio.current ?: return
    KidAction(label ?: if (language == "tr") "Dinle" else "Listen", tone = 1) { audio.playKey(language, key, values) }
}
@Composable internal fun ChessAudioStatus(language: String) {
    val audio = LocalChessAudio.current ?: return
    // The original recording transcript remains visible while listening.
    if (audio.playing && audio.transcript.isNotEmpty()) Text(audio.transcript)
    if (audio.playing) TextButton(onClick = { audio.stop() }) { Text(if (language == "tr") "Sesi durdur" else "Stop audio") }
    audio.error?.let { Text(it) }
}
@Composable internal fun ChessStopSpeechOnChange(vararg keys: Any?) {
    val audio = LocalChessAudio.current
    DisposableEffect(*keys) { onDispose { audio?.stop() } }
}
