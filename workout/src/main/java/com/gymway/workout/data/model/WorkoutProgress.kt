package com.gymway.workout.data.model

import com.google.firebase.Timestamp

data class WorkoutProgress(
    val id: String = "",
    val userId: String = "",
    val workoutPlanId: String = "",
    val date: Timestamp = Timestamp.now(),
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val notes: String = "",
    val weight: Double? = null,
    val bodyFat: Double? = null
) {
    val completionRate: Float
        get() = if (totalExercises > 0) {
            completedExercises.toFloat() / totalExercises
        } else {
            0f
        }
}