package com.gymway.workout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_status")
data class ExerciseStatusEntity(
    @PrimaryKey val exerciseId: String,
    val workoutPlanId: String,
    val isCompleted: Boolean,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)