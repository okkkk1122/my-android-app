package com.gymway.workout.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymway.workout.ui.screen.WorkoutHomeScreen
import com.gymway.workout.ui.screen.WorkoutDetailScreen
import com.gymway.workout.ui.screen.ProgressScreen
import com.gymway.workout.viewmodel.WorkoutViewModel
import com.gymway.workout.viewmodel.WorkoutViewModelFactory

object WorkoutRoutes {
    const val WORKOUT_HOME = "workout_home"
    const val WORKOUT_DETAIL = "workout_detail"
    const val PROGRESS = "progress"
}

fun NavGraphBuilder.workoutGraph(
    navController: NavController,
    userId: String,
    context: Context
) {
    println("🗺️ [WorkoutNavigation] ساخت workoutGraph")

    composable(WorkoutRoutes.WORKOUT_HOME) {
        println("📍 [WorkoutNavigation] کامپوز WORKOUT_HOME")
        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        WorkoutHomeScreen(
            navController = navController,
            workoutViewModel = workoutViewModel
        )
    }

    composable(
        route = "${WorkoutRoutes.WORKOUT_DETAIL}/{workoutId}"
    ) { backStackEntry ->
        val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
        println("📍 [WorkoutNavigation] کامپوز WORKOUT_DETAIL - workoutId: $workoutId")

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
        println("📍 [WorkoutNavigation] کامپوز PROGRESS")
        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = WorkoutViewModelFactory(context)
        )
        ProgressScreen(
            workoutViewModel = workoutViewModel,
            userId = userId,
            onBack = {
                println("🔙 [WorkoutNavigation] بازگشت از PROGRESS")
                navController.popBackStack()
            }
        )
    }
}