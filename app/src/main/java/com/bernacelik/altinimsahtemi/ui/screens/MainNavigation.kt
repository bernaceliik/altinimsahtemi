package com.bernacelik.altinimsahtemi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bernacelik.altinimsahtemi.ui.screens.*
import com.bernacelik.altinimsahtemi.ui.theme.AltinimSahtemiTheme

class MainNavigation : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AltinimSahtemiTheme {
                AppNavigation()
            }
        }
    }
}

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
                onLoginSuccess = { navController.navigate("home") { popUpTo("welcome") { inclusive = true } } },
                onNavigateToGoogleSignIn = { navController.navigate("google_signin") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        composable("forgot_password") { ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() }) }

        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { isJeweler ->
                    isKuyumcuUser = isJeweler
                    navController.navigate("email_verification/$isJeweler") { popUpTo("register") { inclusive = true } }
                }
            )
        }

        composable(
            route = "email_verification/{isKuyumcu}",
            arguments = listOf(navArgument("isKuyumcu") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isKuyumcuArg = backStackEntry.arguments?.getBoolean("isKuyumcu") ?: false
            LaunchedEffect(isKuyumcuArg) { isKuyumcuUser = isKuyumcuArg }
            EmailVerificationScreen(
                email = "can@ornek.com",
                onVerifySuccess = {
                    if (isKuyumcuArg) navController.navigate("approval_pending") { popUpTo("email_verification/{isKuyumcu}") { inclusive = true } }
                    else navController.navigate("home") { popUpTo("email_verification/{isKuyumcu}") { inclusive = true } }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("approval_pending") { ApprovalPendingScreen(businessName = "Can kuyumculuk", taxNumber = "••••4821") }

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
            AudioRecordScreen(m, o, s, { navController.popBackStack() }, { dur -> navController.navigate("audio_result/$m/$o/$s/$dur") }, { navController.navigate("profile") })
        }

        composable(
            route = "audio_result/{material}/{objectType}/{surface}/{duration}",
            arguments = listOf(navArgument("material") { type = NavType.StringType }, navArgument("objectType") { type = NavType.StringType }, navArgument("surface") { type = NavType.StringType }, navArgument("duration") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            val d = backStackEntry.arguments?.getString("duration") ?: "00:00:00"
            AudioResultScreen(m, o, { navController.navigate("record_review/$m/$o/$s/$d") }, { navController.navigate("home") { popUpTo("home") { inclusive = true } } })
        }

        composable(
            route = "record_review/{material}/{objectType}/{surface}/{duration}",
            arguments = listOf(navArgument("material") { type = NavType.StringType }, navArgument("objectType") { type = NavType.StringType }, navArgument("surface") { type = NavType.StringType }, navArgument("duration") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.arguments?.getString("material") ?: "24K"
            val o = backStackEntry.arguments?.getString("objectType") ?: "Gram"
            val s = backStackEntry.arguments?.getString("surface") ?: "Mermer"
            val d = backStackEntry.arguments?.getString("duration") ?: "00:00:00"
            RecordReviewScreen(d, {
                libraryRecords.add(0, RecordItem(libraryRecords.size + 1, m, o, "Şimdi", "$s zemin", Color.Yellow, "BUGÜN"))
                navController.navigate("library") { popUpTo("home") { inclusive = false } }
            }, { navController.navigate("session_setup") { popUpTo("home") { inclusive = false } } }, { navController.navigate("profile") })
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