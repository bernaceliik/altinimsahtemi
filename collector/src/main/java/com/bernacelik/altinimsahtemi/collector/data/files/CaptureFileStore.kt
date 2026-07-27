package com.bernacelik.altinimsahtemi.collector.data.files

import android.content.Context
import java.io.File

/**
 * Kayıt dosyalarının disk düzeni.
 *
 * Uygulamaya özel harici dizin kullanılır: `adb pull` ile izin gerektirmeden
 * çekilebilir ama uygulama kaldırıldığında temizlenir. Saha ekibi verileri
 * yükleme kuyruğu boşalmadan uygulamayı kaldırmamalıdır.
 *
 * ```
 * files/captures/<sessionId>/kayit-001.wav
 *                           /kayit-001.json
 *                           /session.json
 * ```
 */
class CaptureFileStore(private val context: Context) {

    private val root: File
        get() = File(context.getExternalFilesDir(null) ?: context.filesDir, "captures")

    fun sessionDir(sessionId: String): File = File(root, sessionId).apply { mkdirs() }

    fun wavFile(sessionId: String, sequence: Int): File =
        File(sessionDir(sessionId), "${baseName(sequence)}.wav")

    fun jsonFile(sessionId: String, sequence: Int): File =
        File(sessionDir(sessionId), "${baseName(sequence)}.json")

    fun sessionManifest(sessionId: String): File = File(sessionDir(sessionId), "session.json")

    fun deleteCapture(sessionId: String, sequence: Int) {
        wavFile(sessionId, sequence).delete()
        jsonFile(sessionId, sequence).delete()
    }

    fun deleteSession(sessionId: String) {
        File(root, sessionId).deleteRecursively()
    }

    /** Yükleme kuyruğu ekranında gösterilen toplam disk kullanımı. */
    fun totalBytes(): Long =
        root.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    private fun baseName(sequence: Int): String = "kayit-%03d".format(sequence)
}
