package com.bernacelik.altinimsahtemi.audio

import android.media.AudioFormat
import android.media.MediaRecorder

/**
 * Ham PCM kodlaması. Android sabitlerinden bağımsız tutulur ki WAV yazıcı ve
 * ölçüm kodu JVM üzerinde (cihazsız) test edilebilsin.
 */
enum class PcmEncoding(val bitsPerSample: Int, val wireName: String) {
    /** 32-bit IEEE float. Kırpılma analizi ve normalizasyon için tercih edilen format. */
    FLOAT_32(32, "pcm_float"),

    /** 16-bit işaretli tamsayı. FLOAT_32 desteklenmeyen cihazlarda yedek. */
    PCM_16(16, "pcm_s16le");

    fun toAudioFormat(): Int = when (this) {
        FLOAT_32 -> AudioFormat.ENCODING_PCM_FLOAT
        PCM_16 -> AudioFormat.ENCODING_PCM_16BIT
    }
}

/**
 * Mikrofon giriş kaynağı. Sıralama tercih önceliğini yansıtır.
 *
 * [UNPROCESSED] kritik: diğer kaynaklarda platform otomatik kazanç ve gürültü
 * engelleme uygular, bu da ölçmek istediğimiz sönümlenme eğrisini bozar.
 */
enum class AudioSourceKind(val androidValue: Int) {
    UNPROCESSED(MediaRecorder.AudioSource.UNPROCESSED),
    VOICE_RECOGNITION(MediaRecorder.AudioSource.VOICE_RECOGNITION),
    MIC(MediaRecorder.AudioSource.MIC),
}

/**
 * Cihazda gerçekten doğrulanmış kayıt yapılandırması.
 *
 * Bu nesne [AudioCapabilities.detectBestProfile] tarafından, istenen değerler
 * kabul edilerek değil **AudioRecord fiilen kurulup doğrulanarak** üretilir.
 */
data class AudioProfile(
    val sampleRate: Int,
    val encoding: PcmEncoding,
    val source: AudioSourceKind,
    val channelCount: Int = 1,
    /** AudioRecord.getMinBufferSize sonucu (bayt). */
    val minBufferSizeBytes: Int,
    /** Platform UNPROCESSED kaynağını desteklediğini bildiriyor mu. */
    val unprocessedSupported: Boolean,
) {
    val bitsPerSample: Int get() = encoding.bitsPerSample

    /** Verilen milisaniyenin kaç örneğe denk geldiği (tek kanal). */
    fun msToFrames(ms: Int): Int = (sampleRate.toLong() * ms / 1000L).toInt()

    fun framesToMs(frames: Int): Int = (frames.toLong() * 1000L / sampleRate).toInt()
}
