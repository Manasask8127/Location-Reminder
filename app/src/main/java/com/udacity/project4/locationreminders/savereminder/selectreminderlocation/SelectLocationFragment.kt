package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.data.model.Resource
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.BuildConfig
import org.koin.android.ext.android.inject
import timber.log.Timber

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE=33
private const val LOCATION_PERMISSION_INDEX=0
private const val REQUEST_TO_TURN_ON_LOCATION=29
private const val BACKGROUND_LOCATION_PERMISSION_INDEX=1
private const val REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE=34



class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var pointOfInterest: PointOfInterest

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location


        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        binding.saveLocationButton.setOnClickListener {
            if (this::pointOfInterest.isInitialized) {
                val latLng = pointOfInterest.latLng
                _viewModel.latitude.value = latLng.latitude
                _viewModel.longitude.value = latLng.longitude
                _viewModel.selectedPOI.value = pointOfInterest
                _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            } else
                Snackbar.make(binding.saveLocationButton, R.string.select_poi, Snackbar.LENGTH_LONG)
                    .show()
        }


    }


    @SuppressLint("MissingPermission")
    private fun requestToEnableLocation() {
        if (foregroundAndBackgroundLocationPermissionEnabled()) {
            map.isMyLocationEnabled = true
        } else {
            requestForegroundAndBackgroundPermission()
        }
    }

    private fun foregroundAndBackgroundLocationPermissionEnabled(): Boolean {
        val foregroundLocationEnabled = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val bakgroundLocationEnabled =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
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
                    exception.startResolutionForResult(activity!!, REQUEST_TO_TURN_ON_LOCATION)
                } catch (ex: IntentSender.SendIntentException) {
                    Timber.d("Error while location settings ${ex.message}")
                }
            } else {
                Snackbar.make(
                    binding.mapLayout,
                    "Location required", Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkDeviceLocation()
                }.show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType == GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        val latitude = 12.965616
        val longitude = 77.5761
        val zoom = 15f
        val cityLatLong = LatLng(latitude, longitude)
        map.addMarker(MarkerOptions().position(cityLatLong).title("marker for city"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLatLong, zoom))
        // map.addMarker(MarkerOptions().position(cityLatLong))
        requestToEnableLocation()
        setPoiClickListener(map)
        onLocationSelected()
        map.setOnMyLocationClickListener(
            GoogleMap.OnMyLocationClickListener {
                checkDeviceLocation()
                true
            }
        )
//        setMapStyle(map)
//        requestToEnableLocation()
//        requestForegroundAndBackgroundPermission()
//        setPoiClick(map)

    }

    private fun setPoiClickListener(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            this.pointOfInterest = poi
            val poiMaker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            map.addCircle(
                CircleOptions().center(poi.latLng).radius(250.00)
                    .strokeColor(Color.WHITE).fillColor(Color.MAGENTA).strokeWidth(3F)
            )
            poiMaker.showInfoWindow()
        }
    }

//    private fun setMapStyle(map: GoogleMap?) {
//        try {
//            val success = map?.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
//            )
//            if (!success!!)
//                Timber.d("Style parsng failed")
//
//        }
//        catch (e:Resources.NotFoundException) {
//            Timber.d("Can't find style to parse , Error: ${e}")
//        }
//    }
//
//
//    private fun setPoiClick(map: GoogleMap?) {
//        map?.setOnPoiClickListener { poi->
//            binding.saveLocationButton.visibility=View.VISIBLE
//            val poiMarket= map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
//            val zoom=17f
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng,zoom))
//            poiMarket?.showInfoWindow()
//
//            binding.saveLocationButton.setOnClickListener {
//                onLocationSelected(poi)
//            }
//        }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
            || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                binding.mapLayout,
                "Permission Denied", Snackbar.LENGTH_LONG
            ).setAction("Settings")
            {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            map.isMyLocationEnabled = true
        }
    }
}

