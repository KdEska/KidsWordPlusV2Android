package com.demarpsoftware.kidswordplus

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

internal data class ChessSecretCopy(val id:String,val turkish:String,val english:String)
internal data class ChessSecretDeck(val remaining:List<String> = emptyList(),val last:String? = null) {
    fun draw(ids:List<String>,random:Random=Random.Default):Pair<ChessSecretDeck,String> {
        require(ids.isNotEmpty() && ids.distinct().size==ids.size)
        val valid=ids.toSet()
        val pending=remaining.filter {it in valid}.distinct().ifEmpty {ids.shuffled(random)}.toMutableList()
        if(pending.size>1 && pending.first()==last) {val first=pending[0];pending[0]=pending[1];pending[1]=first}
        val selected=pending.removeAt(0)
        return ChessSecretDeck(pending,selected) to selected
    }
    fun encode()=JSONObject().put("remaining",JSONArray(remaining)).put("last",last ?: JSONObject.NULL).toString()
    companion object {
        fun decode(value:String?):ChessSecretDeck=runCatching {
            val root=JSONObject(value ?: "{}")
            val ids=root.optJSONArray("remaining") ?: JSONArray()
            ChessSecretDeck((0 until ids.length()).map {ids.getString(it)},root.optString("last").takeIf {it.isNotBlank()&&it!="null"})
        }.getOrDefault(ChessSecretDeck())
    }
}
internal object ChessSecrets {
    private var selected:ChessSecretCopy?=null
    @Synchronized fun current(context:Context):ChessSecretCopy {
        selected?.let {return it}
        val prefs=context.applicationContext.getSharedPreferences("chess_secrets_v1",Context.MODE_PRIVATE)
        val (deck,id)=ChessSecretDeck.decode(prefs.getString("deck",null)).draw(chessSecretCopies.map {it.id})
        prefs.edit().putString("deck",deck.encode()).apply()
        return chessSecretCopies.first {it.id==id}.also {selected=it}
    }
}

