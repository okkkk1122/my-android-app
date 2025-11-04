// workout/data/model/Coach.kt
package com.gymway.workout.data.model

import com.google.firebase.firestore.PropertyName

data class Coach(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("name") val name: String = "",
    @get:PropertyName("email") val email: String = "",
    @get:PropertyName("specialty") val specialty: String = "",
    @get:PropertyName("athletes") val athletes: List<String> = emptyList(), // لیست ID ورزشکاران
    @get:PropertyName("createdAt") val createdAt: com.google.firebase.Timestamp? = null
) {
    val athletesCount: Int
        get() = athletes.size
}