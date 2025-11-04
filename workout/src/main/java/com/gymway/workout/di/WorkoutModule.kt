// workout/di/WorkoutModule.kt
package com.gymway.workout.di

import android.content.Context
import com.gymway.workout.data.local.WorkoutDatabase
import com.gymway.workout.data.local.WorkoutLocalDataSource
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import com.gymway.workout.repository.CoachRepository
import com.gymway.workout.repository.WorkoutRepository
import com.gymway.workout.viewmodel.CoachViewModel
import com.gymway.workout.viewmodel.WorkoutViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkoutModule {

    @Provides
    @Singleton
    fun provideWorkoutDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        println("🏗️ [WorkoutModule] ارائه WorkoutDatabase")
        return WorkoutDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutLocalDataSource(database: WorkoutDatabase): WorkoutLocalDataSource {
        println("💾 [WorkoutModule] ارائه WorkoutLocalDataSource")
        return WorkoutLocalDataSource(database.workoutDao())
    }

    @Provides
    @Singleton
    fun provideWorkoutRemoteDataSource(): WorkoutRemoteDataSource {
        println("🌐 [WorkoutModule] ارائه WorkoutRemoteDataSource")
        return WorkoutRemoteDataSource()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        localDataSource: WorkoutLocalDataSource,
        remoteDataSource: WorkoutRemoteDataSource
    ): WorkoutRepository {
        println("🔁 [WorkoutModule] ارائه WorkoutRepository")
        return WorkoutRepository(localDataSource, remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideCoachRepository(
        remoteDataSource: WorkoutRemoteDataSource
    ): CoachRepository {
        println("👥 [WorkoutModule] ارائه CoachRepository")
        return CoachRepository(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideWorkoutViewModel(repository: WorkoutRepository): WorkoutViewModel {
        println("🧠 [WorkoutModule] ارائه WorkoutViewModel")
        return WorkoutViewModel(repository)
    }

    @Provides
    @Singleton
    fun provideCoachViewModel(repository: CoachRepository): CoachViewModel {
        println("🧠 [WorkoutModule] ارائه CoachViewModel")
        return CoachViewModel(repository)
    }
}