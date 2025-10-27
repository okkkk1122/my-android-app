package com.gymway.workout.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gymway.workout.data.local.WorkoutDatabase
import com.gymway.workout.data.local.WorkoutLocalDataSource
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import com.gymway.workout.repository.WorkoutRepository

class WorkoutViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        println("🏭 [ViewModelFactory] ایجاد ViewModel جدید")

        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            // ایجاد تمام dependencies مورد نیاز
            println("🔧 [ViewModelFactory] ایجاد Database")
            val database = WorkoutDatabase.getInstance(context)

            println("🔧 [ViewModelFactory] ایجاد LocalDataSource")
            val localDataSource = WorkoutLocalDataSource(database.workoutDao())

            println("🔧 [ViewModelFactory] ایجاد RemoteDataSource")
            val remoteDataSource = WorkoutRemoteDataSource()

            println("🔧 [ViewModelFactory] ایجاد Repository")
            val repository = WorkoutRepository(localDataSource, remoteDataSource)

            println("✅ [ViewModelFactory] WorkoutViewModel ایجاد شد")
            return WorkoutViewModel(repository) as T
        }

        println("❌ [ViewModelFactory] کلاس ViewModel ناشناخته: ${modelClass.name}")
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}