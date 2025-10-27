package com.gymway.workout.data.local.dao

import androidx.room.*
import com.gymway.workout.data.local.entity.ExerciseStatusEntity
import com.gymway.workout.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // Workout Plans
    @Query("SELECT * FROM workout_plans ORDER BY lastUpdated DESC")
    fun getWorkoutPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plans")
    suspend fun getAllWorkoutPlans(): List<WorkoutPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(plan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlans(plans: List<WorkoutPlanEntity>)

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deleteWorkoutPlan(planId: String)

    @Query("DELETE FROM workout_plans")
    suspend fun deleteAllWorkoutPlans()

    // Exercise Status
    @Query("SELECT * FROM exercise_status WHERE workoutPlanId = :workoutPlanId")
    suspend fun getExerciseStatuses(workoutPlanId: String): List<ExerciseStatusEntity>

    @Query("SELECT * FROM exercise_status WHERE isSynced = 0")
    suspend fun getPendingSyncExercises(): List<ExerciseStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseStatus(status: ExerciseStatusEntity)

    @Query("UPDATE exercise_status SET isSynced = 1 WHERE exerciseId = :exerciseId")
    suspend fun markExerciseAsSynced(exerciseId: String)

    @Query("DELETE FROM exercise_status WHERE workoutPlanId = :workoutPlanId")
    suspend fun deleteExerciseStatuses(workoutPlanId: String)
}