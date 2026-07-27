package com.bernacelik.altinimsahtemi.audio

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Darbe yakalama parametreleri.
 *
 * Varsayılanlar, 15 cm mesafeden mermer/cam zemine metal darbesi senaryosuna göre
 * seçilmiştir; laboratuvar düzeneği değişirse kalibrasyon ekranından ayarlanabilir.
 */
data class CaptureConfig(
    /** Tetiklemeden önce saklanacak süre — atağın ilk anları kesilmesin diye. */
    val preRollMs: Int = 100,
    /** Tetiklemeden itibaren kaydedilecek toplam süre (sönümlenme buraya sığmalı). */
    val captureMs: Int = 1500,
    /** Gürültü tabanının kaç dB üstünde tetikleneceği. */
    val triggerThresholdDb: Float = 18f,
    /**
     * Mutlak alt sınır. Çok sessiz odalarda gürültü tabanı -85 dBFS'e inebilir ve
     * +18 dB eşik bir hışırtıyla tetiklenebilir; gerçek bir darbe -20 dBFS
     * civarında olduğu için bu sınır yanlış tetiklemeleri eler.
     */
    val minTriggerDbfs: Float = -45f,
    /** Aynı vuruşun çınlaması ikinci bir darbe sayılmasın diye ölü süre. */
    val refractoryMs: Int = 400,
    /** Gürültü tabanı üstel hareketli ortalamasının öğrenme hızı. */
    val noiseFloorAlpha: Float = 0.05f,
    /** Tepe taraması için alt pencere uzunluğu (örnek). */
    val analysisWindow: Int = 128,
) {
    init {
        require(preRollMs >= 0 && captureMs > 0) { "Süreler geçersiz" }
        require(noiseFloorAlpha > 0f && noiseFloorAlpha <= 1f) { "alpha 0..1 aralığında olmalı" }
    }
}

/**
 * Uyarlanabilir eşikli darbe (onset) dedektörü.
 *
 * Boştayken ortam gürültüsünü üstel hareketli ortalamayla izler; tepe değeri
 * eşiği aştığında tetiklenir ve refrakter süre boyunca — vuruşun kendi
 * çınlaması yeni darbe sayılmasın diye — sessize alınır.
 *
 * Android bağımlılığı yoktur, sentetik sinyallerle JVM'de test edilir.
 */
class ImpactDetector(
    private val sampleRate: Int,
    private val config: CaptureConfig = CaptureConfig(),
) {
    /** Öğrenilmiş ortam gürültü tabanı. */
    var noiseFloorDbfs: Float = -80f
        private set

    private val refractorySamples = (sampleRate.toLong() * config.refractoryMs / 1000L).toInt()
    private var refractoryRemaining = 0

    /** Kalibrasyon ekranında ölçülen gürültü tabanını başlangıç değeri olarak alır. */
    fun calibrate(measuredNoiseFloorDbfs: Float) {
        noiseFloorDbfs = measuredNoiseFloorDbfs
        refractoryRemaining = 0
    }

    fun reset() {
        refractoryRemaining = 0
    }

    /** Şu anki tetikleme eşiği. */
    fun thresholdDbfs(): Float =
        max(noiseFloorDbfs + config.triggerThresholdDb, config.minTriggerDbfs)

    /**
     * Bir bloğu işler.
     *
     * @return darbenin blok içindeki örnek indeksi, tetiklenme yoksa -1.
     */
    fun process(block: FloatArray, count: Int = block.size): Int {
        val thresholdLinear = AudioMetrics.fromDbfs(thresholdDbfs())
        var i = 0
        while (i < count) {
            val end = min(i + config.analysisWindow, count)
            val windowLength = end - i

            if (refractoryRemaining > 0) {
                // Ölü süre: ne tetikleme ne gürültü tabanı güncellemesi — çınlama
                // tabanı yukarı çekip sonraki darbeleri kaçırmamalı.
                refractoryRemaining -= windowLength
                i = end
                continue
            }

            var peak = 0f
            for (j in i until end) {
                val v = abs(block[j])
                if (v > peak) peak = v
            }

            if (peak >= thresholdLinear) {
                var onset = i
                while (onset < end && abs(block[onset]) < thresholdLinear) onset++
                refractoryRemaining = refractorySamples
                return onset
            }

            var sum = 0.0
            for (j in i until end) {
                val v = block[j].toDouble()
                sum += v * v
            }
            val windowDbfs = AudioMetrics.toDbfs(kotlin.math.sqrt(sum / windowLength).toFloat())
            noiseFloorDbfs += config.noiseFloorAlpha * (windowDbfs - noiseFloorDbfs)

            i = end
        }
        return -1
    }
}
