package com.bernacelik.altinimsahtemi.audio

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/** Seviye ölçümleri. Android bağımlılığı yok — doğrudan JVM'de test edilebilir. */
object AudioMetrics {

    /** Bu değere dayanan örnekler kırpılmış (clipped) sayılır. */
    const val CLIP_THRESHOLD = 0.99f

    /** dBFS ölçeğinin alt sınırı; log10(0) yerine bu döner. */
    const val SILENCE_DBFS = -120f

    fun peak(samples: FloatArray, count: Int = samples.size): Float {
        var max = 0f
        for (i in 0 until count) {
            val v = abs(samples[i])
            if (v > max) max = v
        }
        return max
    }

    fun rms(samples: FloatArray, count: Int = samples.size): Float {
        if (count <= 0) return 0f
        var sum = 0.0
        for (i in 0 until count) {
            val v = samples[i].toDouble()
            sum += v * v
        }
        return sqrt(sum / count).toFloat()
    }

    /** Doğrusal genliği (0..1) dBFS'e çevirir. */
    fun toDbfs(amplitude: Float): Float {
        if (amplitude <= 0f) return SILENCE_DBFS
        val db = 20f * log10(amplitude)
        return if (db < SILENCE_DBFS) SILENCE_DBFS else db
    }

    /** dBFS değerini doğrusal genliğe geri çevirir. */
    fun fromDbfs(dbfs: Float): Float =
        if (dbfs <= SILENCE_DBFS) 0f else Math.pow(10.0, (dbfs / 20f).toDouble()).toFloat()

    fun peakDbfs(samples: FloatArray, count: Int = samples.size): Float = toDbfs(peak(samples, count))

    fun rmsDbfs(samples: FloatArray, count: Int = samples.size): Float = toDbfs(rms(samples, count))

    fun isClipped(samples: FloatArray, count: Int = samples.size): Boolean =
        peak(samples, count) >= CLIP_THRESHOLD
}
