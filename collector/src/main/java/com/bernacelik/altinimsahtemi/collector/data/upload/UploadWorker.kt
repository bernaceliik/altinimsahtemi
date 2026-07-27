package com.bernacelik.altinimsahtemi.collector.data.upload

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bernacelik.altinimsahtemi.collector.CollectorApp
import com.bernacelik.altinimsahtemi.collector.data.db.UploadState
import java.util.concurrent.TimeUnit

/**
 * Bekleyen kayıtları sırayla buluta taşır.
 *
 * Yükleme, kayıt akışından tamamen ayrıdır: saha ekibi çevrimdışı çalışabilsin
 * ve mikrofon işi hiçbir zaman ağ beklemesin diye. Bir kayıt kalıcı olarak
 * başarısız olsa bile diskte durur; hiçbir veri sessizce kaybolmaz.
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CollectorApp).container
        val uploader = container.uploader

        if (!uploader.isConfigured) {
            Log.i(TAG, "Firebase yapılandırılmadı; kuyruk olduğu gibi bekliyor")
            return Result.success()
        }

        val repository = container.repository
        val pending = repository.pendingUploads()
        if (pending.isEmpty()) return Result.success()

        var failed = 0
        for (capture in pending) {
            val session = repository.session(capture.sessionId)
            if (session == null) {
                repository.setUploadState(capture.id, UploadState.FAILED, "Oturum kaydı bulunamadı")
                failed++
                continue
            }
            repository.setUploadState(capture.id, UploadState.UPLOADING)
            runCatching { uploader.upload(session, capture) }
                .onSuccess { repository.setUploadState(capture.id, UploadState.UPLOADED) }
                .onFailure { error ->
                    failed++
                    Log.w(TAG, "Yükleme başarısız: ${capture.id}", error)
                    repository.setUploadState(
                        capture.id,
                        UploadState.FAILED,
                        error.message ?: error::class.java.simpleName,
                    )
                }
        }

        // Kalan varsa WorkManager üstel geri çekilmeyle tekrar deneyecek.
        return if (failed > 0) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "UploadWorker"
        private const val WORK_NAME = "capture-upload"

        /** Kuyruğu tetikler. Aynı isimli iş zaten varsa yenisi eklenmez. */
        fun enqueue(context: Context, requireUnmetered: Boolean = true) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (requireUnmetered) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .build()

            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        /** Kullanıcı "şimdi dene" dediğinde kısıtları gevşetip kuyruğu yeniden başlatır. */
        fun retryNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
