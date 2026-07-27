package com.bernacelik.altinimsahtemi.collector.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bernacelik.altinimsahtemi.collector.data.SessionRequest
import com.bernacelik.altinimsahtemi.collector.ui.screens.CalibrationScreen
import com.bernacelik.altinimsahtemi.collector.ui.screens.CaptureScreen
import com.bernacelik.altinimsahtemi.collector.ui.screens.OperatorSetupScreen
import com.bernacelik.altinimsahtemi.collector.ui.screens.SessionReviewScreen
import com.bernacelik.altinimsahtemi.collector.ui.screens.SessionSetupScreen
import com.bernacelik.altinimsahtemi.collector.ui.screens.UploadQueueScreen

private object Routes {
    const val OPERATOR = "operator"
    const val SESSION_SETUP = "session_setup"
    const val CALIBRATION = "calibration"
    const val CAPTURE = "capture"
    const val REVIEW = "review"
    const val UPLOADS = "uploads"
}

@Composable
fun CollectorNavigation(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: CollectorViewModel = viewModel(factory = CollectorViewModel.Factory),
) {
    val navController = rememberNavController()

    val operator by viewModel.operator.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val profileError by viewModel.profileError.collectAsStateWithLifecycle()
    val calibration by viewModel.calibration.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val captures by viewModel.captures.collectAsStateWithLifecycle()
    val allCaptures by viewModel.allCaptures.collectAsStateWithLifecycle()
    val level by viewModel.level.collectAsStateWithLifecycle()
    val lastImpact by viewModel.lastImpact.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val autoTrigger by viewModel.autoTrigger.collectAsStateWithLifecycle()

    // Kurulum ekranında toplanan etiketler kalibrasyondan sonra kullanılacağı için
    // navigasyon argümanı yerine burada tutulur.
    var pendingRequest by remember { mutableStateOf<SessionRequest?>(null) }

    // İzin verildiği anda cihaz profili yoklanır.
    LaunchedEffect(hasPermission) {
        if (hasPermission && profile == null) viewModel.detectProfile()
    }

    NavHost(navController = navController, startDestination = Routes.OPERATOR) {

        composable(Routes.OPERATOR) {
            OperatorSetupScreen(
                operator = operator,
                profile = profile,
                profileError = profileError,
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onSave = viewModel::saveOperator,
                onContinue = { navController.navigate(Routes.SESSION_SETUP) },
            )
        }

        composable(Routes.SESSION_SETUP) {
            SessionSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = { request ->
                    pendingRequest = request
                    viewModel.resetCalibration()
                    navController.navigate(Routes.CALIBRATION)
                },
            )
        }

        composable(Routes.CALIBRATION) {
            CalibrationScreen(
                state = calibration,
                onBack = { navController.popBackStack() },
                onStart = { viewModel.runCalibration() },
                onRetry = { viewModel.runCalibration() },
                onContinue = {
                    pendingRequest?.let { viewModel.startSession(it) }
                    navController.navigate(Routes.CAPTURE)
                },
            )
        }

        composable(Routes.CAPTURE) {
            CaptureScreen(
                session = session,
                capturedCount = captures.size,
                isListening = isListening,
                autoTrigger = autoTrigger,
                level = level,
                lastImpact = lastImpact,
                onToggleListening = {
                    if (isListening) viewModel.stopListening() else viewModel.startListening()
                },
                onToggleAutoTrigger = viewModel::setAutoTrigger,
                onManualCapture = viewModel::requestManualCapture,
                onUndoLast = viewModel::deleteLastCapture,
                onFinish = {
                    viewModel.stopListening()
                    navController.navigate(Routes.REVIEW)
                },
            )
        }

        composable(Routes.REVIEW) {
            SessionReviewScreen(
                session = session,
                captures = captures,
                onBack = { navController.popBackStack() },
                onDelete = viewModel::deleteCapture,
                onFinish = {
                    viewModel.closeSession()
                    navController.navigate(Routes.UPLOADS) {
                        popUpTo(Routes.OPERATOR) { inclusive = false }
                    }
                },
            )
        }

        composable(Routes.UPLOADS) {
            UploadQueueScreen(
                captures = allCaptures,
                uploaderConfigured = viewModel.uploaderConfigured,
                totalBytes = viewModel.totalStorageBytes(),
                onBack = {
                    navController.navigate(Routes.SESSION_SETUP) {
                        popUpTo(Routes.OPERATOR) { inclusive = false }
                    }
                },
                onRetry = viewModel::retryUploadsNow,
            )
        }
    }
}
