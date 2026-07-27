package com.bernacelik.altinimsahtemi.collector.ui

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bernacelik.altinimsahtemi.audio.AudioMetrics
import com.bernacelik.altinimsahtemi.audio.AudioProfile
import com.bernacelik.altinimsahtemi.audio.CaptureEngine
import com.bernacelik.altinimsahtemi.audio.CaptureEvent
import com.bernacelik.altinimsahtemi.audio.RawAudioRecorder
import com.bernacelik.altinimsahtemi.collector.AppContainer
import com.bernacelik.altinimsahtemi.collector.CollectorApp
import com.bernacelik.altinimsahtemi.collector.data.SessionRequest
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureEntity
import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import com.bernacelik.altinimsahtemi.collector.data.prefs.Operator
import com.bernacelik.altinimsahtemi.collector.data.upload.UploadWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Kalibrasyon ekranının durumu. */
sealed interface CalibrationState {
    data object Idle : CalibrationState

    data class Measuring(val progress: Float, val currentDbfs: Float) : CalibrationState

    data class Done(
        val noiseFloorDbfs: Float,
        val thresholdDbfs: Float,
        val verdict: Verdict,
    ) : CalibrationState

    /** Ortamın kayda uygunluğu. */
    enum class Verdict { QUIET, ACCEPTABLE, TOO_NOISY }
}

/** Canlı seviye göstergesi için anlık ölçüm. */
data class LevelSnapshot(
    val peakDbfs: Float,
    val rmsDbfs: Float,
    val noiseFloorDbfs: Float,
    val thresholdDbfs: Float,
)

/** Son yakalanan darbenin görsel özeti (dalga formu şeridi için). */
data class ImpactPreview(
    val sequence: Int,
    val envelope: FloatArray,
    val quality: String,
    val peakDbfs: Float,
)

class CollectorViewModel(private val container: AppContainer) : ViewModel() {

    val operator: StateFlow<Operator> = container.operatorStore.operator
        .stateIn(viewModelScope, SharingStarted.Eagerly, Operator("", "", ""))

    private val _profile = MutableStateFlow(container.audioProfile)
    val profile: StateFlow<AudioProfile?> = _profile.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    private val _calibration = MutableStateFlow<CalibrationState>(CalibrationState.Idle)
    val calibration: StateFlow<CalibrationState> = _calibration.asStateFlow()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _session = MutableStateFlow<SessionEntity?>(null)
    val session: StateFlow<SessionEntity?> = _session.asStateFlow()

    private val _level = MutableStateFlow<LevelSnapshot?>(null)
    val level: StateFlow<LevelSnapshot?> = _level.asStateFlow()

