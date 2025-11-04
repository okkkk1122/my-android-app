// workout/navigation/WorkoutNavigation.kt
package com.gymway.workout.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymway.workout.ui.screen.*
import com.gymway.workout.viewmodel.CoachViewModel
import com.gymway.workout.viewmodel.WorkoutViewModel
import com.gymway.workout.viewmodel.WorkoutViewModelFactory

object WorkoutRoutes {
    const val WORKOUT_HOME = "workout_home"
    const val WORKOUT_DETAIL = "workout_detail"
    const val PROGRESS = "progress"
    const val COACH_DASHBOARD = "coach_dashboard"
    const val COACH_ATHLETES = "coach_athletes"
    const val CREATE_WORKOUT = "create_workout"
    const val ATHLETE_PROGRESS = "athlete_progress"
}

private const val TAG = "WorkoutNavigation"

fun NavGraphBuilder.workoutGraph(
    navController: NavController,
    userId: String,
    context: android.content.Context
) {
    Log.d(TAG, "ساخت workoutGraph کامل")

    // صفحات اصلی workout
    composable(WorkoutRoutes.WORKOUT_HOME) {
        Log.d(TAG, "کامپوز WORKOUT_HOME")
        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        WorkoutHomeScreen(
            navController = navController,
            workoutViewModel = workoutViewModel
        )
    }

    composable(
        route = "${WorkoutRoutes.WORKOUT_DETAIL}/{workoutId}",
        arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
    ) { backStackEntry ->
        val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
        Log.d(TAG, "کامپوز WORKOUT_DETAIL - workoutId: $workoutId")

        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        WorkoutDetailScreen(
            navController = navController,
            workoutId = workoutId,
            workoutViewModel = workoutViewModel
        )
    }

    composable(WorkoutRoutes.PROGRESS) {
        Log.d(TAG, "کامپوز PROGRESS")
        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        ProgressScreen(
            workoutViewModel = workoutViewModel,
            userId = userId,
            onBack = {
                Log.d(TAG, "بازگشت از PROGRESS")
                navController.popBackStack()
            }
        )
    }

    // صفحات Coach
    composable(WorkoutRoutes.COACH_DASHBOARD) {
        Log.d(TAG, "کامپوز COACH_DASHBOARD")
        val coachViewModel: CoachViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        CoachDashboardScreen(
            navController = navController,
            coachViewModel = coachViewModel
        )
    }

    composable(WorkoutRoutes.COACH_ATHLETES) {
        Log.d(TAG, "کامپوز COACH_ATHLETES")
        val coachViewModel: CoachViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        CoachAthletesScreen(
            navController = navController,
            coachViewModel = coachViewModel
        )
    }

    composable(WorkoutRoutes.CREATE_WORKOUT) {
        Log.d(TAG, "کامپوز CREATE_WORKOUT")
        val coachViewModel: CoachViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        CreateWorkoutScreen(
            navController = navController,
            coachViewModel = coachViewModel
        )
    }

    composable(
        route = "${WorkoutRoutes.ATHLETE_PROGRESS}/{athleteId}",
        arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
    ) { backStackEntry ->
        val athleteId = backStackEntry.arguments?.getString("athleteId") ?: ""
        Log.d(TAG, "کامپوز ATHLETE_PROGRESS - athleteId: $athleteId")

        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        ProgressScreen(
            workoutViewModel = workoutViewModel,
            userId = athleteId,
            onBack = {
                Log.d(TAG, "بازگشت از ATHLETE_PROGRESS")
                navController.popBackStack()
            }
        )
    }
}