package com.bernacelik.altinimsahtemi.collector.data.upload

import android.content.Context
import android.util.Log
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureEntity
import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Bir kaydın buluta aktarılması.
 *
 * Arayüz olmasının sebebi: saha çalışması backend kurulumunu beklememeli.
 * `google-services.json` yokken [NoopUploader] devreye girer, kayıtlar diskte
 * `PENDING` olarak birikir ve yapılandırma tamamlandığında geriye dönük yüklenir.
 */
interface CaptureUploader {

    /** Yükleyici gerçekten yapılandırılmış mı. */
    val isConfigured: Boolean

    suspend fun upload(session: SessionEntity, capture: CaptureEntity)

    companion object {
        /**
         * Firebase yapılandırılmışsa gerçek yükleyiciyi, değilse [NoopUploader] döner.
         *
         * [FirebaseApp.initializeApp] yalnızca `google-services.json` derlemeye
         * dahil edildiğinde null olmayan bir değer verir — tespit için en güvenilir yol budur.
         */
        fun create(context: Context): CaptureUploader {
            val app = runCatching { FirebaseApp.initializeApp(context) }.getOrNull()
            return if (app != null) {
                FirebaseCaptureUploader()
            } else {
                Log.w("CaptureUploader", "Firebase yapılandırılmadı — kayıtlar yerel kuyrukta bekleyecek")
                NoopUploader()
            }
        }
    }
}

/** Firebase kurulumu tamamlanana kadar kullanılan yer tutucu. */
class NoopUploader : CaptureUploader {
    override val isConfigured = false

    override suspend fun upload(session: SessionEntity, capture: CaptureEntity) {
        throw NotConfiguredException()
    }

    class NotConfiguredException :
        IllegalStateException("Firebase yapılandırılmadı (google-services.json eksik)")
}

/**
 * WAV'ı Storage'a, metadata'yı Firestore'a yazar.
 *
 * Storage yolu materyal ve obje tipine göre klasörlenir; böylece kova, indirmeden
 * de gözle taranabilir bir veri seti mimarisi sunar.
 */
class FirebaseCaptureUploader : CaptureUploader {

    override val isConfigured = true

    override suspend fun upload(session: SessionEntity, capture: CaptureEntity) {
        ensureSignedIn()

        val wav = File(capture.wavPath)
        require(wav.exists()) { "WAV dosyası bulunamadı: ${capture.wavPath}" }

        val storagePath = storagePath(session, capture)
        FirebaseStorage.getInstance().reference.child(storagePath)
            .putStream(wav.inputStream())
            .await()

        val metadataJson = File(capture.jsonPath).takeIf { it.exists() }?.readText()
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CAPTURES)
            .document(capture.id)
            .set(
                mapOf(
                    "captureId" to capture.id,
                    "sessionId" to capture.sessionId,
                    "sequence" to capture.sequence,
                    "storagePath" to storagePath,
                    "material" to session.materialSlug,
                    "objectType" to session.objectTypeSlug,
                    "surface" to session.surfaceSlug,
                    "strikeMethod" to session.strikeMethodSlug,
                    "isGenuineGold" to session.isGenuineGold,
                    "sampleRate" to session.sampleRate,
                    "audioSource" to session.audioSource,
                    "durationMs" to capture.durationMs,
                    "peakDbfs" to capture.peakDbfs,
                    "rmsDbfs" to capture.rmsDbfs,
                    "noiseFloorDbfs" to capture.noiseFloorDbfs,
                    "clipped" to capture.clipped,
                    "quality" to capture.quality,
                    "trigger" to capture.trigger,
                    "operatorId" to session.operatorId,
                    "createdAt" to capture.createdAt,
                    // Ham JSON da saklanır: şema büyüdüğünde eski kayıtlar
                    // yeniden işlenebilsin diye.
                    "metadataJson" to metadataJson,
                )
            )
            .await()
    }

    /** `raw/22k/ceyrek/<sessionId>/kayit-001.wav` */
    private fun storagePath(session: SessionEntity, capture: CaptureEntity): String =
        "raw/${session.materialSlug}/${session.objectTypeSlug}/${session.id}/" +
            File(capture.wavPath).name

    /** Storage kuralları kimlik doğrulaması ister; saha aracı anonim oturum açar. */
    private suspend fun ensureSignedIn() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    private companion object {
        const val COLLECTION_CAPTURES = "captures"
    }
}
