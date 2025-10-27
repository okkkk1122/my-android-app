package com.gymway.workout.data.model

import com.google.firebase.firestore.PropertyName

data class Exercise(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("name") val name: String = "",
    @get:PropertyName("sets") val sets: Int = 3,
    @get:PropertyName("reps") val reps: Int = 10,
    @get:PropertyName("weight") val weight: Double? = null,
    @get:PropertyName("restTime") val restTime: Int = 60,
    @get:PropertyName("notes") val notes: String = "",
    @get:PropertyName("muscleGroup") val muscleGroup: String = "",
    @get:PropertyName("isCompleted") val isCompleted: Boolean = false
) {
    fun getDisplayText(): String {
        return if (weight != null) {
            "$sets x $reps - ${weight}kg"
        } else {
            "$sets x $reps"
        }
    }
}