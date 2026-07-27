package com.bernacelik.altinimsahtemi.collector.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Her WAV dosyasının yanına yazılan ve Firestore'a da giden metadata.
 *
 * Şema sürümlenmiştir: alan eklendiğinde [SCHEMA_VERSION] artırılır, böylece
 * Python tarafındaki yükleyici eski ve yeni kayıtları ayırt edebilir. Bir alanın
 * anlamı asla değiştirilmez — yalnızca yeni alan eklenir.
 */
@Serializable
data class CaptureMetadata(
    val schemaVersion: Int = SCHEMA_VERSION,
    val captureId: String,
    val sessionId: String,
    val sequence: Int,
    val label: Label,
    val audio: AudioInfo,
    val levels: Levels,
    val device: DeviceInfo,
    val operator: OperatorInfo,
    val capturedAt: String,
    val trigger: String,
    val quality: String,
) {
    @Serializable
    data class Label(
        val material: String,
        val materialLabel: String,
        val isGenuineGold: Boolean,
        val objectType: String,
        val objectTypeLabel: String,
        val surface: String,
        val surfaceLabel: String,
        val strikeMethod: String,
        val strikeMethodLabel: String,
        val notes: String = "",
    )

    @Serializable
    data class AudioInfo(
        val sampleRate: Int,
        val bitDepth: Int,
        val encoding: String,
        val channels: Int,
        /** UNPROCESSED / VOICE_RECOGNITION / MIC — hangi zincirin kullanıldığı. */
        val audioSource: String,
        /** Platform işlenmemiş kaynağı desteklediğini bildiriyor muydu. */
        val unprocessedSupported: Boolean,
        val agcDisabled: Boolean,
        val nsDisabled: Boolean,
        val aecDisabled: Boolean,
        val durationMs: Int,
        val preRollMs: Int,
    )

    @Serializable
    data class Levels(
        val peakDbfs: Float,
        val rmsDbfs: Float,
        val noiseFloorDbfs: Float,
        val clipped: Boolean,
    )

    @Serializable
    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val androidApi: Int,
        val deviceId: String,
        val micDistanceCm: Int,
        val micAngleDeg: Int,
    )

    @Serializable
    data class OperatorInfo(
        val id: String,
        val name: String,
    )

    companion object {
        const val SCHEMA_VERSION = 1

        val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
