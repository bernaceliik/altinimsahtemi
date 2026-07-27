package com.bernacelik.altinimsahtemi.collector.data.model

import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import kotlinx.serialization.Serializable

/**
 * Oturum kapatıldığında klasöre yazılan özet (`session.json`).
 *
 * Tek tek kayıtların sidecar JSON'ları zaten etiketleri taşır; bu dosya oturumun
 * bütününü — kaç tekrar hedeflendiği, hangi donanım profiliyle çalışıldığı —
 * tek bakışta okunabilir kılar.
 */
@Serializable
data class SessionManifest(
    val schemaVersion: Int = CaptureMetadata.SCHEMA_VERSION,
    val sessionId: String,
    val material: String,
    val materialLabel: String,
    val isGenuineGold: Boolean,
    val objectType: String,
    val objectTypeLabel: String,
    val surface: String,
    val surfaceLabel: String,
    val strikeMethod: String,
    val strikeMethodLabel: String,
    val micDistanceCm: Int,
    val micAngleDeg: Int,
    val targetRepetitions: Int,
    val capturedCount: Int,
    val sampleRate: Int,
    val bitDepth: Int,
    val encoding: String,
    val audioSource: String,
    val unprocessedSupported: Boolean,
    val noiseFloorDbfs: Float,
    val operatorId: String,
    val operatorName: String,
    val notes: String,
    val createdAt: Long,
    val closedAt: Long?,
) {
    companion object {
        fun from(session: SessionEntity, capturedCount: Int) = SessionManifest(
            sessionId = session.id,
            material = session.materialSlug,
            materialLabel = session.materialLabel,
            isGenuineGold = session.isGenuineGold,
            objectType = session.objectTypeSlug,
            objectTypeLabel = session.objectTypeLabel,
            surface = session.surfaceSlug,
            surfaceLabel = session.surfaceLabel,
            strikeMethod = session.strikeMethodSlug,
            strikeMethodLabel = session.strikeMethodLabel,
            micDistanceCm = session.micDistanceCm,
            micAngleDeg = session.micAngleDeg,
            targetRepetitions = session.targetRepetitions,
            capturedCount = capturedCount,
            sampleRate = session.sampleRate,
            bitDepth = session.bitDepth,
            encoding = session.encoding,
            audioSource = session.audioSource,
            unprocessedSupported = session.unprocessedSupported,
            noiseFloorDbfs = session.noiseFloorDbfs,
            operatorId = session.operatorId,
            operatorName = session.operatorName,
            notes = session.notes,
            createdAt = session.createdAt,
            closedAt = session.closedAt,
        )
    }
}