// BEGIN GENERATED CHESS SECRETS
internal val chessSecretCopies=listOf(
    ChessSecretCopy("secret.001","Taşlarını merkeze yaklaştır; daha çok kareyi kontrol et!","Bring your pieces toward the center to control more squares!"),
    ChessSecretCopy("secret.002","Hamle yapmadan önce sor: Şahım güvende mi?","Before you move, ask: Is my king safe?"),
    ChessSecretCopy("secret.003","Aynı taşı tekrar tekrar oynatmak yerine yeni taşlarını oyuna kat!","Bring new pieces into play instead of moving the same piece again and again!"),
    ChessSecretCopy("secret.004","Hamleni seçmeden önce rakibinin son hamlesine bak.","Look at your opponent's last move before choosing yours."),
    ChessSecretCopy("secret.005","Şah çekme, taş alma ve tehdit fırsatlarına bak.","Look for checks, captures and threats."),
    ChessSecretCopy("secret.006","Taş almadan önce saldıran ve savunan taşları say.","Before a capture, count the attackers and defenders."),
    ChessSecretCopy("secret.007","Hamlenin bir taşını karşılıksız vermediğini kontrol et.","Check that your move does not give away a piece for free."),
    ChessSecretCopy("secret.008","Savunmasız bir taşın onu koruyacak bir arkadaşa ihtiyacı olabilir.","An undefended piece may need a friend to protect it."),
    ChessSecretCopy("secret.009","Atlarını ve fillerini oyuna erken katmayı dene.","Try bringing your knights and bishops into play early."),
    ChessSecretCopy("secret.010","Piyonlar düz önlerindeki değil, çaprazlarındaki taşı alır.","Pawns capture diagonally, not straight ahead."),
    ChessSecretCopy("secret.011","Piyon geri gidemez; yolunu dikkatle seç.","A pawn cannot move backward, so choose its path carefully."),
    ChessSecretCopy("secret.012","At, diğer taşların üzerinden atlayabilir.","The knight can jump over other pieces."),
    ChessSecretCopy("secret.013","Fil hep aynı renkteki karelerde kalır.","A bishop stays on squares of the same color."),
    ChessSecretCopy("secret.014","Kalenin rahatça ilerleyebileceği açık yollar ara.","Look for clear lines where your rook can move freely."),
    ChessSecretCopy("secret.015","Vezir güçlüdür ama yine de desteğe ihtiyaç duyar.","The queen is strong, but she still needs support."),
    ChessSecretCopy("secret.016","Oyun sonunda şahın da yardım edebilir. Onu güvende tut!","In the endgame, your king can help too. Keep it safe!"),
    ChessSecretCopy("secret.017","Taşların bir takım gibi birlikte çalışsın.","Let your pieces work together as a team."),
    ChessSecretCopy("secret.018","Kendine sor: Rakibim ne yapmak istiyor?","Ask yourself: What does my opponent want to do?"),
    ChessSecretCopy("secret.019","Bir taşın tehdit altındaysa güvenli bir cevap ara.","If a piece is attacked, look for a safe reply."),
    ChessSecretCopy("secret.020","Şah çekildiğinde şahını güvene almalısın.","When your king is in check, you must make it safe."),
    ChessSecretCopy("secret.021","Pat, yenilgi değil beraberliktir.","Stalemate is a draw, not a defeat."),
    ChessSecretCopy("secret.022","Acele etme. Tahtaya bir kez daha bak.","Do not rush. Take one more look at the board."),
    ChessSecretCopy("secret.023","Her hata sana yeni bir şey öğretebilir.","Every mistake can teach you something new."),
    ChessSecretCopy("secret.024","Süren varsa iki farklı hamleyi karşılaştır.","If you have time, compare two possible moves."),
    ChessSecretCopy("secret.025","Çatalda bir taş aynı anda iki hedefe saldırır.","A fork attacks two targets with one piece."),
    ChessSecretCopy("secret.026","Şahının önündeki taşı oynatmadan önce arkasındaki yolu kontrol et.","Before moving a piece in front of your king, check the line behind it."),
    ChessSecretCopy("secret.027","Şahının kaçabileceği güvenli bir kare var mı, kontrol et.","Check whether your king has a safe escape square."),
    ChessSecretCopy("secret.028","Son sıraya ulaşan piyon; vezir, kale, fil veya at olabilir.","A pawn that reaches the last rank can become a queen, rook, bishop or knight."),
    ChessSecretCopy("secret.029","İki kale açık bir yol üzerinde birbirini destekleyebilir.","Two rooks can support each other along a clear line."),
    ChessSecretCopy("secret.030","Filin çaprazını sonuna kadar gözünle takip et.","Look along a bishop's diagonal all the way to the end."),
    ChessSecretCopy("secret.031","Merkeze yakın bir atın çoğu zaman daha çok seçeneği vardır.","A knight near the center often has more choices."),
    ChessSecretCopy("secret.032","Her saldırı şah değildir. Hangi taşın tehdit edildiğini bul.","Not every attack is check. Find which piece is threatened."),
    ChessSecretCopy("secret.033","Önde olsan da şahını güvende tut.","Even when you are ahead, keep your king safe."),
    ChessSecretCopy("secret.034","Zor bir konum, akıllıca bir fikir aramak için fırsattır.","A difficult position is a chance to look for a clever idea."),
    ChessSecretCopy("secret.035","Beraberlik de güzel bir sonuç olabilir.","A draw can be a good result too."),
    ChessSecretCopy("secret.036","Rokta şahın ve bir kalen aynı hamlede hareket eder.","Castling moves your king and a rook in the same move."),
    ChessSecretCopy("secret.037","Rakibinin savunmasız taşlarını ara.","Look for your opponent's undefended pieces."),
    ChessSecretCopy("secret.038","Bir taşa dokunmadan önce küçük bir plan yap.","Make a small plan before touching a piece."),
    ChessSecretCopy("secret.039","Oyun bitince gurur duyduğun bir hamleni bul.","After a game, find one move you are proud of."),
    ChessSecretCopy("secret.040","Bir taş yoldan çekilince yeni bir tehdit ortaya çıkabilir.","A threat may appear when another piece moves out of the way.")
)
// END GENERATED CHESS SECRETS
