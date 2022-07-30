package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber
import java.util.ArrayList

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */

private const val REQUEST_CODE_FOR_SIGN_IN=101

class AuthenticationActivity : AppCompatActivity() {


    private lateinit var binding:ActivityAuthenticationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_authentication)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

        binding.authenticationButton.setOnClickListener{
            launchSignInFlow()
        }

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode== REQUEST_CODE_FOR_SIGN_IN){
//            val response=IdpResponse.fromResultIntent(data)
//            if(requestCode==Activity.RESULT_OK){
//                Timber.d("Logged in Successfully")
//                observeAuthenticationState()
//            }
//            else{
//                Timber.d("login failed due to ${response?.error?.errorCode}")
//                Toast.makeText(this,"Unable to login",Toast.LENGTH_LONG).show()
//
//            }
//        }
//    }

    private fun observeAuthenticationState() {
        startActivity(Intent(this,RemindersActivity::class.java))
        finish()
    }

    private fun launchSignInFlow() {
        val providers= arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())

//        startActivityForResult(
//            AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .build() , REQUEST_CODE_FOR_SIGN_IN
//        )

        getResult.launch(AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build())
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                Timber.d("Logged in Successfully")
                observeAuthenticationState()
            }
            else{
                Timber.d("login failed ")
                Toast.makeText(this,"Unable to login",Toast.LENGTH_LONG).show()

            }
        }




}
