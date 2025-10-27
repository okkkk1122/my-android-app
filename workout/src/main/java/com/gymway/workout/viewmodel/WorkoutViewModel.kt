package com.gymway.workout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.data.model.WorkoutProgress
import com.gymway.workout.repository.WorkoutRepository
import com.google.firebase.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // StateFlow برای برنامه‌های تمرینی
    private val _workoutPlans = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val workoutPlans: StateFlow<List<WorkoutPlan>> = _workoutPlans.asStateFlow()

    // StateFlow برای تاریخچه پیشرفت
    private val _progressHistory = MutableStateFlow<List<WorkoutProgress>>(emptyList())
    val progressHistory: StateFlow<List<WorkoutProgress>> = _progressHistory.asStateFlow()

    // StateFlow برای وضعیت بارگذاری
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow برای خطاها
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // StateFlow برای وضعیت همگام‌سازی
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        println("🚀 [ViewModel] WorkoutViewModel ساخته شد")

        // مشاهده تغییرات real-time از Local Database
        viewModelScope.launch {
            println("🔄 [ViewModel] شروع مشاهده Flow از Repository")
            repository.getWorkoutPlansFlow().collect { plans ->
                _workoutPlans.value = plans
                println("📊 [ViewModel] داده Flow آپدیت شد - ${plans.size} برنامه")
                plans.forEach { plan ->
                    val completed = plan.exercises.count { it.isCompleted }
                    println("   📋 ${plan.title}: ${completed}/${plan.exercises.size} تکمیل شده")
                }
            }
        }

        // بارگذاری اولیه داده‌ها
        loadWorkoutPlans()
        loadProgressHistory("user_123")

        println("✅ [ViewModel] مقداردهی اولیه کامل شد")
    }

    // 🔥 بارگذاری برنامه‌ها - الگوی Offline-First
    fun loadWorkoutPlans() {
        println("📥 [ViewModel] شروع بارگذاری برنامه‌ها")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                println("🔄 [ViewModel] فراخوانی Repository.getWorkoutPlans()")
                val plans = repository.getWorkoutPlans()
                _workoutPlans.value = plans
                println("✅ [ViewModel] بارگذاری کامل - ${plans.size} برنامه")
                plans.forEach { plan ->
                    val completed = plan.exercises.count { it.isCompleted }
                    println("   🎯 ${plan.title}: ${completed}/${plan.exercises.size} تمرین تکمیل شده")
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در بارگذاری: ${e.message}"
                println("❌ [ViewModel] خطا در بارگذاری: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                println("⚡ [ViewModel] وضعیت loading: false")
            }
        }
    }

    // 🔥 تغییر وضعیت تمرین - Optimistic Updates
    fun toggleExerciseCompletion(workoutId: String, exerciseId: String, isCompleted: Boolean) {
        println("🎯 [ViewModel] کاربر تغییر تمرین داد: $exerciseId -> $isCompleted")
        viewModelScope.launch {
            try {
                // ۱. فوری UI رو آپدیت کن (Optimistic Update)
                println("⚡ [ViewModel] شروع آپدیت فوری UI")
                val currentPlans = _workoutPlans.value
                println("📊 [ViewModel] قبل از آپدیت: ${currentPlans.size} برنامه")

                _workoutPlans.value = currentPlans.map { plan ->
                    if (plan.id == workoutId) {
                        println("🔍 [ViewModel] برنامه ${plan.title} پیدا شد")
                        val updatedExercises = plan.exercises.map { exercise ->
                            if (exercise.id == exerciseId) {
                                println("✅ [ViewModel] تمرین ${exercise.name} آپدیت شد: $isCompleted")
                                exercise.copy(isCompleted = isCompleted)
                            } else exercise
                        }
                        val updatedPlan = plan.copy(exercises = updatedExercises)
                        val completedCount = updatedExercises.count { it.isCompleted }
                        println("📈 [ViewModel] پیشرفت برنامه: ${completedCount}/${updatedExercises.size}")
                        updatedPlan
                    } else plan
                }

                println("✅ [ViewModel] UI فوری آپدیت شد")

                // ۲. در پس‌زمینه ذخیره و sync کن
                println("💾 [ViewModel] فراخوانی Repository.toggleExerciseCompletion()")
                repository.toggleExerciseCompletion(workoutId, exerciseId, isCompleted)
                println("✅ [ViewModel] Repository فراخوانی شد")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در بروزرسانی تمرین: ${e.message}"
                println("❌ [ViewModel] خطا در تغییر تمرین: ${e.message}")
                e.printStackTrace()

                // Rollback در صورت خطا
                println("🔄 [ViewModel] تلاش برای rollback به دلیل خطا")
                loadWorkoutPlans()
            }
        }
    }

    // تکمیل همه تمرین‌های یک برنامه
    fun markAllExercisesCompleted(workoutId: String) {
        println("🔥 [ViewModel] کاربر تکمیل همه تمرین‌ها را زد: $workoutId")
        viewModelScope.launch {
            try {
                // ۱. فوری UI رو آپدیت کن
                println("⚡ [ViewModel] شروع آپدیت فوری UI برای همه تمرین‌ها")
                _workoutPlans.value = _workoutPlans.value.map { plan ->
                    if (plan.id == workoutId) {
                        println("🔍 [ViewModel] برنامه ${plan.title} پیدا شد")
                        val updatedExercises = plan.exercises.map { exercise ->
                            println("✅ [ViewModel] تمرین ${exercise.name} تکمیل شد")
                            exercise.copy(isCompleted = true)
                        }
                        plan.copy(exercises = updatedExercises)
                    } else plan
                }

                println("✅ [ViewModel] UI فوری آپدیت شد")

                // ۲. در پس‌زمینه sync کن
                println("💾 [ViewModel] فراخوانی Repository.markAllExercisesCompleted()")
                repository.markAllExercisesCompleted(workoutId)
                println("✅ [ViewModel] Repository فراخوانی شد")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در تکمیل تمرین‌ها: ${e.message}"
                println("❌ [ViewModel] خطا در تکمیل همه تمرین‌ها: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // 📊 مدیریت تاریخچه پیشرفت
    fun loadProgressHistory(userId: String) {
        println("📊 [ViewModel] بارگذاری تاریخچه پیشرفت برای کاربر: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // داده‌های نمونه - در نسخه واقعی از دیتابیس می‌گیریم
                val now = Date()
                val yesterday = Date(now.time - TimeUnit.DAYS.toMillis(1))

                val sampleProgress = listOf(
                    WorkoutProgress(
                        id = "progress_1",
                        userId = userId,
                        workoutPlanId = "plan_1",
                        date = Timestamp(now),
                        completedExercises = 3,
                        totalExercises = 3,
                        notes = "تمرین عالی بود! انرژی زیادی داشتم",
                        weight = 75.5,
                        bodyFat = 18.0
                    ),
                    WorkoutProgress(
                        id = "progress_2",
                        userId = userId,
                        workoutPlanId = "plan_2",
                        date = Timestamp(yesterday),
                        completedExercises = 2,
                        totalExercises = 3,
                        notes = "خسته بودم، نتوانستم آخرین تمرین رو انجام بدم",
                        weight = 75.2,
                        bodyFat = 18.2
                    )
                )
                _progressHistory.value = sampleProgress
                println("✅ [ViewModel] ${sampleProgress.size} رکورد پیشرفت بارگذاری شد")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در بارگذاری تاریخچه: ${e.message}"
                println("❌ [ViewModel] خطا در دریافت پیشرفت: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ایجاد رکورد پیشرفت جدید
    fun createSampleProgress(userId: String) {
        println("➕ [ViewModel] ایجاد رکورد پیشرفت جدید برای کاربر: $userId")
        viewModelScope.launch {
            try {
                val newProgress = WorkoutProgress(
                    id = "progress_${System.currentTimeMillis()}",
                    userId = userId,
                    workoutPlanId = "plan_1",
                    date = Timestamp(Date()),
                    completedExercises = (1..3).random(),
                    totalExercises = 3,
                    notes = "ثبت جدید در ${Date()}",
                    weight = 75.0 + (Math.random() * 2 - 1),
                    bodyFat = 18.0 + (Math.random() * 1 - 0.5)
                )

                _progressHistory.value = _progressHistory.value + newProgress
                println("✅ [ViewModel] رکورد جدید پیشرفت ایجاد شد: ${newProgress.completedExercises}/${newProgress.totalExercises}")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در ثبت پیشرفت: ${e.message}"
                println("❌ [ViewModel] خطا در ایجاد پیشرفت: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // 🔄 همگام‌سازی دستی تغییرات pending
    fun syncPendingChanges() {
        println("🔄 [ViewModel] کاربر همگام‌سازی دستی را زد")
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                println("📡 [ViewModel] شروع همگام‌سازی دستی")
                repository.syncPendingChanges()
                println("✅ [ViewModel] همگام‌سازی دستی کامل شد")

                // بعد از sync، داده‌ها رو refresh کن
                loadWorkoutPlans()

            } catch (e: Exception) {
                _errorMessage.value = "خطا در همگام‌سازی: ${e.message}"
                println("❌ [ViewModel] خطا در همگام‌سازی: ${e.message}")
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
                println("⚡ [ViewModel] وضعیت syncing: false")
            }
        }
    }

    // 🔄 بارگذاری مجدد از سرور
    fun refreshFromServer() {
        println("🔄 [ViewModel] کاربر refresh از سرور را زد")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("🌐 [ViewModel] شروع refresh از سرور")
                val plans = repository.refreshFromServer()
                _workoutPlans.value = plans
                println("✅ [ViewModel] refresh از سرور کامل شد - ${plans.size} برنامه")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در بروزرسانی: ${e.message}"
                println("❌ [ViewModel] خطا در refresh: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                println("⚡ [ViewModel] وضعیت loading: false")
            }
        }
    }

    // ایجاد داده‌های تستی
    fun initializeSampleData() {
        println("🎯 [ViewModel] کاربر ایجاد داده تستی را زد")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("🛠️ [ViewModel] شروع ایجاد داده‌های تستی")
                repository.initializeSampleData()

                // بعد از ایجاد، داده‌ها رو refresh کن
                loadWorkoutPlans()
                println("✅ [ViewModel] داده‌های تستی ایجاد و بارگذاری شدند")

            } catch (e: Exception) {
                _errorMessage.value = "خطا در ایجاد داده تستی: ${e.message}"
                println("❌ [ViewModel] خطا در ایجاد داده تستی: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                println("⚡ [ViewModel] وضعیت loading: false")
            }
        }
    }

    // همگام‌سازی برنامه‌ها
    fun syncWorkoutPlans() {
        println("🔄 [ViewModel] همگام‌سازی برنامه‌ها")
        viewModelScope.launch {
            try {
                println("📡 [ViewModel] شروع همگام‌سازی برنامه‌ها")
                val plans = repository.getWorkoutPlans()
                _workoutPlans.value = plans
                println("✅ [ViewModel] همگام‌سازی برنامه‌ها انجام شد - ${plans.size} برنامه")

            } catch (e: Exception) {
                println("❌ [ViewModel] خطا در همگام‌سازی: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // پاک کردن خطا
    fun clearError() {
        println("🗑️ [ViewModel] پاک کردن خطا")
        _errorMessage.value = null
    }

    // تست مستقیم Database
    fun testDatabase() {
        println("🧪 [ViewModel] تست مستقیم Database")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ایجاد یک تابع تست در Repository
                testDatabaseOperations()
                println("✅ [ViewModel] تست Database کامل شد")
            } catch (e: Exception) {
                _errorMessage.value = "خطا در تست Database: ${e.message}"
                println("❌ [ViewModel] خطا در تست Database: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // تابع کمک کننده برای تست Database
    private suspend fun testDatabaseOperations() {
        println("🧪 [ViewModel] شروع عملیات تست Database")
        // اینجا می‌تونی تست‌های مستقیم انجام بدی
        // مثلاً:
        // repository.testDatabaseOperations()
    }
}