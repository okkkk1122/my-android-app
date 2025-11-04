
package com.gymway.gymway.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.gymway.gymway.ui.ProfileScreen
import com.gymway.workout.ui.screen.CoachAthletesScreen
import com.gymway.workout.ui.screen.CoachDashboardScreen
import com.gymway.workout.ui.screen.CreateWorkoutScreen
import com.gymway.workout.ui.screen.ProgressScreen
import com.gymway.workout.ui.screen.WorkoutDetailScreen
import com.gymway.workout.ui.screen.WorkoutHomeScreen
import com.gymway.workout.viewmodel.CoachViewModel
import com.gymway.workout.viewmodel.WorkoutViewModel
import com.gymway.workout.viewmodel.WorkoutViewModelFactory

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val EMAIL_VERIFICATION = "email_verification"
    const val HOME = "home"
    const val ATHLETE_HOME = "athlete_home"
    const val COACH_HOME = "coach_home"
    const val PROFILE = "profile"
    const val WORKOUT_HOME = "workout_home"
    const val WORKOUT_DETAIL = "workout_detail"
    const val PROGRESS = "progress"

    // 🔥 اضافه کردن routes های Coach
    const val COACH_DASHBOARD = "coach_dashboard"
    const val COACH_ATHLETES = "coach_athletes"
    const val CREATE_WORKOUT = "create_workout"
    const val ATHLETE_PROGRESS = "athlete_progress"
}

private const val TAG = "AppNavGraph"

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(authState, currentUser) {
        when (authState) {
            is com.gymway.auth.AuthUiState.Success -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route

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

    val profileState by authViewModel.profileState.collectAsStateWithLifecycle()
    LaunchedEffect(profileState) {
        when (profileState) {
            is com.gymway.auth.ProfileUiState.Success -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == Routes.PROFILE) {
                    when {
                        currentUser?.isAthlete == true -> {
                            navController.navigate(Routes.ATHLETE_HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                        currentUser?.isCoach == true -> {
                            navController.navigate(Routes.COACH_HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                    }
                    authViewModel.resetProfileState()
                }
            }
            else -> {}
        }
    }

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
                onLoginSuccess = {},
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
                onVerified = {},
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
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
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
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onNavigateToWorkout = {
                    navController.navigate(Routes.WORKOUT_HOME)
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
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onNavigateToCoachDashboard = {
                    Log.d(TAG, "دریافت درخواست برای CoachDashboard")
                    navController.navigate(Routes.COACH_DASHBOARD)
                },
                navController = navController // اضافه کردن navController
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = {
                    when {
                        currentUser?.isAthlete == true -> {
                            navController.navigate(Routes.ATHLETE_HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                        currentUser?.isCoach == true -> {
                            navController.navigate(Routes.COACH_HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // صفحات Workout
        composable(Routes.WORKOUT_HOME) {
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            WorkoutHomeScreen(
                navController = navController,
                workoutViewModel = workoutViewModel
            )
        }

        composable(
            route = "${Routes.WORKOUT_DETAIL}/{workoutId}"
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            WorkoutDetailScreen(
                navController = navController,
                workoutId = workoutId,
                workoutViewModel = workoutViewModel
            )
        }

        composable(Routes.PROGRESS) {
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            ProgressScreen(
                workoutViewModel = workoutViewModel,
                userId = currentUser?.uid ?: "default_user",
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 🔥 صفحات Coach - کاملاً جدید
        composable(Routes.COACH_DASHBOARD) {
            Log.d(TAG, "کامپوز COACH_DASHBOARD")
            val coachViewModel: CoachViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            CoachDashboardScreen(
                navController = navController,
                coachViewModel = coachViewModel
            )
        }

        composable(Routes.COACH_ATHLETES) {
            Log.d(TAG, "کامپوز COACH_ATHLETES")
            val coachViewModel: CoachViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            CoachAthletesScreen(
                navController = navController,
                coachViewModel = coachViewModel
            )
        }

        // ✅ اینو جایگزین کن:
        // در بخش composable مربوط به CREATE_WORKOUT:
        composable(Routes.CREATE_WORKOUT) {
            Log.d(TAG, "🎯 کامپوز CREATE_WORKOUT - شروع")

            val coachViewModel: CoachViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )

            // استفاده از صفحه ایمن‌سازی شده
            CreateWorkoutScreen(
                navController = navController,
                coachViewModel = coachViewModel
            )
        }

        composable(
            route = "${Routes.ATHLETE_PROGRESS}/{athleteId}"
        ) { backStackEntry ->
            println("🔍 [NavGraph-ROUTE] وارد ATHLETE_PROGRESS شدیم")

            val athleteId = backStackEntry.arguments?.getString("athleteId") ?: ""
            println("🔍 [NavGraph-ROUTE] athleteId: $athleteId")

            val currentRoute = navController.currentBackStackEntry?.destination?.route
            println("🔍 [NavGraph-ROUTE] route فعلی: $currentRoute")

            // تست: بدون ViewModel
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(context)
            )
            println("🔍 [NavGraph-ROUTE] ViewModel ساخته شد: $workoutViewModel")

            ProgressScreen(
                workoutViewModel = workoutViewModel,
                userId = athleteId,
                onBack = {
                    println("🔍 [NavGraph-ROUTE] فراخوانی onBack")
                    navController.popBackStack()
                }
            )
        }
    }
}

fun AuthViewModel.getCurrentUser() = currentUser.value
