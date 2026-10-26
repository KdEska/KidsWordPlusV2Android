package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun ParentPrivacyScreen(tr:Boolean) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val sections=listOf(
        t("Konuşma pratiği","Speaking practice") to t("Mikrofon yalnızca çocuk konuşma düğmesine dokunduğunda açılır. En fazla 5 saniye dinlenir. Ses cihazda değerlendirilir; uygulama ses kaydı saklamaz veya sunucuya göndermez.","The microphone opens only when your child taps the speaking button. Listening lasts up to 5 seconds. Audio is processed on the device; the app does not store recordings or upload audio."),
        t("Geri bildirim","Feedback") to t("Gönderdiğinizde mesajınız ve bu teknik bilgiler destek sunucumuzda saklanır, info@kidswordplus.com adresine e-postayla iletilir.","When you send, your message and these technical details are saved on our support server and emailed to info@kidswordplus.com."),
        t("Öğrenme verileri","Learning data") to t(
            "Çocuğunuzun adı, seçilen karakter, profil fotoğrafı, öğrenme ilerlemesi ve tercihler bu cihazda saklanır.",
            "Your child's name, selected companion, profile photo, learning progress and preferences are stored on this device."),
        t("Profil fotoğrafı","Profile photo") to t(
            "Galeriden yalnızca seçtiğiniz fotoğraf okunur. İsterseniz kamera seçeneğini kullanabilirsiniz. Fotoğrafın küçük bir kopyası uygulamanın bu cihazdaki özel alanında saklanır; özgün galeri fotoğrafınız değiştirilmez.",
            "Only the photo you select from the gallery is read. You can also choose to use the camera. A small copy is stored in the app's private storage on this device; your original gallery photo is not changed."),
        t("Dil paketleri","Language packs") to t(
            "Uygulamayı kullanmak için internet bağlantısı gerekir. Dil paketleri bu cihazda saklanır. Paket kataloğunu ve seçtiğiniz paketleri indirmek veya güncellemek için internet bağlantısı kullanılır. Dil paketini kaldırmak öğrenme ilerlemesini silmez.",
            "An internet connection is required to use the app. Language packs are stored on this device. An internet connection is used to download or update the package catalog and the packs you select. Removing a language pack does not delete learning progress."),
        t("Aile paylaşımı","Family sharing") to t(
            "Başarı kartları yalnızca paylaşımı açtığınızda hazırlanır. Ön izlemedeki kartı Paylaş düğmesiyle Android paylaşım menüsüne aktarabilirsiniz. Kart seçtiğiniz uygulamayla paylaşılır; çocuğun adı ve profil fotoğrafı kartlara eklenmez. Hazırlanan kartlar uygulamanın geçici önbelleğinde saklanır.",
            "Achievement cards are prepared only when you open sharing. Use Share in the preview to send the card to the Android share menu and choose an app. Your child's name and profile photo are not included in the cards. Prepared cards are stored in the app's temporary cache."),
        t("Tanılama ve bildirimler","Diagnostics and notifications") to t(
            "Firebase Crashlytics teknik hata raporlarını ve Google Analytics kullanım olaylarını Google’a gönderir. Çocuğun adı, fotoğrafı ve öğrenilen kelimeler bunlara eklenmez. Reklam kimliği toplama ve reklam kişiselleştirme kapalıdır.",
            "Firebase Crashlytics sends technical error reports and Google Analytics sends usage events to Google. Your child's name, photo and studied words are not included. Advertising ID collection and ad personalization are disabled."),
        t("Bildirimler","Notifications") to t(
            "Push bildirimleri isteğe bağlıdır. Açıldığında Firebase Cloud Messaging, mesajları ulaştırmak için kurulum kimliği ve bildirim belirteci kullanır. Kapatıldığında kayıt durur ve bildirim belirteci silinir.",
            "Push notifications are optional. When enabled, Firebase Cloud Messaging uses an installation identifier and a notification token to deliver messages. Turning this off stops registration and deletes the notification token."),
        t("Öğrenme hatırlatmaları","Learning reminders") to t(
            "Öğrenme hatırlatmaları isteğe bağlıdır ve bu cihazda planlanır. Hatırlatma saatleri cihazda kalır; profil veya ilerleme verileri Firebase’e gönderilmez.",
            "Learning reminders are optional and scheduled on this device. Reminder times stay on the device; no profile or progress data is sent to Firebase."),
        t("Verileri silme","Deleting data") to t(
            "Yerel verileri silmek için Android ayarlarından uygulamanın depolama verilerini temizleyebilir veya uygulamayı kaldırabilirsiniz. Bu işlem profil fotoğrafını, indirilen paketleri ve bu cihazdaki öğrenme ilerlemesini siler.",
            "To delete local data, clear the app's storage in Android settings or uninstall the app. This removes the profile photo, downloaded packs and learning progress stored on this device.")
    )
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth().privacyCard(20),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            DesignArt("ui/parent/privacy.webp",Modifier.size(60.dp),decodeLimit=256)
            Text(t("Gizlilik","Privacy"),style=MaterialTheme.typography.headlineMedium,color=KidHeading,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading()})
        }
        sections.forEach {(title,body) ->
            Column(Modifier.fillMaxWidth().privacyCard(22),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(title,style=MaterialTheme.typography.titleLarge.copy(fontSize=20.sp,lineHeight=26.sp),color=KidHeading,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading()})
                SelectionContainer {Text(body,style=MaterialTheme.typography.bodyLarge.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.NORMAL)),fontWeight=FontWeight.Medium,letterSpacing=0.sp,lineHeight=27.sp),color=KidBody)}
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
private fun Modifier.privacyCard(padding:Int)=background(Color.White,RoundedCornerShape(24.dp)).border(1.dp,KidHeading.copy(alpha=.12f),RoundedCornerShape(24.dp)).padding(padding.dp)
