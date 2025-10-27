package com.gymway.workout.data.local

import com.gymway.workout.data.local.dao.WorkoutDao
import com.gymway.workout.data.local.entity.ExerciseStatusEntity
import com.gymway.workout.data.local.entity.WorkoutPlanEntity
import com.gymway.workout.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutLocalDataSource @Inject constructor(
    private val workoutDao: WorkoutDao
) {

    // لاگ: دریافت برنامه‌ها از دیتابیس
    fun getWorkoutPlans(): Flow<List<WorkoutPlan>> {
        println("📊 [LocalDataSource] دریافت برنامه‌ها از دیتابیس (Flow)")
        return workoutDao.getWorkoutPlans().map { entities ->
            val plans = entities.map { it.toWorkoutPlan() }
            println("✅ [LocalDataSource] ${plans.size} برنامه از دیتابیس دریافت شد")
            plans
        }
    }

    // لاگ: دریافت مستقیم برنامه‌ها
    suspend fun getWorkoutPlansFromLocal(): List<WorkoutPlan> {
        println("📊 [LocalDataSource] دریافت مستقیم برنامه‌ها از دیتابیس")
        return try {
            val entities = workoutDao.getAllWorkoutPlans()
            val plans = entities.map { it.toWorkoutPlan() }
            println("✅ [LocalDataSource] ${plans.size} برنامه مستقیم دریافت شد")
            plans
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در دریافت مستقیم: ${e.message}")
            emptyList()
        }
    }

    // لاگ: دریافت برنامه بر اساس ID
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlan? {
        println("📋 [LocalDataSource] دریافت برنامه با ID: $planId")
        return try {
            val entity = workoutDao.getWorkoutPlanById(planId)
            val plan = entity?.toWorkoutPlan()
            println("✅ [LocalDataSource] برنامه ${if (plan != null) "پیدا" else "پیدا نشد"}")
            plan
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در دریافت برنامه: ${e.message}")
            null
        }
    }

    // لاگ: ذخیره برنامه‌ها
    suspend fun saveWorkoutPlans(plans: List<WorkoutPlan>) {
        println("💾 [LocalDataSource] ذخیره ${plans.size} برنامه در دیتابیس")
        try {
            val entities = plans.map { WorkoutPlanEntity.fromWorkoutPlan(it) }
            workoutDao.insertWorkoutPlans(entities)
            println("✅ [LocalDataSource] ${plans.size} برنامه با موفقیت ذخیره شد")
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در ذخیره برنامه‌ها: ${e.message}")
        }
    }

    // لاگ: ذخیره یک برنامه
    suspend fun saveWorkoutPlan(plan: WorkoutPlan) {
        println("💾 [LocalDataSource] ذخیره برنامه: ${plan.title}")
        try {
            val entity = WorkoutPlanEntity.fromWorkoutPlan(plan)
            workoutDao.insertWorkoutPlan(entity)
            println("✅ [LocalDataSource] برنامه '${plan.title}' ذخیره شد")
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در ذخیره برنامه: ${e.message}")
        }
    }

    // 🔥 لاگ: تابع اصلی - ذخیره وضعیت تمرین
    suspend fun updateExerciseCompletion(exerciseId: String, workoutPlanId: String, isCompleted: Boolean) {
        println("🎯 [LocalDataSource] شروع ذخیره‌سازی تمرین: $exerciseId -> $isCompleted")

        try {
            // ۱. ذخیره وضعیت تمرین برای sync
            println("📝 [LocalDataSource] ایجاد ExerciseStatusEntity")
            val status = ExerciseStatusEntity(
                exerciseId = exerciseId,
                workoutPlanId = workoutPlanId,
                isCompleted = isCompleted,
                isSynced = false
            )

            println("💾 [LocalDataSource] ذخیره در exercise_status")
            workoutDao.insertExerciseStatus(status)
            println("✅ [LocalDataSource] وضعیت تمرین در exercise_status ذخیره شد")

            // ۲. آپدیت WorkoutPlan در دیتابیس
            println("🔍 [LocalDataSource] جستجوی برنامه با ID: $workoutPlanId")
            val currentPlanEntity = workoutDao.getWorkoutPlanById(workoutPlanId)

            if (currentPlanEntity != null) {
                println("📋 [LocalDataSource] برنامه پیدا شد: ${currentPlanEntity.title}")

                // آپدیت لیست تمرین‌ها
                println("🔄 [LocalDataSource] آپدیت لیست تمرین‌ها")
                val updatedExercises = currentPlanEntity.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        println("✅ [LocalDataSource] تمرین $exerciseId آپدیت شد")
                        exercise.copy(isCompleted = isCompleted)
                    } else exercise
                }

                // ایجاد entity آپدیت شده
                println("💾 [LocalDataSource] ایجاد WorkoutPlanEntity آپدیت شده")
                val updatedPlanEntity = currentPlanEntity.copy(exercises = updatedExercises)

                // ذخیره در دیتابیس
                println("💾 [LocalDataSource] ذخیره برنامه آپدیت شده در workout_plans")
                workoutDao.insertWorkoutPlan(updatedPlanEntity)
                println("✅ [LocalDataSource] برنامه آپدیت شده در workout_plans ذخیره شد")

            } else {
                println("❌ [LocalDataSource] برنامه با ID $workoutPlanId پیدا نشد")
            }

            println("🎉 [LocalDataSource] ذخیره‌سازی کامل شد: $exerciseId -> $isCompleted")

        } catch (e: Exception) {
            println("💥 [LocalDataSource] خطا در ذخیره‌سازی: ${e.message}")
            e.printStackTrace()
        }
    }

    // لاگ: دریافت تمرین‌های pending
    suspend fun getPendingSyncExercises(): List<ExerciseStatusEntity> {
        println("🔄 [LocalDataSource] دریافت تمرین‌های pending برای sync")
        return try {
            val pending = workoutDao.getPendingSyncExercises()
            println("✅ [LocalDataSource] ${pending.size} تمرین pending پیدا شد")
            pending
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در دریافت pending: ${e.message}")
            emptyList()
        }
    }

    // لاگ: علامت‌گذاری تمرین به عنوان synced
    suspend fun markExerciseAsSynced(exerciseId: String) {
        println("✅ [LocalDataSource] علامت‌گذاری تمرین $exerciseId به عنوان synced")
        try {
            workoutDao.markExerciseAsSynced(exerciseId)
            println("✅ [LocalDataSource] تمرین $exerciseId synced شد")
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در علامت‌گذاری: ${e.message}")
        }
    }

    // لاگ: پاک کردن داده‌ها
    suspend fun clearLocalData() {
        println("🗑️ [LocalDataSource] پاک کردن تمام داده‌های دیتابیس")
        try {
            workoutDao.deleteAllWorkoutPlans()
            println("✅ [LocalDataSource] داده‌ها پاک شدند")
        } catch (e: Exception) {
            println("❌ [LocalDataSource] خطا در پاک کردن: ${e.message}")
        }
    }
}