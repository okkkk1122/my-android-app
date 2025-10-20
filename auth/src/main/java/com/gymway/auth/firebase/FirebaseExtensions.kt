package com.gymway.auth.firebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

suspend fun FirebaseAuth.createUserWithEmailAndPasswordSuspended(email: String, password: String): AuthResult {
    return this.createUserWithEmailAndPassword(email, password).await()
}

suspend fun FirebaseAuth.signInWithEmailAndPasswordSuspended(email: String, password: String): AuthResult {
    return this.signInWithEmailAndPassword(email, password).await()
}
