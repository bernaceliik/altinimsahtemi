package com.bernacelik.altinimsahtemi.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class AudioMetricsTest {

    @Test
    fun `tam olcek 0 dBFS yarim olcek yaklasik -6 dBFS verir`() {
        assertEquals(0f, AudioMetrics.toDbfs(1f), 0.001f)
        assertEquals(-6.0206f, AudioMetrics.toDbfs(0.5f), 0.001f)
        assertEquals(-20f, AudioMetrics.toDbfs(0.1f), 0.001f)
    }

    @Test
    fun `sifir genlik sessizlik tabanina sabitlenir`() {
        assertEquals(AudioMetrics.SILENCE_DBFS, AudioMetrics.toDbfs(0f), 0f)
        assertEquals(AudioMetrics.SILENCE_DBFS, AudioMetrics.toDbfs(-1f), 0f)
    }

    @Test
    fun `dBFS donusumu ileri geri tutarli`() {
        for (db in listOf(-3f, -12f, -45f, -80f)) {
            assertEquals(db, AudioMetrics.toDbfs(AudioMetrics.fromDbfs(db)), 0.01f)
        }
    }

    @Test
    fun `sinus dalgasinin RMS degeri genligin karekok iki boleni`() {
        val amplitude = 0.8f
        val samples = FloatArray(4800) { sin(2 * PI * 100 * it / 48_000.0).toFloat() * amplitude }

        assertEquals(amplitude, AudioMetrics.peak(samples), 0.001f)
        assertEquals(amplitude / sqrt(2f), AudioMetrics.rms(samples), 0.005f)
    }

    @Test
    fun `count parametresi yalnizca dizinin bas kismini olcer`() {
        val samples = floatArrayOf(0.1f, 0.2f, 0.9f, 1.0f)

        assertEquals(0.2f, AudioMetrics.peak(samples, count = 2), 0.001f)
        assertEquals(1.0f, AudioMetrics.peak(samples), 0.001f)
    }

    @Test
    fun `kirpilma esigi tepe degerine gore belirlenir`() {
        assertTrue(AudioMetrics.isClipped(floatArrayOf(0.5f, -0.995f)))
        assertFalse(AudioMetrics.isClipped(floatArrayOf(0.5f, -0.98f)))
    }
}
