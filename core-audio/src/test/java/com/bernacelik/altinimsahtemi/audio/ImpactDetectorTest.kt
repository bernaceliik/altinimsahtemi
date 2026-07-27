package com.bernacelik.altinimsahtemi.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Dedektör, sentetik "sessiz oda + metal darbe" sinyalleriyle sınanır.
 * Gerçek cihaz gerekmez; bu sayede tetikleme mantığındaki regresyonlar sahaya
 * çıkmadan yakalanır.
 */
class ImpactDetectorTest {

    private val sampleRate = 48_000
    private val blockSize = 960 // 20 ms

    /** -80 dBFS civarı sabit ortam gürültüsü. */
    private fun noise(length: Int, seed: Int = 42): FloatArray {
        val random = Random(seed)
        return FloatArray(length) { (random.nextFloat() * 2f - 1f) * 0.0001f }
    }

    /** Verilen indekse sönümlenen bir darbe ekler (metal tınısı benzetimi). */
    private fun FloatArray.addImpact(at: Int, amplitude: Float = 0.5f, decayMs: Int = 80) {
        val length = sampleRate * decayMs / 1000
        for (i in 0 until length) {
            val index = at + i
            if (index >= size) break
            val envelope = exp(-5.0 * i / length).toFloat()
            this[index] += amplitude * envelope * sin(2 * PI * 3200 * i / sampleRate).toFloat()
        }
    }

    /** Sinyali bloklar hâlinde dedektöre verir, tetikleme anlarını örnek cinsinden döner. */
    private fun runDetector(signal: FloatArray, detector: ImpactDetector): List<Int> {
        val triggers = mutableListOf<Int>()
        var offset = 0
        while (offset < signal.size) {
            val count = minOf(blockSize, signal.size - offset)
            val block = signal.copyOfRange(offset, offset + count)
            val onset = detector.process(block, count)
            if (onset >= 0) triggers += offset + onset
            offset += count
        }
        return triggers
    }

    @Test
    fun `sessiz ortamda tetikleme olmaz ve gurultu tabani ogrenilir`() {
        val detector = ImpactDetector(sampleRate, CaptureConfig())
        val signal = noise(sampleRate) // 1 saniye

        val triggers = runDetector(signal, detector)

        assertEquals(emptyList<Int>(), triggers)
        // -80 dBFS civarı bir tabana yakınsamalı
        assertTrue(
            "Öğrenilen taban: ${detector.noiseFloorDbfs}",
            detector.noiseFloorDbfs in -95f..-70f,
        )
    }

    @Test
    fun `tek darbe tam olarak bir kez tetikler`() {
        val detector = ImpactDetector(sampleRate, CaptureConfig())
        val signal = noise(sampleRate).apply { addImpact(at = sampleRate / 5) }

        val triggers = runDetector(signal, detector)

        assertEquals(1, triggers.size)
        // Onset, darbenin başladığı yerin birkaç milisaniye yakınında olmalı
        assertTrue(
            "Onset ${triggers[0]}, beklenen ~${sampleRate / 5}",
            triggers[0] in (sampleRate / 5 - 100)..(sampleRate / 5 + 480),
        )
    }

    @Test
    fun `refrakter sure icindeki ikinci darbe yok sayilir sonraki yakalanir`() {
        val config = CaptureConfig(refractoryMs = 400)
        val detector = ImpactDetector(sampleRate, config)

        val signal = noise(sampleRate * 2).apply {
            addImpact(at = (0.2 * sampleRate).toInt())  // tetikler
            addImpact(at = (0.4 * sampleRate).toInt())  // ölü süre içinde — yok sayılır
            addImpact(at = (1.0 * sampleRate).toInt())  // ölü süre bitti — tetikler
        }

        val triggers = runDetector(signal, detector)

        assertEquals("Tetiklemeler: $triggers", 2, triggers.size)
        assertTrue(triggers[0] < (0.3 * sampleRate).toInt())
        assertTrue(triggers[1] > (0.9 * sampleRate).toInt())
    }

    @Test
    fun `esik mutlak alt sinirin altina inemez`() {
        // Yankısız odada taban çok düşer; +18 dB'lik bağıl eşik hışırtıyla
        // tetiklenmesin diye minTriggerDbfs devreye girmeli.
        val config = CaptureConfig(minTriggerDbfs = -45f, triggerThresholdDb = 18f)
        val detector = ImpactDetector(sampleRate, config)
        detector.calibrate(-95f)

        assertEquals(-45f, detector.thresholdDbfs(), 0.001f)
    }

    @Test
    fun `gurultulu ortamda esik gurultu tabaniyla birlikte yukselir`() {
        val config = CaptureConfig(minTriggerDbfs = -45f, triggerThresholdDb = 18f)
        val detector = ImpactDetector(sampleRate, config)
        detector.calibrate(-20f)

        assertEquals(-2f, detector.thresholdDbfs(), 0.001f)
    }
}
