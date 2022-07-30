package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData(private val firebaseAuth:
                           FirebaseAuth=FirebaseAuth.getInstance()):LiveData<FirebaseUser>() {


    private val authenticationStateListener=FirebaseAuth.AuthStateListener {
        value=it.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authenticationStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authenticationStateListener)
    }
}