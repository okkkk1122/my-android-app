package com.gymway.workout.repository

import com.gymway.workout.data.local.WorkoutLocalDataSource
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import com.gymway.workout.data.model.WorkoutPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val localDataSource: WorkoutLocalDataSource,
    private val remoteDataSource: WorkoutRemoteDataSource
) {

    // لاگ: دریافت برنامه‌ها - الگوی Offline-First
    suspend fun getWorkoutPlans(): List<WorkoutPlan> {
        println("🏠 [Repository] شروع دریافت برنامه‌ها - الگوی Offline-First")

        return try {
            // ۱. اول از Local بگیر
            println("📱 [Repository] تلاش برای دریافت از Local")
            val localPlans = getWorkoutPlansFromLocal()

            if (localPlans.isNotEmpty()) {
                println("✅ [Repository] استفاده از داده Local - ${localPlans.size} برنامه")
                return localPlans
            }

            // ۲. اگر Local خالی بود، از Remote بگیر
            println("🌐 [Repository] داده Local خالی، دریافت از Firebase")
            val remotePlans = remoteDataSource.getWorkoutPlans()

            // ۳. ذخیره در Local برای دفعات بعد
            println("💾 [Repository] ذخیره داده Firebase در Local")
            localDataSource.saveWorkoutPlans(remotePlans)
            println("✅ [Repository] داده Firebase در Local ذخیره شد")

            remotePlans

        } catch (e: Exception) {
            // ۴. حتی اگر خطای شبکه هم بود، از Local برگردون
            println("⚠️ [Repository] خطای شبکه، استفاده از داده Local: ${e.message}")
            getWorkoutPlansFromLocal()
        }
    }

    // Flow برای مشاهده تغییرات real-time
    fun getWorkoutPlansFlow(): Flow<List<WorkoutPlan>> {
        println("🔄 [Repository] دریافت Flow از LocalDataSource")
        return localDataSource.getWorkoutPlans()
    }

    // 🔥 لاگ: تغییر وضعیت تمرین
    suspend fun toggleExerciseCompletion(workoutId: String, exerciseId: String, isCompleted: Boolean) {
        println("🎯 [Repository] شروع تغییر وضعیت تمرین: $exerciseId -> $isCompleted")

        // ۱. فوری در Local ذخیره کن
        println("💾 [Repository] ذخیره فوری در Local")
        localDataSource.updateExerciseCompletion(exerciseId, workoutId, isCompleted)
        println("✅ [Repository] وضعیت تمرین در Local ذخیره شد")

        // ۲. در پس‌زمینه با Firebase sync کن
        println("🌐 [Repository] شروع همگام‌سازی با Firebase در پس‌زمینه")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                remoteDataSource.toggleExerciseCompletion(workoutId, exerciseId, isCompleted)
                localDataSource.markExerciseAsSynced(exerciseId)
                println("✅ [Repository] همگام‌سازی با Firebase موفق: $exerciseId")
            } catch (e: Exception) {
                println("⚠️ [Repository] خطا در همگام‌سازی با Firebase: ${e.message}")
            }
        }
    }

    // لاگ: تکمیل همه تمرین‌ها
    suspend fun markAllExercisesCompleted(workoutId: String) {
        println("🔥 [Repository] تکمیل همه تمرین‌های برنامه: $workoutId")

        // ۱. ابتدا در Local آپدیت کن
        println("💾 [Repository] آپدیت Local")
        val plans = getWorkoutPlansFromLocal()
        val plan = plans.find { it.id == workoutId }
        plan?.exercises?.forEach { exercise ->
            localDataSource.updateExerciseCompletion(exercise.id, workoutId, true)
        }
        println("✅ [Repository] تکمیل همه تمرین‌ها در Local ذخیره شد")

        // ۲. در پس‌زمینه با Firebase sync کن
        println("🌐 [Repository] همگام‌سازی با Firebase در پس‌زمینه")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                remoteDataSource.markAllExercisesCompleted(workoutId)
                plan?.exercises?.forEach { exercise ->
                    localDataSource.markExerciseAsSynced(exercise.id)
                }
                println("✅ [Repository] همه تمرین‌ها با Firebase همگام شدند")
            } catch (e: Exception) {
                println("⚠️ [Repository] خطا در همگام‌سازی همه تمرین‌ها: ${e.message}")
            }
        }
    }

    // لاگ: همگام‌سازی دستی
    suspend fun syncPendingChanges() {
        println("🔄 [Repository] شروع همگام‌سازی دستی تغییرات pending")
        try {
            val pendingExercises = localDataSource.getPendingSyncExercises()
            if (pendingExercises.isEmpty()) {
                println("✅ [Repository] هیچ تغییر pending برای همگام‌سازی وجود ندارد")
                return
            }

            println("📋 [Repository] ${pendingExercises.size} تغییر pending پیدا شد")

            pendingExercises.forEach { exercise ->
                try {
                    println("🌐 [Repository] همگام‌سازی تمرین: ${exercise.exerciseId}")
                    remoteDataSource.toggleExerciseCompletion(
                        exercise.workoutPlanId,
                        exercise.exerciseId,
                        exercise.isCompleted
                    )
                    localDataSource.markExerciseAsSynced(exercise.exerciseId)
                    println("✅ [Repository] همگام‌سازی تمرین موفق: ${exercise.exerciseId}")
                } catch (e: Exception) {
                    println("❌ [Repository] خطا در همگام‌سازی ${exercise.exerciseId}: ${e.message}")
                }
            }

            println("🎉 [Repository] همگام‌سازی کامل شد")

        } catch (e: Exception) {
            println("💥 [Repository] خطا در همگام‌سازی کلی: ${e.message}")
        }
    }

    // لاگ: بارگذاری مجدد از سرور
    suspend fun refreshFromServer(): List<WorkoutPlan> {
        println("🔄 [Repository] بارگذاری مجدد از سرور")
        return try {
            val plans = remoteDataSource.getWorkoutPlans()
            localDataSource.saveWorkoutPlans(plans)
            println("✅ [Repository] داده از Firebase refresh شد")
            plans
        } catch (e: Exception) {
            println("⚠️ [Repository] خطا در refresh از Firebase: ${e.message}")
            getWorkoutPlansFromLocal()
        }
    }

    // لاگ: ایجاد داده‌های تستی
    suspend fun initializeSampleData() {
        println("🎯 [Repository] شروع ایجاد داده‌های تستی")
        try {
            remoteDataSource.createSampleWorkoutPlans()
            val plans = remoteDataSource.getWorkoutPlans()
            localDataSource.saveWorkoutPlans(plans)
            println("✅ [Repository] داده‌های تستی در Local و Firebase ایجاد شدند")
        } catch (e: Exception) {
            println("❌ [Repository] خطا در ایجاد داده‌های تستی: ${e.message}")
        }
    }

    // کمک کننده: گرفتن داده از Local
    private suspend fun getWorkoutPlansFromLocal(): List<WorkoutPlan> {
        return try {
            localDataSource.getWorkoutPlansFromLocal()
        } catch (e: Exception) {
            println("❌ [Repository] خطا در دریافت از Local: ${e.message}")
            emptyList()
        }
    }
}