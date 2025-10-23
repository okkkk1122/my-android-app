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
import com.gymway.gymway.ui.AthleteHomeScreen
import com.gymway.gymway.ui.CoachHomeScreen
import com.gymway.gymway.ui.EmailVerificationScreen
import com.gymway.gymway.ui.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val EMAIL_VERIFICATION = "email_verification"
    const val HOME = "home"
    const val ATHLETE_HOME = "athlete_home"
    const val COACH_HOME = "coach_home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // مدیریت state تغییرات authentication و نقش کاربر
    LaunchedEffect(authState, currentUser) {
        when (authState) {
            is com.gymway.auth.AuthUiState.Success -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route

                // اگر کاربر لاگین شده و در صفحه auth هست، به صفحه مناسب نقش هدایت شود
                if (currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER || currentRoute == Routes.EMAIL_VERIFICATION) {
                    when {
                        currentUser?.isAthlete == true -> {
                            navController.navigate(Routes.ATHLETE_HOME) {
                                popUpTo(currentRoute!!) { inclusive = true }
                            }
                        }
                        currentUser?.isCoach == true -> {
                            navController.navigate(Routes.COACH_HOME) {
                                popUpTo(currentRoute!!) { inclusive = true }
                            }
                        }
                        else -> {
                            // اگر نقش مشخص نیست، به صفحه اصلی معمولی هدایت شود
                            navController.navigate(Routes.HOME) {
                                popUpTo(currentRoute!!) { inclusive = true }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    // Load current user on app start
    LaunchedEffect(Unit) {
        if (authViewModel.getCurrentUser() != null) {
            authViewModel.loadCurrentUser()
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
                onLoginSuccess = {
                    // Navigation handled by LaunchedEffect
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
                    // Navigation handled by LaunchedEffect
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

        composable(Routes.ATHLETE_HOME) {
            AthleteHomeScreen(
                currentUser = currentUser,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ATHLETE_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.COACH_HOME) {
            CoachHomeScreen(
                currentUser = currentUser,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.COACH_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Extension function to get current user
fun AuthViewModel.getCurrentUser() = currentUser.value