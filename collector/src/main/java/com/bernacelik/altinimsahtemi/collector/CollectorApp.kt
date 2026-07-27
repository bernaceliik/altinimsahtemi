package com.bernacelik.altinimsahtemi.collector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.bernacelik.altinimsahtemi.audio.AudioCapabilities
import com.bernacelik.altinimsahtemi.audio.AudioProfile
import com.bernacelik.altinimsahtemi.audio.CaptureConfig
import com.bernacelik.altinimsahtemi.collector.data.CaptureRepository
import com.bernacelik.altinimsahtemi.collector.data.db.CollectorDatabase
import com.bernacelik.altinimsahtemi.collector.data.files.CaptureFileStore
import com.bernacelik.altinimsahtemi.collector.data.prefs.OperatorStore
import com.bernacelik.altinimsahtemi.collector.data.upload.CaptureUploader

/**
 * Uygulama genelindeki bağımlılıklar.
 *
 * Saha aracı için tam bir DI çerçevesi gereksiz karmaşıklık; tek bir konteyner
 * hem yeterli hem okunabilir.
 */
class AppContainer(private val context: Context) {

    val appContext: Context get() = context

    val captureConfig = CaptureConfig()

    private val database by lazy { CollectorDatabase.create(context) }
    val fileStore by lazy { CaptureFileStore(context) }
    val operatorStore by lazy { OperatorStore(context) }

    val repository by lazy {
        CaptureRepository(database.sessionDao(), database.captureDao(), fileStore)
    }

    val uploader: CaptureUploader by lazy { CaptureUploader.create(context) }

    /**
     * Cihazda doğrulanmış kayıt profili. Tespit maliyetli olduğu (birden çok
     * AudioRecord kurulumu) ve oturum boyunca sabit kalması gerektiği için
     * bir kez hesaplanıp saklanır.
     */
    @Volatile
    var audioProfile: AudioProfile? = null
        private set

    fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /** İzin yoksa null döner — çağıranın ayrıca kontrol etmesine gerek kalmaz. */
    @SuppressLint("MissingPermission")
    fun ensureAudioProfile(): AudioProfile? {
        if (!hasRecordPermission()) return null
        audioProfile?.let { return it }
        return AudioCapabilities.detectBestProfile(context)?.also { audioProfile = it }
    }
}

class CollectorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
