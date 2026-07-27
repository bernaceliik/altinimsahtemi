package com.bernacelik.altinimsahtemi.collector.data

import android.os.Build
import com.bernacelik.altinimsahtemi.audio.AudioProfile
import com.bernacelik.altinimsahtemi.audio.CaptureConfig
import com.bernacelik.altinimsahtemi.audio.CapturedImpact
import com.bernacelik.altinimsahtemi.audio.EffectState
import com.bernacelik.altinimsahtemi.audio.WavWriter
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureDao
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureEntity
import com.bernacelik.altinimsahtemi.collector.data.db.SessionDao
import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import com.bernacelik.altinimsahtemi.collector.data.db.UploadState
import com.bernacelik.altinimsahtemi.collector.data.files.CaptureFileStore
import com.bernacelik.altinimsahtemi.collector.data.model.CaptureMetadata
import com.bernacelik.altinimsahtemi.collector.data.model.SessionManifest
import com.bernacelik.altinimsahtemi.collector.data.prefs.Operator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/** Oturum kurulum ekranında toplanan etiketler. */
data class SessionRequest(
    val materialSlug: String,
    val materialLabel: String,
    val isGenuineGold: Boolean,
    val objectTypeSlug: String,
    val objectTypeLabel: String,
    val surfaceSlug: String,
    val surfaceLabel: String,
    val strikeMethodSlug: String,
    val strikeMethodLabel: String,
    val micDistanceCm: Int,
    val micAngleDeg: Int,
    val targetRepetitions: Int,
    val notes: String = "",
)

/**
 * Yakalanan darbeleri kalıcılaştırır: WAV + sidecar JSON diske, satır veritabanına.
 *
 * Sidecar JSON bilinçli bir tekrardır — veritabanı bozulsa veya dosyalar
 * `adb pull` ile elle çekilse bile her WAV kendi etiketini yanında taşır.
 */
