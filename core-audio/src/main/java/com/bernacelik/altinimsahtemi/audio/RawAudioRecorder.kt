package com.bernacelik.altinimsahtemi.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AudioEffect
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.atomic.AtomicBoolean

/** Mikrofondan okunan tek bir ham blok. [samples] yalnızca ilk [frameCount] öğe için geçerlidir. */
class AudioBlock(
    val samples: FloatArray,
    val frameCount: Int,
    val elapsedNs: Long,
)

/** Hangi DSP efektlerinin kapatılabildiğinin kaydı — metadata'ya yazılır. */
data class EffectState(
    val agcDisabled: Boolean,
    val nsDisabled: Boolean,
    val aecDisabled: Boolean,
)

/**
 * [AudioRecord] sarmalayıcısı: platformun ses işleme zincirini kapatıp ham
 * float örnekleri akış olarak yayar.
 *
 * Otomatik kazanç ve gürültü engelleme, ölçmek istediğimiz sönümlenme eğrisini
 * yok ettiği için burada açıkça devre dışı bırakılır; başarılıp başarılmadığı
 * [EffectState] üzerinden raporlanır ve her kaydın metadata'sına işlenir.
 */
class RawAudioRecorder(private val profile: AudioProfile) {

    @Volatile
    var effectState: EffectState = EffectState(false, false, false)
        private set

    /**
     * Kayda başlar ve blokları yayar. Akış toplanmayı bıraktığında kayıt durur
     * ve tüm kaynaklar serbest bırakılır.
     *
     * Blok boyutu, ~20 ms'lik pencerelere denk gelecek şekilde seçilir; bu hem
     * canlı seviye göstergesi hem darbe tespiti için yeterince küçük bir gecikme verir.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun audioStream(): Flow<AudioBlock> = callbackFlow {
        val blockFrames = blockSizeFrames()
        val bufferBytes = maxOf(
            profile.minBufferSizeBytes * 4,
            blockFrames * (profile.bitsPerSample / 8) * 4,
        )

        val record = AudioRecord(
            profile.source.androidValue,
            profile.sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            profile.encoding.toAudioFormat(),
            bufferBytes,
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            close(IllegalStateException("AudioRecord başlatılamadı: profil=$profile"))
            return@callbackFlow
        }

        val effects = disableEffects(record.audioSessionId)
        effectState = effects.state

        val running = AtomicBoolean(true)
        val thread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val floats = FloatArray(blockFrames)
            val shorts = if (profile.encoding == PcmEncoding.PCM_16) ShortArray(blockFrames) else null
            val startNs = System.nanoTime()
            try {
                record.startRecording()
                while (running.get()) {
                    val read = if (shorts != null) {
                        val n = record.read(shorts, 0, blockFrames, AudioRecord.READ_BLOCKING)
                        if (n > 0) for (i in 0 until n) floats[i] = shorts[i] / 32768f
                        n
                    } else {
                        record.read(floats, 0, blockFrames, AudioRecord.READ_BLOCKING)
                    }
                    if (read <= 0) {
                        if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                            close(IllegalStateException("AudioRecord okuma hatası: $read"))
                            break
                        }
                        continue
                    }
                    // Kopya şart: aynı diziyi tekrar kullanırsak tüketici yarış durumuna girer.
                    val block = AudioBlock(floats.copyOf(read), read, System.nanoTime() - startNs)
                    // Bloklar düşürülemez — düşen blok, veri setinde sessiz bir boşluk demektir.
                    trySendBlocking(block)
                }
            } catch (t: Throwable) {
                close(t)
            }
        }, "raw-audio-capture")

        thread.start()

        awaitClose {
            running.set(false)
            thread.join(1_000)
            runCatching { record.stop() }
            record.release()
            effects.release()
        }
    }

    /** ~20 ms'lik blok, 64'ün katına yuvarlanmış. */
    private fun blockSizeFrames(): Int {
        val target = profile.msToFrames(BLOCK_MS)
        return maxOf(256, (target / 64) * 64)
    }

    private class Effects(
        val state: EffectState,
        private val created: List<AudioEffect>,
    ) {
        fun release() = created.forEach { runCatching { it.release() } }
    }

    /**
     * Efektleri kapatmayı dener. Bir efekt cihazda mevcut değilse zaten uygulanmıyor
     * demektir, bu da "kapalı" sayılır.
     */
    private fun disableEffects(sessionId: Int): Effects {
        val created = mutableListOf<AudioEffect>()

        fun <T : AudioEffect> turnOff(available: Boolean, create: () -> T?): Boolean {
            if (!available) return true
            return runCatching {
                val effect = create() ?: return@runCatching false
                created += effect
                effect.enabled = false
                !effect.enabled
            }.getOrElse {
                Log.w(TAG, "Efekt kapatılamadı", it)
                false
            }
        }

        val agc = turnOff(AutomaticGainControl.isAvailable()) { AutomaticGainControl.create(sessionId) }
        val ns = turnOff(NoiseSuppressor.isAvailable()) { NoiseSuppressor.create(sessionId) }
        val aec = turnOff(AcousticEchoCanceler.isAvailable()) { AcousticEchoCanceler.create(sessionId) }

        if (!agc || !ns || !aec) {
            Log.w(TAG, "Sinyal zinciri tam temiz değil: agc=$agc ns=$ns aec=$aec")
        }
        return Effects(EffectState(agc, ns, aec), created)
    }

    private companion object {
        const val TAG = "RawAudioRecorder"
        const val BLOCK_MS = 20
    }
}
