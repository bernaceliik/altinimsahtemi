package com.bernacelik.altinimsahtemi.audio

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

enum class TriggerType(val wireName: String) {
    AUTO_ONSET("auto_onset"),
    MANUAL("manual"),
}

/** Yakalanmış tek bir darbe: ham örnekler ve o ana ait ölçümler. */
class CapturedImpact(
    val samples: FloatArray,
    val profile: AudioProfile,
    val trigger: TriggerType,
    val peakDbfs: Float,
    val rmsDbfs: Float,
    val noiseFloorDbfs: Float,
    val clipped: Boolean,
    val preRollFrames: Int,
    val capturedAtMillis: Long,
) {
    val durationMs: Int get() = profile.framesToMs(samples.size)

    /**
     * Kayıt kabul edilebilir mi. Kırpılmış sinyalin tepe bilgisi kaybolur;
     * gürültü tabanına fazla yakın sinyalde ise sönümlenme eğrisi okunamaz.
     */
    fun quality(): CaptureQuality = when {
        clipped -> CaptureQuality.CLIPPED
        peakDbfs < noiseFloorDbfs + MIN_SNR_DB -> CaptureQuality.TOO_QUIET
        else -> CaptureQuality.CLEAN
    }

    private companion object {
        const val MIN_SNR_DB = 24f
    }
}

enum class CaptureQuality { CLEAN, CLIPPED, TOO_QUIET }

sealed interface CaptureEvent {
    /** Canlı seviye göstergesi için, blok başına yayılır. */
    data class Level(
        val peakDbfs: Float,
        val rmsDbfs: Float,
        val noiseFloorDbfs: Float,
        val thresholdDbfs: Float,
    ) : CaptureEvent

    data class Impact(val capture: CapturedImpact) : CaptureEvent
}

/**
 * Kayıt motoru: [RawAudioRecorder] + [RingBuffer] + [ImpactDetector] birleşimi.
 *
 * Sürekli dinler, canlı seviye yayar ve bir darbe tespit edildiğinde
 * (pre-roll dahil) sabit uzunlukta bir pencereyi toplayıp [CaptureEvent.Impact]
 * olarak verir. Otomatik tetikleme kapatılabilir; manuel yakalama her zaman
 * yedek olarak durur.
 */
class CaptureEngine(
    private val profile: AudioProfile,
    private val config: CaptureConfig = CaptureConfig(),
    private val recorder: RawAudioRecorder = RawAudioRecorder(profile),
) {
    private val autoTriggerEnabled = AtomicBoolean(true)
    private val manualRequested = AtomicBoolean(false)

    val effectState: EffectState get() = recorder.effectState

    fun setAutoTriggerEnabled(enabled: Boolean) = autoTriggerEnabled.set(enabled)

    /** Bir sonraki bloktan itibaren manuel bir yakalama başlatır. */
    fun requestManualCapture() = manualRequested.set(true)

    /** Toplama durdurulana kadar süren olay akışı. */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun events(): Flow<CaptureEvent> = flow {
        val detector = ImpactDetector(profile.sampleRate, config)
        val preRollFrames = profile.msToFrames(config.preRollMs)
        val captureFrames = profile.msToFrames(config.captureMs)
        val ring = RingBuffer(maxOf(preRollFrames, 1))
        val pending = PendingCapture(captureFrames + preRollFrames)

        recorder.audioStream().collect { block ->
            var offset = 0

            // 1) Devam eden bir yakalama varsa önce onu doldur.
            if (pending.active) {
                offset = pending.fill(block.samples, 0, block.frameCount)
                if (pending.isComplete) {
                    emit(CaptureEvent.Impact(pending.finish(profile, detector.noiseFloorDbfs)))
                    ring.clear()
                }
            }

            // 2) Kalan örnekler: seviye ölçümü, tetikleme kontrolü, pre-roll besleme.
            if (offset < block.frameCount) {
                val remaining = block.frameCount - offset
                val tail = if (offset == 0) block.samples else block.samples.copyOfRange(offset, block.frameCount)

                emit(
                    CaptureEvent.Level(
                        peakDbfs = AudioMetrics.peakDbfs(tail, remaining),
                        rmsDbfs = AudioMetrics.rmsDbfs(tail, remaining),
                        noiseFloorDbfs = detector.noiseFloorDbfs,
                        thresholdDbfs = detector.thresholdDbfs(),
                    )
                )

                val manual = manualRequested.getAndSet(false)
                val onset = if (autoTriggerEnabled.get() && !pending.active) {
                    detector.process(tail, remaining)
                } else {
                    -1
                }

                when {
                    manual && !pending.active -> {
                        pending.begin(TriggerType.MANUAL, ring.snapshot())
                        pending.fill(tail, 0, remaining)
                    }
                    onset >= 0 -> {
                        // Pre-roll: halka tamponundaki geçmiş + bu blokta darbeden öncesi.
                        pending.begin(TriggerType.AUTO_ONSET, ring.snapshot())
                        pending.fill(tail, 0, remaining)
                    }
                    else -> ring.write(tail, remaining)
                }

                if (pending.isComplete) {
                    emit(CaptureEvent.Impact(pending.finish(profile, detector.noiseFloorDbfs)))
                    ring.clear()
                }
            }
        }
    }

    /** Bloklar arasında birikmekte olan yakalamayı tutan yardımcı. */
    private class PendingCapture(private val maxFrames: Int) {
        private var buffer = FloatArray(0)
        private var filled = 0
        private var trigger = TriggerType.AUTO_ONSET
        private var preRoll = 0

        var active = false
            private set

        val isComplete: Boolean get() = active && filled >= buffer.size

        fun begin(trigger: TriggerType, preRollSamples: FloatArray) {
            this.trigger = trigger
            this.preRoll = minOf(preRollSamples.size, maxFrames)
            buffer = FloatArray(maxFrames)
            System.arraycopy(preRollSamples, preRollSamples.size - preRoll, buffer, 0, preRoll)
            filled = preRoll
            active = true
        }

        /** @return kaynaktan tüketilen örnek sayısı. */
        fun fill(source: FloatArray, from: Int, count: Int): Int {
            val take = minOf(buffer.size - filled, count - from)
            if (take <= 0) return from
            System.arraycopy(source, from, buffer, filled, take)
            filled += take
            return from + take
        }

        fun finish(profile: AudioProfile, noiseFloorDbfs: Float): CapturedImpact {
            val samples = if (filled == buffer.size) buffer else buffer.copyOf(filled)
            active = false
            filled = 0
            return CapturedImpact(
                samples = samples,
                profile = profile,
                trigger = trigger,
                peakDbfs = AudioMetrics.peakDbfs(samples),
                rmsDbfs = AudioMetrics.rmsDbfs(samples),
                noiseFloorDbfs = noiseFloorDbfs,
                clipped = AudioMetrics.isClipped(samples),
                preRollFrames = preRoll,
                capturedAtMillis = System.currentTimeMillis(),
            )
        }
    }
}
