package com.gymway.workout.data.local.converter

import androidx.room.TypeConverter
import com.gymway.workout.data.model.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExerciseListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromExerciseList(exercises: List<Exercise>): String {
        return try {
            val json = gson.toJson(exercises)
            println("🔧 [TypeConverter] تبدیل ${exercises.size} تمرین به JSON")
            json
        } catch (e: Exception) {
            println("❌ [TypeConverter] خطا در تبدیل به JSON: ${e.message}")
            "[]"
        }
    }

    @TypeConverter
    fun toExerciseList(exercisesString: String): List<Exercise> {
        return try {
            if (exercisesString.isEmpty() || exercisesString == "[]") {
                println("🔧 [TypeConverter] JSON خالی، برگرداندن لیست خالی")
                return emptyList()
            }

            val listType = object : TypeToken<List<Exercise>>() {}.type
            val result = gson.fromJson<List<Exercise>>(exercisesString, listType) ?: emptyList()
            println("🔧 [TypeConverter] تبدیل JSON به ${result.size} تمرین")
            result
        } catch (e: Exception) {
            println("❌ [TypeConverter] خطا در تبدیل JSON: ${e.message}")
            emptyList()
        }
    }
}