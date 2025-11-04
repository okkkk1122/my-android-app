package com.gymway.workout.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gymway.workout.data.local.WorkoutDatabase
import com.gymway.workout.data.local.WorkoutLocalDataSource
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import com.gymway.workout.repository.CoachRepository
import com.gymway.workout.repository.WorkoutRepository

private const val TAG = "ViewModelFactory"

class WorkoutViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d(TAG, "ایجاد ViewModel برای: ${modelClass.simpleName}")

        return when {
            modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> {
                Log.d(TAG, "ایجاد WorkoutViewModel")
                val database = WorkoutDatabase.getInstance(context)
                val localDataSource = WorkoutLocalDataSource(database.workoutDao())
                val remoteDataSource = WorkoutRemoteDataSource()
                val repository = WorkoutRepository(localDataSource, remoteDataSource)
                WorkoutViewModel(repository) as T
            }

            modelClass.isAssignableFrom(CoachViewModel::class.java) -> {
                Log.d(TAG, "ایجاد CoachViewModel")
                val remoteDataSource = WorkoutRemoteDataSource()
                val repository = CoachRepository(remoteDataSource)
                CoachViewModel(repository) as T
            }

            else -> {
                Log.e(TAG, "کلاس ViewModel ناشناخته: ${modelClass.name}")
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}