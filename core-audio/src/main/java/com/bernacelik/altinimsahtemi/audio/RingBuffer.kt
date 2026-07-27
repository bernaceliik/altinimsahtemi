package com.bernacelik.altinimsahtemi.audio

/**
 * Sabit kapasiteli dairesel float tamponu — darbe öncesi (pre-roll) sesi tutar.
 *
 * Bir darbe tespit edildiğinde iş işten geçmiştir: atağın ilk milisaniyeleri
 * sinyalin en bilgi yoğun kısmıdır ve tetikleme anında çoktan geçmiştir.
 * Sürekli dönen bu tampon sayesinde tetiklemeden **önceki** ses de dosyaya girer.
 */
class RingBuffer(val capacity: Int) {

    init {
        require(capacity > 0) { "Kapasite pozitif olmalı" }
    }

    private val buffer = FloatArray(capacity)
    private var writeIndex = 0

    /** Tamponda hâlihazırda duran geçerli örnek sayısı (kapasiteyi aşmaz). */
    var size: Int = 0
        private set

    fun write(source: FloatArray, count: Int = source.size) {
        require(count <= source.size) { "count, kaynak diziyi aşamaz" }

        // Kapasiteden uzun yazımda yalnızca son `capacity` örnek anlamlıdır.
        val start = if (count > capacity) count - capacity else 0
        val effective = count - start

        for (i in 0 until effective) {
            buffer[writeIndex] = source[start + i]
            writeIndex = (writeIndex + 1) % capacity
        }
        size = minOf(size + effective, capacity)
    }

    /** Tampondaki örnekleri eskiden yeniye doğru sıralı olarak kopyalar. */
    fun snapshot(): FloatArray {
        val out = FloatArray(size)
        // Dolu tamponda en eski örnek writeIndex'tedir; dolmamışta 0'dan başlar.
        val startIndex = if (size < capacity) 0 else writeIndex
        for (i in 0 until size) {
            out[i] = buffer[(startIndex + i) % capacity]
        }
        return out
    }

    fun clear() {
        writeIndex = 0
        size = 0
    }
}