class CaptureRepository(
    private val sessionDao: SessionDao,
    private val captureDao: CaptureDao,
    private val fileStore: CaptureFileStore,
) {

    fun observeCaptures(sessionId: String): Flow<List<CaptureEntity>> =
        captureDao.observeBySession(sessionId)

    fun observeAllCaptures(): Flow<List<CaptureEntity>> = captureDao.observeAll()

    fun observeSessions(): Flow<List<SessionEntity>> = sessionDao.observeAll()

    suspend fun session(id: String): SessionEntity? = sessionDao.byId(id)

    suspend fun startSession(
        request: SessionRequest,
        profile: AudioProfile,
        noiseFloorDbfs: Float,
        operator: Operator,
    ): String = withContext(Dispatchers.IO) {
        val session = SessionEntity(
            id = UUID.randomUUID().toString(),
            materialSlug = request.materialSlug,
            materialLabel = request.materialLabel,
            isGenuineGold = request.isGenuineGold,
            objectTypeSlug = request.objectTypeSlug,
            objectTypeLabel = request.objectTypeLabel,
            surfaceSlug = request.surfaceSlug,
            surfaceLabel = request.surfaceLabel,
            strikeMethodSlug = request.strikeMethodSlug,
            strikeMethodLabel = request.strikeMethodLabel,
            micDistanceCm = request.micDistanceCm,
            micAngleDeg = request.micAngleDeg,
            targetRepetitions = request.targetRepetitions,
            sampleRate = profile.sampleRate,
            bitDepth = profile.bitsPerSample,
            encoding = profile.encoding.wireName,
            audioSource = profile.source.name,
            unprocessedSupported = profile.unprocessedSupported,
            noiseFloorDbfs = noiseFloorDbfs,
            operatorId = operator.id,
            operatorName = operator.name,
            notes = request.notes,
            createdAt = System.currentTimeMillis(),
        )
        sessionDao.insert(session)
        session.id
    }

    /** Darbeyi WAV + JSON olarak yazar ve kuyruğa `PENDING` durumunda ekler. */
    suspend fun saveCapture(
        sessionId: String,
        impact: CapturedImpact,
        effects: EffectState,
        config: CaptureConfig,
        operator: Operator,
    ): CaptureEntity = withContext(Dispatchers.IO) {
        val session = requireNotNull(sessionDao.byId(sessionId)) { "Oturum bulunamadı: $sessionId" }
        val sequence = captureDao.lastSequence(sessionId) + 1
        val captureId = UUID.randomUUID().toString()

        val wavFile = fileStore.wavFile(sessionId, sequence)
        WavWriter.write(
            file = wavFile,
            samples = impact.samples,
            count = impact.samples.size,
            sampleRate = impact.profile.sampleRate,
            encoding = impact.profile.encoding,
            channelCount = impact.profile.channelCount,
        )

        val metadata = buildMetadata(captureId, session, sequence, impact, effects, config, operator)
        val jsonFile = fileStore.jsonFile(sessionId, sequence)
        jsonFile.writeText(CaptureMetadata.json.encodeToString(CaptureMetadata.serializer(), metadata))

        val entity = CaptureEntity(
            id = captureId,
            sessionId = sessionId,
            sequence = sequence,
            wavPath = wavFile.absolutePath,
            jsonPath = jsonFile.absolutePath,
            durationMs = impact.durationMs,
            peakDbfs = impact.peakDbfs,
            rmsDbfs = impact.rmsDbfs,
            noiseFloorDbfs = impact.noiseFloorDbfs,
            clipped = impact.clipped,
            quality = impact.quality().name,
            trigger = impact.trigger.wireName,
            createdAt = impact.capturedAtMillis,
            uploadState = UploadState.PENDING.name,
        )
        captureDao.insert(entity)
        entity
    }

    /** Kaydı hem diskten hem veritabanından siler (saha ekibinin "geri al" işlemi). */
    suspend fun deleteCapture(capture: CaptureEntity) = withContext(Dispatchers.IO) {
        fileStore.deleteCapture(capture.sessionId, capture.sequence)
        captureDao.delete(capture.id)
    }

    suspend fun closeSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.close(sessionId, System.currentTimeMillis())
        val session = sessionDao.byId(sessionId) ?: return@withContext
        val manifest = SessionManifest.from(session, captureDao.countInSession(sessionId))
        fileStore.sessionManifest(sessionId).writeText(
            CaptureMetadata.json.encodeToString(SessionManifest.serializer(), manifest)
        )
    }

    suspend fun pendingUploads(): List<CaptureEntity> =
        captureDao.byUploadStates(listOf(UploadState.PENDING.name, UploadState.FAILED.name))

    suspend fun setUploadState(id: String, state: UploadState, error: String? = null) =
        captureDao.setUploadState(id, state.name, error)

    suspend fun captureById(id: String): CaptureEntity? = captureDao.byId(id)

    private fun buildMetadata(
        captureId: String,
        session: SessionEntity,
        sequence: Int,
        impact: CapturedImpact,
        effects: EffectState,
        config: CaptureConfig,
        operator: Operator,
    ) = CaptureMetadata(
        captureId = captureId,
        sessionId = session.id,
        sequence = sequence,
        label = CaptureMetadata.Label(
            material = session.materialSlug,
            materialLabel = session.materialLabel,
            isGenuineGold = session.isGenuineGold,
            objectType = session.objectTypeSlug,
            objectTypeLabel = session.objectTypeLabel,
            surface = session.surfaceSlug,
            surfaceLabel = session.surfaceLabel,
            strikeMethod = session.strikeMethodSlug,
            strikeMethodLabel = session.strikeMethodLabel,
            notes = session.notes,
        ),
        audio = CaptureMetadata.AudioInfo(
            sampleRate = impact.profile.sampleRate,
            bitDepth = impact.profile.bitsPerSample,
            encoding = impact.profile.encoding.wireName,
            channels = impact.profile.channelCount,
            audioSource = impact.profile.source.name,
            unprocessedSupported = impact.profile.unprocessedSupported,
            agcDisabled = effects.agcDisabled,
            nsDisabled = effects.nsDisabled,
            aecDisabled = effects.aecDisabled,
            durationMs = impact.durationMs,
            preRollMs = impact.profile.framesToMs(impact.preRollFrames),
        ),
        levels = CaptureMetadata.Levels(
            peakDbfs = impact.peakDbfs,
            rmsDbfs = impact.rmsDbfs,
            noiseFloorDbfs = impact.noiseFloorDbfs,
            clipped = impact.clipped,
        ),
        device = CaptureMetadata.DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidApi = Build.VERSION.SDK_INT,
            deviceId = operator.id,
            micDistanceCm = session.micDistanceCm,
            micAngleDeg = session.micAngleDeg,
        ),
        operator = CaptureMetadata.OperatorInfo(id = operator.id, name = operator.name),
        capturedAt = isoTimestamp(impact.capturedAtMillis),
        trigger = impact.trigger.wireName,
        quality = impact.quality().name,
    )

    private fun isoTimestamp(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))
}
