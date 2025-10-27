package com.gymway.workout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gymway.workout.data.model.Exercise
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.data.local.converter.ExerciseListConverter

@Entity(tableName = "workout_plans")
@TypeConverters(ExerciseListConverter::class)
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val exercises: List<Exercise>,
    val duration: Int = 60,
    val difficulty: String = "beginner",
    val createdBy: String = "",
    val assignedTo: String = "",
    val isCompleted: Boolean = false,
    val isSynced: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val serverTimestamp: Long? = null
) {
    fun toWorkoutPlan(): WorkoutPlan {
        return WorkoutPlan(
            id = id,
            title = title,
            description = description,
            exercises = exercises,
            duration = duration,
            difficulty = difficulty,
            createdBy = createdBy,
            assignedTo = assignedTo,
            isCompleted = isCompleted
        )
    }

    companion object {
        fun fromWorkoutPlan(plan: WorkoutPlan): WorkoutPlanEntity {
            return WorkoutPlanEntity(
                id = plan.id,
                title = plan.title,
                description = plan.description,
                exercises = plan.exercises,
                duration = plan.duration,
                difficulty = plan.difficulty,
                createdBy = plan.createdBy,
                assignedTo = plan.assignedTo,
                isCompleted = plan.isCompleted,
                isSynced = true
            )
        }
    }
}