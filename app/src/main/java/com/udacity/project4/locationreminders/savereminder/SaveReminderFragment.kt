package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.udacity.project4.utils.GeofencingConstants.GEO_FENCE_RADIUS_METERS
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE=331
private const val REQUEST_TO_TURN_ON_LOCATION=29
private const val REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE=341

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofenceClient: GeofencingClient
    private lateinit var reminder: ReminderDataItem

    private val runningQOrLater=android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.Q


    private val geofencePendingIntent:PendingIntent by lazy {
        val intent=Intent(requireContext(),GeofenceBroadcastReceiver::class.java)
        intent.action=ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        geofenceClient=LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        ViewCompat.setElevation(binding.progressBar,150f)

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val pointOfInterest=_viewModel.selectedPOI.value
            val location = pointOfInterest?.name
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            reminder=ReminderDataItem(title,description,location,latitude, longitude)

            if(_viewModel.validateEnteredData(reminder)){
                isLocationEnabled()
            }
        }
    }

    private fun isLocationEnabled() {
        if(foregroundAndBackgroundLocationPermissionEnabled())
            checkDeviceLocation()
        else
            requestForegroundAndBackgroundPermission()
    }

    private fun checkDeviceLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_TO_TURN_ON_LOCATION)
                } catch (ex: IntentSender.SendIntentException) {
                    Timber.d("Error while location settings ${ex.message}")
                }
            } else {
                Snackbar.make(
                    binding.root,
                    "Location required", Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkDeviceLocation()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if(it.isSuccessful)
                addGeofence()
            if (it.isCanceled){
                Toast.makeText(requireContext(),"Can not add reminder",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun foregroundAndBackgroundLocationPermissionEnabled(): Boolean {
        val foregroundLocationEnabled = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val bakgroundLocationEnabled =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else
                true

        return foregroundLocationEnabled && bakgroundLocationEnabled
    }

    private fun requestForegroundAndBackgroundPermission() {
        if (foregroundAndBackgroundLocationPermissionEnabled()) {
            return
        }
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else ->
                REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE

        }

        requestPermissions(permissions, resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults.isEmpty()||grantResults[0]==PackageManager.PERMISSION_DENIED||
            (requestCode== REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE &&
                    grantResults[1]==PackageManager.PERMISSION_DENIED))
            {
               Snackbar.make(binding.saveReminder,R.string.permission_denied_explanation,
               Snackbar.LENGTH_INDEFINITE).setAction(R.string.settings) {
                   startActivity(Intent().apply {
                       action=Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                       data=Uri.fromParts("package",BuildConfig.APPLICATION_ID,null)
                       flags=Intent.FLAG_ACTIVITY_NEW_TASK
                   })
               }.show()
            }
        else{
            checkDeviceLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_TO_TURN_ON_LOCATION){
            checkDeviceLocation(false)
        }
    }

    private fun addGeofence() {
        if(this::reminder.isInitialized) {
            startGeofencing(reminder)
        }
        else{
            Toast.makeText(requireContext(),"Reminder not saved",Toast.LENGTH_LONG).show()
        }
    }

    private fun removeGeofence(pendingIntent: PendingIntent){
        geofenceClient.removeGeofences(pendingIntent).run {
            addOnCompleteListener {
                Timber.d("Geofence removed")
            }
            addOnFailureListener {
                Timber.d("Unable to remove Geofence")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGeofencing(reminder: ReminderDataItem) {
        val currentGeofenceData=reminder
        val geofence=Geofence.Builder()
            .setRequestId(currentGeofenceData.id)
            .setCircularRegion(reminder.latitude!!, reminder.longitude!!,GEO_FENCE_RADIUS_METERS)
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
           .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
           .build()

        val geofenceRequest=GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence).build()


                geofenceClient.addGeofences(geofenceRequest,geofencePendingIntent).run {
                    addOnSuccessListener {
                        Timber.d("add Geofence with id ${geofence.requestId}")
                        Snackbar.make(requireView(),"Geofence added",Snackbar.LENGTH_LONG).show()
                        if(!_viewModel.validateAndSaveReminder(reminder)){
                            removeGeofence(geofencePendingIntent)
                        }
                    }
                    addOnFailureListener {
                        _viewModel.showErrorMessage.postValue("error while adding")
                        if (it.message != null) {
                            Timber.d(it.message)
                        }
                    }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
