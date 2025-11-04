
package com.gymway.workout.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.gymway.workout.data.model.WorkoutPlan
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkoutRemoteDataSource @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val workoutPlansCollection = db.collection("workout_plans")

    suspend fun getWorkoutPlans(): List<WorkoutPlan> {
        println("🌐 [RemoteDataSource] شروع دریافت برنامه‌ها از Firebase")
        return try {
            val snapshot = workoutPlansCollection.get().await()
            println("📡 [RemoteDataSource] snapshot دریافت شد - ${snapshot.documents.size} سند")

            val plans = snapshot.documents.mapNotNull { document ->
                println("📄 [RemoteDataSource] پردازش سند: ${document.id}")
                val plan = document.toObject(WorkoutPlan::class.java)?.copy(id = document.id)
                if (plan == null) {
                    println("❌ [RemoteDataSource] سند ${document.id} تبدیل نشد")
                }
                plan
            }
            println("✅ [RemoteDataSource] ${plans.size} برنامه از Firebase دریافت شد")
            plans.forEach { plan ->
                println("   📋 ${plan.title}: ${plan.exercises.size} تمرین")
            }
            plans
        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در دریافت از Firebase: ${e.message}")
            e.printStackTrace()
            throw Exception("خطا در دریافت داده از سرور: ${e.message}")
        }
    }

    // دریافت برنامه‌های یک مربی خاص
    suspend fun getWorkoutPlansByCoach(coachId: String): List<WorkoutPlan> {
        println("🌐 [RemoteDataSource] دریافت برنامه‌های مربی: $coachId")
        return try {
            val snapshot = workoutPlansCollection
                .whereEqualTo("createdBy", coachId)
                .get()
                .await()

            println("📡 [RemoteDataSource] ${snapshot.documents.size} برنامه برای مربی $coachId")

            val plans = snapshot.documents.mapNotNull { document ->
                document.toObject(WorkoutPlan::class.java)?.copy(id = document.id)
            }
            println("✅ [RemoteDataSource] ${plans.size} برنامه برای مربی دریافت شد")
            plans
        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در دریافت برنامه‌های مربی: ${e.message}")
            throw e
        }
    }

    // ذخیره برنامه جدید
    suspend fun saveWorkoutPlan(workoutPlan: WorkoutPlan) {
        println("🌐 [RemoteDataSource] ذخیره برنامه جدید: ${workoutPlan.title}")
        try {
            workoutPlansCollection.document(workoutPlan.id)
                .set(workoutPlan)
                .await()
            println("✅ [RemoteDataSource] برنامه ${workoutPlan.title} در Firebase ذخیره شد")
        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در ذخیره برنامه: ${e.message}")
            throw e
        }
    }

    suspend fun toggleExerciseCompletion(workoutId: String, exerciseId: String, isCompleted: Boolean) {
        println("🌐 [RemoteDataSource] شروع همگام‌سازی تمرین: $exerciseId -> $isCompleted")
        try {
            // دریافت برنامه فعلی
            println("🔍 [RemoteDataSource] دریافت برنامه $workoutId از Firebase")
            val planDoc = workoutPlansCollection.document(workoutId).get().await()
            val plan = planDoc.toObject(WorkoutPlan::class.java)

            if (plan == null) {
                println("❌ [RemoteDataSource] برنامه $workoutId یافت نشد")
                throw Exception("برنامه یافت نشد")
            }

            println("📋 [RemoteDataSource] برنامه پیدا شد: ${plan.title}")

            // آپدیت تمرین
            println("🔄 [RemoteDataSource] آپدیت لیست تمرین‌ها")
            val updatedExercises = plan.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    println("✅ [RemoteDataSource] تمرین $exerciseId آپدیت شد: $isCompleted")
                    exercise.copy(isCompleted = isCompleted)
                } else exercise
            }

            // ذخیره در فایربیس
            println("💾 [RemoteDataSource] ذخیره در Firebase")
            workoutPlansCollection.document(workoutId)
                .update("exercises", updatedExercises)
                .await()

            println("✅ [RemoteDataSource] همگام‌سازی با Firebase موفق بود")

        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در همگام‌سازی با Firebase: ${e.message}")
            e.printStackTrace()
            throw Exception("خطا در همگام‌سازی با سرور: ${e.message}")
        }
    }

    suspend fun markAllExercisesCompleted(workoutId: String) {
        println("🌐 [RemoteDataSource] تکمیل همه تمرین‌ها در Firebase: $workoutId")
        try {
            val planDoc = workoutPlansCollection.document(workoutId).get().await()
            val plan = planDoc.toObject(WorkoutPlan::class.java)
                ?: throw Exception("برنامه یافت نشد")

            println("📋 [RemoteDataSource] برنامه پیدا شد: ${plan.title}")

            val updatedExercises = plan.exercises.map { exercise ->
                println("✅ [RemoteDataSource] تمرین ${exercise.name} تکمیل شد")
                exercise.copy(isCompleted = true)
            }

            workoutPlansCollection.document(workoutId)
                .update("exercises", updatedExercises)
                .await()

            println("✅ [RemoteDataSource] تکمیل همه تمرین‌ها در Firebase موفق بود")

        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در تکمیل تمرین‌ها در Firebase: ${e.message}")
            e.printStackTrace()
            throw Exception("خطا در تکمیل تمرین‌ها در سرور: ${e.message}")
        }
    }

    // ایجاد داده‌های تستی در Firebase
    suspend fun createSampleWorkoutPlans() {
        println("🎯 [RemoteDataSource] شروع ایجاد داده‌های تستی در Firebase")
        try {
            val samplePlans = listOf(
                WorkoutPlan(
                    id = "plan_1",
                    title = "تمرین روز اول - سینه و پشت بازو",
                    description = "تمرینات قدرتی برای سینه و پشت بازو",
                    exercises = listOf(
                        com.gymway.workout.data.model.Exercise(id = "1-1", name = "پرس سینه", sets = 3, reps = 10),
                        com.gymway.workout.data.model.Exercise(id = "1-2", name = "قفسه سینه", sets = 3, reps = 12),
                        com.gymway.workout.data.model.Exercise(id = "1-3", name = "پشت بازو سیمکش", sets = 3, reps = 15)
                    )
                ),
                WorkoutPlan(
                    id = "plan_2",
                    title = "تمرین روز دوم - پا و شکم",
                    description = "تمرینات پایین تنه و core",
                    exercises = listOf(
                        com.gymway.workout.data.model.Exercise(id = "2-1", name = "اسکات", sets = 4, reps = 8),
                        com.gymway.workout.data.model.Exercise(id = "2-2", name = "پرس پا", sets = 3, reps = 10),
                        com.gymway.workout.data.model.Exercise(id = "2-3", name = "درازونشست", sets = 3, reps = 20)
                    )
                )
            )

            samplePlans.forEach { plan ->
                println("💾 [RemoteDataSource] ذخیره برنامه: ${plan.title}")
                workoutPlansCollection.document(plan.id)
                    .set(plan)
                    .await()
                println("✅ [RemoteDataSource] برنامه ${plan.title} ذخیره شد")
            }

            println("🎉 [RemoteDataSource] داده‌های تستی در Firebase ایجاد شدند")

        } catch (e: Exception) {
            println("💥 [RemoteDataSource] خطا در ایجاد داده‌های تستی: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
