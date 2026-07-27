package com.bernacelik.altinimsahtemi.audio

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * RIFF/WAVE yazıcı.
 *
 * Float kayıtlar `WAVE_FORMAT_IEEE_FLOAT` (0x0003) olarak yazılır; spesifikasyon
 * PCM dışı formatlar için 18 baytlık `fmt ` ve bir `fact` chunk'ı şart koştuğundan
 * bunlar da üretilir. Böylece dosyalar libsndfile/soundfile/librosa tarafında
 * sorunsuz açılır — veri seti Python tarafında işleneceği için bu kritik.
 */
object WavWriter {

    private const val FORMAT_PCM = 1.toShort()
    private const val FORMAT_IEEE_FLOAT = 3.toShort()

    /** PCM için 16, IEEE float için 18 (cbSize alanı dahil). */
    fun fmtChunkSize(encoding: PcmEncoding): Int =
        if (encoding == PcmEncoding.FLOAT_32) 18 else 16

    private fun hasFactChunk(encoding: PcmEncoding): Boolean = encoding == PcmEncoding.FLOAT_32

    /**
     * WAV başlığını üretir. Ayrı bir fonksiyon olması, başlık baytlarının
     * dosya yazmadan doğrulanabilmesi içindir.
     *
     * @param dataSizeBytes `data` chunk'ının bayt cinsinden uzunluğu.
     */
    fun header(
        encoding: PcmEncoding,
        sampleRate: Int,
        channelCount: Int,
        dataSizeBytes: Int,
    ): ByteArray {
        val bitsPerSample = encoding.bitsPerSample
        val bytesPerFrame = channelCount * bitsPerSample / 8
        val fmtSize = fmtChunkSize(encoding)
        val factSize = if (hasFactChunk(encoding)) 8 + 4 else 0

        // "WAVE" (4) + fmt chunk (8 + fmtSize) + fact chunk + data başlığı (8) + veri
        val riffSize = 4 + (8 + fmtSize) + factSize + 8 + dataSizeBytes
        val headerSize = 8 + riffSize - dataSizeBytes

        val buf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray(Charsets.US_ASCII))
        buf.putInt(riffSize)
        buf.put("WAVE".toByteArray(Charsets.US_ASCII))

        buf.put("fmt ".toByteArray(Charsets.US_ASCII))
        buf.putInt(fmtSize)
        buf.putShort(if (encoding == PcmEncoding.FLOAT_32) FORMAT_IEEE_FLOAT else FORMAT_PCM)
        buf.putShort(channelCount.toShort())
        buf.putInt(sampleRate)
        buf.putInt(sampleRate * bytesPerFrame)      // byte rate
        buf.putShort(bytesPerFrame.toShort())        // block align
        buf.putShort(bitsPerSample.toShort())
        if (fmtSize == 18) buf.putShort(0)           // cbSize

        if (hasFactChunk(encoding)) {
            buf.put("fact".toByteArray(Charsets.US_ASCII))
            buf.putInt(4)
            buf.putInt(if (bytesPerFrame > 0) dataSizeBytes / bytesPerFrame else 0)
        }

        buf.put("data".toByteArray(Charsets.US_ASCII))
        buf.putInt(dataSizeBytes)
        return buf.array()
    }

    /** Bellekteki örnekleri tek seferde WAV dosyasına yazar. */
    fun write(
        file: File,
        samples: FloatArray,
        count: Int = samples.size,
        sampleRate: Int,
        encoding: PcmEncoding,
        channelCount: Int = 1,
    ) {
        file.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(file)).use { out ->
            val dataSize = count * channelCount * encoding.bitsPerSample / 8
            out.write(header(encoding, sampleRate, channelCount, dataSize))
            writeSamples(out, samples, count, encoding)
        }
    }

    internal fun writeSamples(out: OutputStream, samples: FloatArray, count: Int, encoding: PcmEncoding) {
        val bytesPerSample = encoding.bitsPerSample / 8
        val buf = ByteBuffer.allocate(count * bytesPerSample).order(ByteOrder.LITTLE_ENDIAN)
        when (encoding) {
            PcmEncoding.FLOAT_32 -> for (i in 0 until count) buf.putFloat(samples[i])
            PcmEncoding.PCM_16 -> for (i in 0 until count) {
                val clamped = samples[i].coerceIn(-1f, 1f)
                buf.putShort((clamped * 32767f).toInt().toShort())
            }
        }
        out.write(buf.array())
    }
}