    private val _lastImpact = MutableStateFlow<ImpactPreview?>(null)
    val lastImpact: StateFlow<ImpactPreview?> = _lastImpact.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _autoTrigger = MutableStateFlow(true)
    val autoTrigger: StateFlow<Boolean> = _autoTrigger.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val captures: StateFlow<List<CaptureEntity>> = _sessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else container.repository.observeCaptures(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCaptures: StateFlow<List<CaptureEntity>> = container.repository.observeAllCaptures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uploaderConfigured: Boolean get() = container.uploader.isConfigured

    private var captureJob: Job? = null
    private var calibrationJob: Job? = null
    private var activeEngine: CaptureEngine? = null

    // --- Kurulum -----------------------------------------------------------

    fun saveOperator(name: String, deviceNote: String) {
        viewModelScope.launch { container.operatorStore.save(name, deviceNote) }
    }

    /** Cihazı yoklayıp doğrulanmış profili yükler. İzin verildikten sonra çağrılmalı. */
    fun detectProfile() {
        viewModelScope.launch {
            container.operatorStore.ensureDeviceId()
            val detected = container.ensureAudioProfile()
            _profile.value = detected
            _profileError.value = if (detected == null) {
                "Cihazda çalışan bir kayıt yapılandırması bulunamadı. Mikrofon izni verildi mi?"
            } else {
                null
            }
        }
    }

    // --- Kalibrasyon -------------------------------------------------------

    /**
     * Ortam gürültü tabanını ölçer.
     *
     * Ortanca (medyan) kullanılır, ortalama değil: ölçüm sırasında araya giren
     * tek bir kapı çarpması tüm tabanı yukarı çekip eşiği bozmasın diye.
     */
    @SuppressLint("MissingPermission")
    fun runCalibration(durationMs: Int = 5_000) {
        val profile = _profile.value ?: return
        stopListening()
        calibrationJob?.cancel()
        _calibration.value = CalibrationState.Measuring(0f, AudioMetrics.SILENCE_DBFS)

        calibrationJob = viewModelScope.launch {
            val readings = mutableListOf<Float>()
            val startedAt = System.currentTimeMillis()

            val reader = launch {
                RawAudioRecorder(profile).audioStream().collect { block ->
                    val rms = AudioMetrics.rmsDbfs(block.samples, block.frameCount)
                    readings += rms
                    _calibration.value = CalibrationState.Measuring(
                        progress = ((System.currentTimeMillis() - startedAt).toFloat() / durationMs)
                            .coerceIn(0f, 1f),
                        currentDbfs = rms,
                    )
                }
            }
            delay(durationMs.toLong())
            reader.cancel()

            val noiseFloor = if (readings.isEmpty()) {
                AudioMetrics.SILENCE_DBFS
            } else {
                readings.sorted()[readings.size / 2]
            }
            val threshold = maxOf(
                noiseFloor + container.captureConfig.triggerThresholdDb,
                container.captureConfig.minTriggerDbfs,
            )
            _calibration.value = CalibrationState.Done(
                noiseFloorDbfs = noiseFloor,
                thresholdDbfs = threshold,
                verdict = when {
                    noiseFloor <= -70f -> CalibrationState.Verdict.QUIET
                    noiseFloor <= -55f -> CalibrationState.Verdict.ACCEPTABLE
                    else -> CalibrationState.Verdict.TOO_NOISY
                },
            )
        }
    }

    fun resetCalibration() {
        calibrationJob?.cancel()
        calibrationJob = null
        _calibration.value = CalibrationState.Idle
    }

    // --- Oturum ------------------------------------------------------------

    fun startSession(request: SessionRequest) {
        val profile = _profile.value ?: return
        val noiseFloor = (_calibration.value as? CalibrationState.Done)?.noiseFloorDbfs
            ?: AudioMetrics.SILENCE_DBFS

        viewModelScope.launch {
            val id = container.repository.startSession(request, profile, noiseFloor, operator.value)
            _sessionId.value = id
            _session.value = container.repository.session(id)
        }
    }

    /**
     * Sürekli dinlemeyi başlatır: seviye göstergesini besler ve tespit edilen
     * her darbeyi WAV + JSON olarak diske yazar.
     */
    @SuppressLint("MissingPermission")
    fun startListening() {
        if (captureJob != null) return
        val profile = _profile.value ?: return
        val sessionId = _sessionId.value ?: return

        val engine = CaptureEngine(profile, container.captureConfig)
        engine.setAutoTriggerEnabled(_autoTrigger.value)
        activeEngine = engine
        _isListening.value = true

        captureJob = viewModelScope.launch {
            try {
                engine.events().collect { event ->
                    when (event) {
                        is CaptureEvent.Level -> _level.value = LevelSnapshot(
                            peakDbfs = event.peakDbfs,
                            rmsDbfs = event.rmsDbfs,
                            noiseFloorDbfs = event.noiseFloorDbfs,
                            thresholdDbfs = event.thresholdDbfs,
                        )

                        is CaptureEvent.Impact -> {
                            val saved = container.repository.saveCapture(
                                sessionId = sessionId,
                                impact = event.capture,
                                effects = engine.effectState,
                                config = container.captureConfig,
                                operator = operator.value,
                            )
                            _lastImpact.value = ImpactPreview(
                                sequence = saved.sequence,
                                envelope = envelope(event.capture.samples),
                                quality = saved.quality,
                                peakDbfs = saved.peakDbfs,
                            )
                        }
                    }
                }
            } finally {
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        captureJob?.cancel()
        captureJob = null
        activeEngine = null
        _isListening.value = false
        _level.value = null
    }

    fun setAutoTrigger(enabled: Boolean) {
        _autoTrigger.value = enabled
        activeEngine?.setAutoTriggerEnabled(enabled)
    }

    fun requestManualCapture() {
        activeEngine?.requestManualCapture()
    }

    fun deleteCapture(capture: CaptureEntity) {
        viewModelScope.launch {
            container.repository.deleteCapture(capture)
            if (_lastImpact.value?.sequence == capture.sequence) _lastImpact.value = null
        }
    }

    /** Son kaydı geri alır — saha ekibinin en sık ihtiyaç duyduğu işlem. */
    fun deleteLastCapture() {
        captures.value.maxByOrNull { it.sequence }?.let { deleteCapture(it) }
    }

    fun closeSession(onClosed: () -> Unit = {}) {
        val id = _sessionId.value ?: return
        stopListening()
        viewModelScope.launch {
            container.repository.closeSession(id)
            UploadWorker.enqueue(container.appContext)
            _sessionId.value = null
            _session.value = null
            _lastImpact.value = null
            _calibration.value = CalibrationState.Idle
            onClosed()
        }
    }

    fun retryUploadsNow() = UploadWorker.retryNow(container.appContext)

    fun totalStorageBytes(): Long = container.fileStore.totalBytes()

    override fun onCleared() {
        stopListening()
        calibrationJob?.cancel()
        super.onCleared()
    }

    /** Dalga formu şeridi için kova başına tepe değeri. */
    private fun envelope(samples: FloatArray, buckets: Int = 96): FloatArray {
        if (samples.isEmpty()) return FloatArray(0)
        val bucketSize = maxOf(1, samples.size / buckets)
        return FloatArray(buckets) { bucket ->
            val from = bucket * bucketSize
            val to = minOf(from + bucketSize, samples.size)
            if (from >= to) {
                0f
            } else {
                var peak = 0f
                for (i in from until to) {
                    val v = kotlin.math.abs(samples[i])
                    if (v > peak) peak = v
                }
                peak
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CollectorViewModel((this[APPLICATION_KEY] as CollectorApp).container)
            }
        }
    }
}
