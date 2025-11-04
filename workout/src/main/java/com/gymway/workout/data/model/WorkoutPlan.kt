package com.gymway.workout.data.model

import com.google.firebase.firestore.PropertyName

data class WorkoutPlan(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("title") val title: String = "",
    @get:PropertyName("description") val description: String = "",
    @get:PropertyName("exercises") val exercises: List<Exercise> = emptyList(),
    @get:PropertyName("duration") val duration: Int = 60,
    @get:PropertyName("difficulty") val difficulty: String = "beginner",
    @get:PropertyName("createdBy") val createdBy: String = "",
    @get:PropertyName("assignedTo") val assignedTo: String = ""
) {
    // Propertyهای computed - بدون مشکل Firestore
    val progress: Float
        get() = if (exercises.isEmpty()) 0f else {
            val completed = exercises.count { it.isCompleted }
            completed.toFloat() / exercises.size
        }

    val isCompleted: Boolean
        get() = exercises.isNotEmpty() && exercises.all { it.isCompleted }
}