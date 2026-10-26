package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ChessSpeechTests {
    private val assets = File("src/main/assets/chess/voices")
    private fun catalog(language: String) = File(assets, language).let { ChessSpeechCatalog(File(it,"content.json").readText(), File(it,"audio-index.json").readText()) }
    private fun move(from: String, to: String) = ChessMove(square(from), square(to))
    @Test fun bothOfflineInventoriesContainEveryReferencedRecording() {
        for (language in listOf("tr","en")) {
            val catalog = catalog(language)
            assertEquals(269, catalog.clips.size)
            for (clip in catalog.clips) assertTrue(File(assets,"$language/${clip.path}").length() > 0)
            assertTrue(catalog.resolve(catalog.text("king.move")).isNotEmpty())
        }
    }
    @Test fun allCurrentLessonAndSetupPromptsResolveInBothLanguages() {
        for (language in listOf("tr","en")) {
            val c = catalog(language)
            val keys = PieceKind.entries.map { it.name.lowercase()+".move" } +
                listOf("setup.complete", "ending.prompt", "ending.check.body", "ending.mate.body", "ending.stalemate.body") +
                ChessSafety.tasks.map { it.audioKey } + ChessOpenings.all.flatMap { it.steps }.map { "opening.step."+it.idea }
            for (key in keys) assertTrue("$language/$key", c.resolve(c.text(key)).isNotEmpty())
            for (task in ChessLessons.tasks) assertTrue(c.resolve(c.text(if(task.capture) "training.capture" else "training.target", mapOf("piece" to c.text(task.kind.name.lowercase()+".name"),"square" to squareName(task.target)))).isNotEmpty())
            for ((square,piece) in ChessBoardSetup.placements) assertTrue(c.resolve(c.text("setup.place", mapOf("piece" to c.text(piece.kind.name.lowercase()+".name"),"square" to squareName(square)))).isNotEmpty())
        }
    }
    @Test fun movesBothColorsCaptureEnPassantAndPromotionResolve() {
        var game = ChessGame(computer = false)
        val positions = ArrayList<ChessGame>()
        for (m in listOf(move("e2","e4"),move("d7","d5"),move("e4","d5"))) { game=game.play(m)!!; positions+=game }
        var ep=ChessGame(computer=false)
        for(m in listOf(move("e2","e4"),move("a7","a6"),move("e4","e5"),move("d7","d5"),move("e5","d6"))) ep=ep.play(m)!!
        positions+=ep
        val origin=ChessCustomPosition.fromFen("7k/P7/8/8/8/8/8/K7 w - - 0 1")
        positions+=ChessGame(computer=false,position=origin.position,startFen=origin.fen()).play(move("a7","a8"))!!
        for(language in listOf("tr","en")) {
            val c=catalog(language)
            assertNull(c.lastMove(ChessGame()))
            for(position in positions) {
                val spoken=c.lastMove(position)!!
                assertFalse(spoken.contains('{'));assertTrue(c.resolve(spoken).isNotEmpty())
                assertTrue(spoken.contains(squareName(position.history.last().to)))
            }
            assertTrue(c.lastMove(ep)!!.contains(c.text("speech.capture",mapOf("piece" to c.text("pawn.name")))))
        }
    }
    @Test fun normalizationAndUnknownContentDoNotSilentlyProducePartialAudio() {
        assertEquals("i şah e4", ChessSpeechCatalog.normalize("İ  Şah, e4!"))
        val c=catalog("tr")
        assertThrows(IllegalStateException::class.java) { c.resolve(c.text("king.move")+" unknownmissingrecording") }
        assertThrows(IllegalArgumentException::class.java) { c.text("training.target") }
        assertThrows(IllegalArgumentException::class.java) { ChessSpeechCatalog("{}", """{"clips":[{"text":"x","file":"../x.mp3"}]}""") }
    }
    @Test fun machinePieceNamesResolveUsingRecordingLanguage() {
        for(language in listOf("tr","en")) {
            val c=catalog(language)
            for((target,piece) in ChessBoardSetup.placements) {
                val prompt=c.prompt("setup.place",mapOf("piece" to piece.kind.name.lowercase(),"square" to squareName(target)))
                assertTrue(c.resolve(prompt).isNotEmpty())
                assertEquals(c.text("setup.place",mapOf("piece" to c.text(piece.kind.name.lowercase()+".name"),"square" to squareName(target))),prompt)
            }
        }
    }
}
