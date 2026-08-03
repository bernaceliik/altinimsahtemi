package com.bernacelik.altinimsahtemi

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bernacelik.altinimsahtemi.ui.screens.*
import com.bernacelik.altinimsahtemi.ui.theme.AltinimSahtemiTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val libraryRecords = remember { mutableStateListOf<RecordItem>() }
    var isKuyumcuUser by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "welcome") {

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { role, isApproved, businessName, taxNumber ->
                    isKuyumcuUser = (role == "jeweler")
                    if (role == "jeweler" && !isApproved) {
                        val bName = if (businessName.isBlank()) "none" else businessName
                        val tNo = if (taxNumber.isBlank()) "none" else taxNumber
                        navController.navigate("approval_pending/$bName/$tNo") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                },
                onNavigateToGoogleSignIn = { navController.navigate("google_signin") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        composable("forgot_password") { ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() }) }

        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { isJeweler, email ->
                    isKuyumcuUser = isJeweler
                    navController.navigate("email_verification/$isJeweler/$email") { popUpTo("register") { inclusive = true } }
                }
            )
        }

        composable(
            route = "email_verification/{isKuyumcu}/{email}",
            arguments = listOf(
                navArgument("isKuyumcu") { type = NavType.BoolType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isKuyumcuArg = backStackEntry.arguments?.getBoolean("isKuyumcu") ?: false
            val emailArg = backStackEntry.arguments?.getString("email") ?: "can@ornek.com"
            LaunchedEffect(isKuyumcuArg) { isKuyumcuUser = isKuyumcuArg }
            EmailVerificationScreen(
                email = emailArg,
                onVerifySuccess = {
                    if (isKuyumcuArg) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    val bName = doc.getString("businessName") ?: "Kuyumcu İşletmesi"
                                    val tNo = doc.getString("taxNumber") ?: "Belirtilmedi"
                                    navController.navigate("approval_pending/$bName/$tNo") {
                                        popUpTo("email_verification/{isKuyumcu}/{email}") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    navController.navigate("approval_pending/Kuyumcu İşletmesi/Belirtilmedi") {
                                        popUpTo("email_verification/{isKuyumcu}/{email}") { inclusive = true }
                                    }
                                }
                        } else {
                            navController.navigate("approval_pending/Kuyumcu İşletmesi/Belirtilmedi") {
                                popUpTo("email_verification/{isKuyumcu}/{email}") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("home") { popUpTo("email_verification/{isKuyumcu}/{email}") { inclusive = true } }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "approval_pending/{businessName}/{taxNumber}",
            arguments = listOf(
                navArgument("businessName") { type = NavType.StringType },
                navArgument("taxNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bNameArg = backStackEntry.arguments?.getString("businessName") ?: "Can kuyumculuk"
            val tNoArg = backStackEntry.arguments?.getString("taxNumber") ?: "••••4821"
            val bName = if (bNameArg == "none") "Can kuyumculuk" else bNameArg
            val tNo = if (tNoArg == "none") "••••4821" else tNoArg
            ApprovalPendingScreen(businessName = bName, taxNumber = tNo)
        }

        // --- İYİLEŞTİRİLMİŞ NAVİGASYON BLOĞU ---
        // Aşağıdaki tüm onNavigateToTab çağrıları artık "startDestinationId" ile kilitlenmeden çalışacak.

        composable("home") {
            HomeScreen(libraryRecords, { navController.navigate("session_setup") }, { route -> navigateToTab(navController, route) }, { navController.navigate("profile") })
        }

        composable("session_setup") {
            SessionSetupScreen({ navController.popBackStack() }, { m, o, s -> navController.navigate("environment_control/$m/$o/$s") }, { navController.navigate("profile") })
        }

        composable(
            route = "environment_control/{material}/{objectType}/{surface}",
            arguments = listOf(navArgument("material") { type = NavType.StringType }, navArgument("objectType") { type = NavType.StringType }, navArgument("surface") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            EnvironmentControlScreen(m, o, s, { navController.popBackStack() }, { navController.navigate("audio_record/$m/$o/$s") }, { navController.navigate("profile") })
        }

        composable(
            route = "audio_record/{material}/{objectType}/{surface}",
            arguments = listOf(navArgument("material") { type = NavType.StringType }, navArgument("objectType") { type = NavType.StringType }, navArgument("surface") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            AudioRecordScreen(m, o, s, { navController.popBackStack() }, { dur, wavPath -> navController.navigate("audio_result/$m/$o/$s/$dur/$wavPath") }, { navController.navigate("profile") })
        }

        composable(
            route = "audio_result/{material}/{objectType}/{surface}/{duration}/{wavPath}",
            arguments = listOf(
                navArgument("material") { type = NavType.StringType },
                navArgument("objectType") { type = NavType.StringType },
                navArgument("surface") { type = NavType.StringType },
                navArgument("duration") { type = NavType.StringType },
                navArgument("wavPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            val d = backStackEntry.arguments?.getString("duration") ?: "00:00:00"
            val wavPathEncoded = backStackEntry.arguments?.getString("wavPath") ?: ""
            val wavPath = Uri.decode(wavPathEncoded)
            AudioResultScreen(
                material = m,
                objectType = o,
                wavPath = wavPath,
                onNavigateToReview = { navController.navigate("record_review/$m/$o/$s/$d/$wavPathEncoded") },
                onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
            )
        }

        composable(
            route = "record_review/{material}/{objectType}/{surface}/{duration}/{wavPath}",
            arguments = listOf(
                navArgument("material") { type = NavType.StringType },
                navArgument("objectType") { type = NavType.StringType },
                navArgument("surface") { type = NavType.StringType },
                navArgument("duration") { type = NavType.StringType },
                navArgument("wavPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            val d = backStackEntry.arguments?.getString("duration") ?: "00:00:00"
            val wavPathEncoded = backStackEntry.arguments?.getString("wavPath") ?: ""
            val wavPath = Uri.decode(wavPathEncoded)
            RecordReviewScreen(
                duration = d,
                wavPath = wavPath,
                material = m,
                objectType = o,
                surface = s,
                onSaveAndUpload = {
                    libraryRecords.add(0, RecordItem(libraryRecords.size + 1, m, o, "Şimdi", "$s zemin", Color.Yellow, "BUGÜN"))
                    navController.navigate("library") { popUpTo("home") { inclusive = false } }
                },
                onDiscardAndRepeat = { navController.navigate("session_setup") { popUpTo("home") { inclusive = false } } },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("library") {
            GoldLibraryScreen(libraryRecords, { navController.navigate("session_setup") }, { route -> navigateToTab(navController, route) }, { navController.navigate("profile") })
        }

        composable("settings") {
            SettingsScreen({ navController.navigate("welcome") { popUpTo("home") { inclusive = true } } }, { route -> navigateToTab(navController, route) }, { navController.navigate("profile") })
        }

        composable("google_signin") { GoogleSignInScreen({ navController.navigate("home") { popUpTo("welcome") { inclusive = true } } }, { navController.popBackStack() }) }

        composable("profile") {
            ProfileScreen(libraryRecords.size, isKuyumcuUser, { navController.popBackStack() }, { navController.navigate("welcome") { popUpTo("home") { inclusive = true } } })
        }
    }
}

// Navigasyon kilitlenmesini engelleyen yardımcı fonksiyon
fun navigateToTab(navController: androidx.navigation.NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}