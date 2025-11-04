
package com.gymway.workout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gymway.workout.data.local.converter.ExerciseListConverter
import com.gymway.workout.data.model.Exercise
import com.gymway.workout.data.model.WorkoutPlan

@Entity(tableName = "workout_plans")
@TypeConverters(ExerciseListConverter::class)
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val exercises: List<Exercise>,
    val duration: Int,
    val difficulty: String,
    val createdBy: String,
    val assignedTo: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // حذف isCompleted از اینجا
    fun toWorkoutPlan(): WorkoutPlan {
        return WorkoutPlan(
            id = id,
            title = title,
            description = description,
            exercises = exercises,
            duration = duration,
            difficulty = difficulty,
            createdBy = createdBy,
            assignedTo = assignedTo
        )
    }

    companion object {
        fun fromWorkoutPlan(workoutPlan: WorkoutPlan): WorkoutPlanEntity {
            return WorkoutPlanEntity(
                id = workoutPlan.id,
                title = workoutPlan.title,
                description = workoutPlan.description,
                exercises = workoutPlan.exercises,
                duration = workoutPlan.duration,
                difficulty = workoutPlan.difficulty,
                createdBy = workoutPlan.createdBy,
                assignedTo = workoutPlan.assignedTo
            )
        }
    }
}
