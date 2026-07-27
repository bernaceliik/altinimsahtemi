package com.bernacelik.altinimsahtemi.collector.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Bir kaydın yükleme kuyruğundaki durumu. */
enum class UploadState { PENDING, UPLOADING, UPLOADED, FAILED }

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,

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

    // Oturum boyunca sabit kalan kayıt yapılandırması — cihazda doğrulanmış hâli.
    val sampleRate: Int,
    val bitDepth: Int,
    val encoding: String,
    val audioSource: String,
    val unprocessedSupported: Boolean,
    val noiseFloorDbfs: Float,

    val operatorId: String,
    val operatorName: String,
    val notes: String = "",

    val createdAt: Long,
    val closedAt: Long? = null,
)

@Entity(
    tableName = "captures",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("uploadState")],
)
data class CaptureEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    /** Oturum içindeki sıra numarası — dosya adı `kayit-001.wav` bundan üretilir. */
    val sequence: Int,

    val wavPath: String,
    val jsonPath: String,

    val durationMs: Int,
    val peakDbfs: Float,
    val rmsDbfs: Float,
    val noiseFloorDbfs: Float,
    val clipped: Boolean,
    /** CaptureQuality: CLEAN / CLIPPED / TOO_QUIET */
    val quality: String,
    /** TriggerType: auto_onset / manual */
    val trigger: String,

    val createdAt: Long,
    val uploadState: String = UploadState.PENDING.name,
    val uploadError: String? = null,
)

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun byId(id: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("UPDATE sessions SET closedAt = :closedAt WHERE id = :id")
    suspend fun close(id: String, closedAt: Long)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface CaptureDao {
    @Insert
    suspend fun insert(capture: CaptureEntity)

    @Update
    suspend fun update(capture: CaptureEntity)

    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun byId(id: String): CaptureEntity?

    @Query("SELECT * FROM captures WHERE sessionId = :sessionId ORDER BY sequence ASC")
    fun observeBySession(sessionId: String): Flow<List<CaptureEntity>>

    @Query("SELECT COUNT(*) FROM captures WHERE sessionId = :sessionId")
    suspend fun countInSession(sessionId: String): Int

    @Query("SELECT COALESCE(MAX(sequence), 0) FROM captures WHERE sessionId = :sessionId")
    suspend fun lastSequence(sessionId: String): Int

    @Query("SELECT * FROM captures WHERE uploadState IN (:states) ORDER BY createdAt ASC")
    suspend fun byUploadStates(states: List<String>): List<CaptureEntity>

    @Query("SELECT * FROM captures ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CaptureEntity>>

    @Query("UPDATE captures SET uploadState = :state, uploadError = :error WHERE id = :id")
    suspend fun setUploadState(id: String, state: String, error: String?)

    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun delete(id: String)
}

@Database(
    entities = [SessionEntity::class, CaptureEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CollectorDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun captureDao(): CaptureDao

    companion object {
        fun create(context: Context): CollectorDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CollectorDatabase::class.java,
                "collector.db",
            ).build()
    }
}
