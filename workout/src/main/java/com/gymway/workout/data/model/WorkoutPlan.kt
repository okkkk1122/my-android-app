package com.gymway.workout.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class WorkoutPlan(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("title") val title: String = "",
    @get:PropertyName("description") val description: String = "",
    @get:PropertyName("exercises") val exercises: List<Exercise> = emptyList(),
    @get:PropertyName("duration") val duration: Int = 60,
    @get:PropertyName("difficulty") val difficulty: String = "beginner",
    @get:PropertyName("createdBy") val createdBy: String = "",
    @get:PropertyName("assignedTo") val assignedTo: String = "",
    @get:PropertyName("assignedAt") val assignedAt: Timestamp? = null,
    @get:PropertyName("isCompleted") val isCompleted: Boolean = false,
    @get:PropertyName("completedAt") val completedAt: Timestamp? = null
) {
    val progress: Float
        get() {
            if (exercises.isEmpty()) return 0f
            val completed = exercises.count { it.isCompleted }
            return completed.toFloat() / exercises.size
        }
}