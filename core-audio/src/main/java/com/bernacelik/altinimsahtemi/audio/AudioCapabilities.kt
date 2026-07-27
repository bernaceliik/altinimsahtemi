package com.bernacelik.altinimsahtemi.audio

import android.Manifest
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 * Cihazın gerçek kayıt yeteneklerini tespit eder.
 *
 * Buradaki temel ilke: **hiçbir değeri varsaymayız**. Bir örnekleme oranının
 * desteklendiğini ancak o oranla bir [AudioRecord] kurup `STATE_INITIALIZED`
 * aldıktan ve dönen `sampleRate` değerini doğruladıktan sonra kabul ederiz.
 *
 * Bu titizliğin sebebi: Android'de dahili MEMS mikrofonun donanım tavanı
 * pratikte 48 kHz'dir. 96/192 kHz talebi çoğu cihazda ya reddedilir ya da
 * sessizce yeniden örneklenmiş veri döner — ikincisi veri setini fark
 * ettirmeden bozar.
 */
object AudioCapabilities {

    private const val TAG = "AudioCapabilities"

    /** Yüksekten alçağa denenecek örnekleme oranları. */
    val CANDIDATE_SAMPLE_RATES = intArrayOf(192_000, 96_000, 48_000, 44_100)

    /** Platform, işlenmemiş ses kaynağını desteklediğini bildiriyor mu. */
    fun supportsUnprocessed(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true"
    }

    /**
     * Dahili mikrofonun ilan ettiği örnekleme oranları. Boş dizi dönerse cihaz
     * bu bilgiyi vermiyor demektir (yoklamaya düşülür), yokluk desteksizlik değildir.
     */
    fun advertisedSampleRates(context: Context): IntArray {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val builtInMic = am.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC }
        return builtInMic?.sampleRates ?: IntArray(0)
    }

    /**
     * Cihazın verebileceği en iyi profili bulur: önce en yüksek örnekleme oranı,
     * eşitlikte float kodlama tercih edilir.
     *
     * RECORD_AUDIO izni verilmeden çağrılamaz — AudioRecord kurulumu izne bağlıdır.
     *
     * @return doğrulanmış profil, ya da hiçbir yapılandırma çalışmazsa null.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun detectBestProfile(context: Context): AudioProfile? {
        val unprocessed = supportsUnprocessed(context)
        val advertised = advertisedSampleRates(context).toSet()

        // İlan edilen oranlar varsa adayları onlarla kesiştir; kesişim boşsa
        // ilan bilgisine güvenmeyip tam listeyi dene.
        val rates = CANDIDATE_SAMPLE_RATES
            .filter { advertised.isEmpty() || it in advertised }
            .ifEmpty { CANDIDATE_SAMPLE_RATES.toList() }

        val sources = buildList {
            if (unprocessed) add(AudioSourceKind.UNPROCESSED)
            add(AudioSourceKind.VOICE_RECOGNITION)
            add(AudioSourceKind.MIC)
        }

        for (source in sources) {
            for (rate in rates) {
                for (encoding in listOf(PcmEncoding.FLOAT_32, PcmEncoding.PCM_16)) {
                    val profile = probe(source, rate, encoding, unprocessed)
                    if (profile != null) {
                        Log.i(TAG, "Doğrulanmış profil: $profile (ilan edilen: ${advertised.sorted()})")
                        return profile
                    }
                }
            }
        }
        Log.e(TAG, "Hiçbir kayıt yapılandırması doğrulanamadı")
        return null
    }

    /**
     * Tek bir yapılandırmayı fiilen kurup doğrular. Kurulum başarısızsa veya
     * cihaz istenenden farklı bir örnekleme oranı dönerse null verir.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun probe(
        source: AudioSourceKind,
        sampleRate: Int,
        encoding: PcmEncoding,
        unprocessedSupported: Boolean,
    ): AudioProfile? {
        val androidEncoding = encoding.toAudioFormat()
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            androidEncoding,
        )
        if (minBuffer <= 0) return null

        var record: AudioRecord? = null
        return try {
            record = AudioRecord(
                source.androidValue,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                androidEncoding,
                minBuffer * 2,
            )
            when {
                record.state != AudioRecord.STATE_INITIALIZED -> null
                // Cihaz sessizce başka bir orana düşürdüyse bu profili reddet.
                record.sampleRate != sampleRate -> {
                    Log.w(TAG, "$sampleRate Hz istendi, ${record.sampleRate} Hz döndü — reddedildi")
                    null
                }
                else -> AudioProfile(
                    sampleRate = sampleRate,
                    encoding = encoding,
                    source = source,
                    channelCount = 1,
                    minBufferSizeBytes = minBuffer,
                    unprocessedSupported = unprocessedSupported,
                )
            }
        } catch (t: Throwable) {
            // Desteklenmeyen kombinasyonlarda bazı cihazlar exception atar.
            null
        } finally {
            record?.release()
        }
    }
}
