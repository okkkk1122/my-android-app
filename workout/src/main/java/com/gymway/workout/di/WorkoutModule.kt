package com.gymway.workout.di

import android.content.Context
import com.gymway.workout.data.local.WorkoutDatabase
import com.gymway.workout.data.local.WorkoutLocalDataSource
import com.gymway.workout.data.remote.WorkoutRemoteDataSource
import com.gymway.workout.repository.WorkoutRepository
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
        return WorkoutDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutLocalDataSource(database: WorkoutDatabase): WorkoutLocalDataSource {
        return WorkoutLocalDataSource(database.workoutDao())
    }

    @Provides
    @Singleton
    fun provideWorkoutRemoteDataSource(): WorkoutRemoteDataSource {
        return WorkoutRemoteDataSource()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        localDataSource: WorkoutLocalDataSource,
        remoteDataSource: WorkoutRemoteDataSource
    ): WorkoutRepository {
        return WorkoutRepository(localDataSource, remoteDataSource)
    }
}