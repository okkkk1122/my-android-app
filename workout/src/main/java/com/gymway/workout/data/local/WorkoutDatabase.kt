package com.gymway.workout.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.gymway.workout.data.local.dao.WorkoutDao
import com.gymway.workout.data.local.entity.ExerciseStatusEntity
import com.gymway.workout.data.local.entity.WorkoutPlanEntity

@Database(
    entities = [WorkoutPlanEntity::class, ExerciseStatusEntity::class],
    version = 2, // از 1 به 2 تغییر بده
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getInstance(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                println("🏗️ [WorkoutDatabase] ساخت instance جدید - version: 2")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration() // این خط مهمه!
                    .build()
                INSTANCE = instance
                println("✅ [WorkoutDatabase] Database ساخته شد - version: 2")
                instance
            }
        }
    }
}