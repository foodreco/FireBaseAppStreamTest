package com.dreamreco.firebaseappstreamtest.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


object FireBaseModule {

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getUser() : FirebaseUser? = firebaseAuth.currentUser

}