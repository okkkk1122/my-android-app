package com.gymway.auth.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "athlete",
    val emailVerified: Boolean = false,
    val createdAt: Timestamp? = null
) {
    val isAthlete: Boolean get() = role == "athlete"
    val isCoach: Boolean get() = role == "coach"
}