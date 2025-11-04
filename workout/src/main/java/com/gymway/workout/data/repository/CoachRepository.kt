package com.gymway.workout.repository

import com.gymway.workout.data.model.Coach
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachRepository @Inject constructor(
    private val remoteDataSource: WorkoutRemoteDataSource
) {
    private val _coaches = MutableStateFlow<List<Coach>>(emptyList())
    val coaches: Flow<List<Coach>> = _coaches

    private val _assignedWorkouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val assignedWorkouts: Flow<List<WorkoutPlan>> = _assignedWorkouts

    // دریافت لیست مربیان
    suspend fun getCoaches(): List<Coach> {
        println("👥 [CoachRepository] دریافت لیست مربیان")
        val sampleCoaches = listOf(
            Coach(
                id = "coach_1",
                name = "علیرضا محمدی",
                email = "alireza@email.com",
                specialty = "بدنسازی و فیتنس",
                athletes = listOf("user_123", "user_456")
            ),
            Coach(
                id = "coach_2",
                name = "زهرا کریمی",
                email = "zahra@email.com",
                specialty = "پیلاتس و یوگا",
                athletes = listOf("user_789")
            )
        )
        _coaches.value = sampleCoaches
        return sampleCoaches
    }

    // دریافت برنامه‌های اختصاص داده شده توسط مربی
    suspend fun getAssignedWorkouts(coachId: String): List<WorkoutPlan> {
        println("📋 [CoachRepository] دریافت برنامه‌های مربی: $coachId")
        try {
            val workouts = remoteDataSource.getWorkoutPlansByCoach(coachId)
            _assignedWorkouts.value = workouts
            println("✅ [CoachRepository] ${workouts.size} برنامه از Firebase دریافت شد")
            return workouts
        } catch (e: Exception) {
            println("⚠️ [CoachRepository] خطا در دریافت از Firebase: ${e.message}")
            // داده نمونه در صورت خطا
            val sampleWorkouts = listOf(
                WorkoutPlan(
                    id = "assigned_1",
                    title = "برنامه فیتنس پیشرفته",
                    description = "برنامه ویژه برای ورزشکاران حرفه‌ای",
                    createdBy = coachId,
                    assignedTo = "user_123"
                ),
                WorkoutPlan(
                    id = "assigned_2",
                    title = "برنامه کاهش وزن",
                    description = "برنامه کاردیو و تغذیه",
                    createdBy = coachId,
                    assignedTo = "user_456"
                )
            )
            _assignedWorkouts.value = sampleWorkouts
            return sampleWorkouts
        }
    }

    // ایجاد برنامه جدید توسط مربی
    suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan) {
        println("🆕 [CoachRepository] ایجاد برنامه جدید: ${workoutPlan.title}")
        try {
            // ذخیره در Firebase
            remoteDataSource.saveWorkoutPlan(workoutPlan)
            _assignedWorkouts.value = _assignedWorkouts.value + workoutPlan
            println("✅ [CoachRepository] برنامه جدید در Firebase ایجاد شد")
        } catch (e: Exception) {
            println("❌ [CoachRepository] خطا در ایجاد برنامه: ${e.message}")
            // حتی در صورت خطا، به صورت محلی اضافه کن
            _assignedWorkouts.value = _assignedWorkouts.value + workoutPlan
            throw e
        }
    }

    // اختصاص برنامه به ورزشکار
    suspend fun assignWorkoutToAthlete(workoutPlan: WorkoutPlan, athleteId: String) {
        println("🎯 [CoachRepository] اختصاص برنامه به ورزشکار: ${workoutPlan.title} -> $athleteId")
        try {
            val assignedWorkout = workoutPlan.copy(
                id = "assigned_${System.currentTimeMillis()}",
                assignedTo = athleteId
            )
            remoteDataSource.saveWorkoutPlan(assignedWorkout)
            _assignedWorkouts.value = _assignedWorkouts.value + assignedWorkout
            println("✅ [CoachRepository] برنامه با موفقیت در Firebase ذخیره شد")
        } catch (e: Exception) {
            println("❌ [CoachRepository] خطا در ذخیره برنامه: ${e.message}")
            throw e
        }
    }
}