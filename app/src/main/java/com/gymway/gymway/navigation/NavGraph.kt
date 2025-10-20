package com.gymway.gymway.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gymway.auth.AuthViewModel
import com.gymway.auth.ui.LoginScreen
import com.gymway.auth.ui.RegisterScreen
import com.gymway.gymway.ui.EmailVerificationScreen
import com.gymway.gymway.ui.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val EMAIL_VERIFICATION = "email_verification"
    const val HOME = "home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // مدیریت state تغییرات authentication
    LaunchedEffect(authState) {
        when (authState) {
            is com.gymway.auth.AuthUiState.Success -> {
                // اگر کاربر لاگین شده و در صفحه auth هست، به home هدایت شود
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(currentRoute!!) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.REGISTER,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLoginSuccess = { uid ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToEmailVerification = {
                    navController.navigate(Routes.EMAIL_VERIFICATION) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEmailVerification = {
                    navController.navigate(Routes.EMAIL_VERIFICATION) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EMAIL_VERIFICATION) {
            EmailVerificationScreen(
                authViewModel = authViewModel,
                onVerified = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}