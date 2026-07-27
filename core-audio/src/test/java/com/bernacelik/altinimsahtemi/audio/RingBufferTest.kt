package com.bernacelik.altinimsahtemi.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class RingBufferTest {

    @Test
    fun `dolmamis tampon yazilan sirayi korur`() {
        val ring = RingBuffer(4)
        ring.write(floatArrayOf(1f, 2f))

        assertEquals(2, ring.size)
        assertArrayEquals(floatArrayOf(1f, 2f), ring.snapshot(), 0f)
    }

    @Test
    fun `sarma sonrasi en eski ornekler dusurulur ve sira bozulmaz`() {
        val ring = RingBuffer(4)
        ring.write(floatArrayOf(1f, 2f, 3f))
        ring.write(floatArrayOf(4f, 5f))

        assertEquals(4, ring.size)
        assertArrayEquals(floatArrayOf(2f, 3f, 4f, 5f), ring.snapshot(), 0f)
    }

    @Test
    fun `kapasiteden uzun yazimda yalnizca son ornekler kalir`() {
        val ring = RingBuffer(3)
        ring.write(floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f))

        assertEquals(3, ring.size)
        assertArrayEquals(floatArrayOf(5f, 6f, 7f), ring.snapshot(), 0f)
    }

    @Test
    fun `count parametresi dizinin yalnizca bas kismini yazar`() {
        val ring = RingBuffer(8)
        ring.write(floatArrayOf(1f, 2f, 3f, 4f), count = 2)

        assertArrayEquals(floatArrayOf(1f, 2f), ring.snapshot(), 0f)
    }

    @Test
    fun `clear sonrasi tampon bos baslar`() {
        val ring = RingBuffer(4)
        ring.write(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        ring.clear()

        assertEquals(0, ring.size)
        assertArrayEquals(FloatArray(0), ring.snapshot(), 0f)

        ring.write(floatArrayOf(9f))
        assertArrayEquals(floatArrayOf(9f), ring.snapshot(), 0f)
    }
}
