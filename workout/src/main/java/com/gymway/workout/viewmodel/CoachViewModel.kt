package com.gymway.workout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.gymway.workout.data.model.Coach
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.repository.CoachRepository
import javax.inject.Inject

class CoachViewModel @Inject constructor(
    private val coachRepository: CoachRepository
) : ViewModel() {

    private val _coaches = MutableStateFlow<List<Coach>>(emptyList())
    val coaches: StateFlow<List<Coach>> = _coaches.asStateFlow()

    private val _assignedWorkouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val assignedWorkouts: StateFlow<List<WorkoutPlan>> = _assignedWorkouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        println("🚀 [CoachViewModel] CoachViewModel ساخته شد")
        loadCoaches()
    }

    // بارگذاری لیست مربیان
    fun loadCoaches() {
        println("👥 [CoachViewModel] بارگذاری لیست مربیان")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val coachesList = coachRepository.getCoaches()
                _coaches.value = coachesList
                println("✅ [CoachViewModel] ${coachesList.size} مربی بارگذاری شد")
            } catch (e: Exception) {
                _errorMessage.value = "خطا در بارگذاری مربیان: ${e.message}"
                println("❌ [CoachViewModel] خطا در بارگذاری مربیان: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // بارگذاری برنامه‌های اختصاص داده شده
    fun loadAssignedWorkouts(coachId: String) {
        println("📋 [CoachViewModel] بارگذاری برنامه‌های مربی: $coachId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val workouts = coachRepository.getAssignedWorkouts(coachId)
                _assignedWorkouts.value = workouts
                println("✅ [CoachViewModel] ${workouts.size} برنامه بارگذاری شد")
            } catch (e: Exception) {
                _errorMessage.value = "خطا در بارگذاری برنامه‌ها: ${e.message}"
                println("❌ [CoachViewModel] خطا در بارگذاری برنامه‌ها: ${e.message}")

                // داده نمونه در صورت خطا
                val sampleWorkouts = listOf(
                    WorkoutPlan(
                        id = "sample_1",
                        title = "برنامه نمونه فیتنس",
                        description = "برنامه تمرینی نمونه",
                        createdBy = coachId,
                        assignedTo = "user_123"
                    )
                )
                _assignedWorkouts.value = sampleWorkouts
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ایجاد برنامه جدید - با مدیریت خطا
    fun createWorkoutPlan(workoutPlan: WorkoutPlan) {
        println("🆕 [CoachViewModel] ایجاد برنامه جدید: ${workoutPlan.title}")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                coachRepository.createWorkoutPlan(workoutPlan)
                println("✅ [CoachViewModel] برنامه جدید ایجاد شد")

                // آپدیت لیست
                loadAssignedWorkouts(workoutPlan.createdBy)

            } catch (e: Exception) {
                _errorMessage.value = "خطا در ایجاد برنامه: ${e.message}"
                println("❌ [CoachViewModel] خطا در ایجاد برنامه: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // اختصاص برنامه به ورزشکار
    fun assignWorkoutToAthlete(workoutPlan: WorkoutPlan, athleteId: String) {
        println("🎯 [CoachViewModel] اختصاص برنامه به ورزشکار: ${workoutPlan.title}")
        viewModelScope.launch {
            try {
                coachRepository.assignWorkoutToAthlete(workoutPlan, athleteId)
                println("✅ [CoachViewModel] برنامه با موفقیت اختصاص داده شد")
            } catch (e: Exception) {
                _errorMessage.value = "خطا در اختصاص برنامه: ${e.message}"
                println("❌ [CoachViewModel] خطا در اختصاص برنامه: ${e.message}")
            }
        }
    }

    // پاک کردن خطا
    fun clearError() {
        _errorMessage.value = null
    }
}