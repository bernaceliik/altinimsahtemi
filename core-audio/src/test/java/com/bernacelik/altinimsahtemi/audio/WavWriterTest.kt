package com.bernacelik.altinimsahtemi.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * WAV başlığının spesifikasyona birebir uyduğunu doğrular. Bu dosyalar Python
 * tarafında (soundfile/librosa) okunacağı için başlıktaki tek baytlık sapma bile
 * tüm veri setini kullanılamaz hâle getirir.
 */
class WavWriterTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun ByteArray.ascii(offset: Int, length: Int = 4) =
        String(this, offset, length, Charsets.US_ASCII)

    private fun ByteArray.int(offset: Int) =
        ByteBuffer.wrap(this, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int

    private fun ByteArray.short(offset: Int) =
        ByteBuffer.wrap(this, offset, 2).order(ByteOrder.LITTLE_ENDIAN).short

    @Test
    fun `16-bit PCM basligi klasik 44 baytlik yapiyi uretir`() {
        val dataSize = 1000 * 2
        val header = WavWriter.header(PcmEncoding.PCM_16, 48_000, 1, dataSize)

        assertEquals(44, header.size)
        assertEquals("RIFF", header.ascii(0))
        assertEquals(36 + dataSize, header.int(4))
        assertEquals("WAVE", header.ascii(8))
        assertEquals("fmt ", header.ascii(12))
        assertEquals(16, header.int(16))
        assertEquals(1.toShort(), header.short(20))       // WAVE_FORMAT_PCM
        assertEquals(1.toShort(), header.short(22))       // kanal
        assertEquals(48_000, header.int(24))
        assertEquals(48_000 * 2, header.int(28))          // byte rate
        assertEquals(2.toShort(), header.short(32))       // block align
        assertEquals(16.toShort(), header.short(34))      // bit derinliği
        assertEquals("data", header.ascii(36))
        assertEquals(dataSize, header.int(40))
    }

    @Test
    fun `32-bit float basligi IEEE_FLOAT etiketi ve fact chunk icerir`() {
        val frames = 500
        val dataSize = frames * 4
        val header = WavWriter.header(PcmEncoding.FLOAT_32, 96_000, 1, dataSize)

        // 8 (RIFF) + 4 (WAVE) + 26 (fmt) + 12 (fact) + 8 (data başlığı)
        assertEquals(58, header.size)
        assertEquals(50 + dataSize, header.int(4))
        assertEquals("fmt ", header.ascii(12))
        assertEquals(18, header.int(16))                  // PCM dışı formatlar için 18
        assertEquals(3.toShort(), header.short(20))       // WAVE_FORMAT_IEEE_FLOAT
        assertEquals(96_000, header.int(24))
        assertEquals(96_000 * 4, header.int(28))
        assertEquals(4.toShort(), header.short(32))
        assertEquals(32.toShort(), header.short(34))
        assertEquals(0.toShort(), header.short(36))       // cbSize
        assertEquals("fact", header.ascii(38))
        assertEquals(4, header.int(42))
        assertEquals(frames, header.int(46))              // kanal başına örnek sayısı
        assertEquals("data", header.ascii(50))
        assertEquals(dataSize, header.int(54))
    }

    @Test
    fun `float kayit yazilip geri okundugunda ornekler birebir korunur`() {
        val samples = floatArrayOf(0f, 0.5f, -0.5f, 0.999f, -1f, 0.123456f)
        val file = tempFolder.newFile("kayit-001.wav")

        WavWriter.write(file, samples, samples.size, 48_000, PcmEncoding.FLOAT_32)

        val bytes = file.readBytes()
        assertEquals(58 + samples.size * 4, bytes.size)
        assertEquals(58 + samples.size * 4, bytes.int(4) + 8)

        val decoded = FloatArray(samples.size)
        ByteBuffer.wrap(bytes, 58, samples.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asFloatBuffer()
            .get(decoded)
        assertArrayEquals(samples, decoded, 0f)
    }

    @Test
    fun `16-bit yazimda genlik sinirlarinin disi kirpilir`() {
        val samples = floatArrayOf(2f, -2f, 0f)
        val file = tempFolder.newFile("kirpma.wav")

        WavWriter.write(file, samples, samples.size, 44_100, PcmEncoding.PCM_16)

        val bytes = file.readBytes()
        assertEquals(32767.toShort(), bytes.short(44))
        assertEquals((-32767).toShort(), bytes.short(46))
        assertEquals(0.toShort(), bytes.short(48))
    }
}
