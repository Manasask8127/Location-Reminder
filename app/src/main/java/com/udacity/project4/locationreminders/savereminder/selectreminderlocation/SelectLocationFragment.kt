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
import android.widget.Toast
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
import java.util.*


private const val REQUEST_TO_TURN_ON_LOCATION=29
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
                _viewModel.navigationCommand.value = NavigationCommand.Back
            } else {
                Toast.makeText(requireContext(), R.string.select_poi, Toast.LENGTH_LONG).show()
            }
        }


    }


    private fun locationPermissionEnabled() {
        when {
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                map.isMyLocationEnabled = true
            }
            (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )) -> {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Know your location",
                    Snackbar.LENGTH_LONG
                ).show()
                requestPermission()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType == GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID

            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType=GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
        val latitude = 12.965616
        val longitude = 77.5761
        val zoom = 10f
        val cityLatLong = LatLng(latitude, longitude)
        map.addMarker(MarkerOptions().position(cityLatLong).title("marker for city"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLatLong, zoom))
        // map.addMarker(MarkerOptions().position(cityLatLong))
        setMapStyle(map)
        locationPermissionEnabled()
        setPoiClickListener(map)
        setOnMapClick(map)
        onLocationSelected()
    }

    private fun setOnMapClick(map: GoogleMap) {
        map.setOnMapClickListener {
            map.clear()
            val droppedPin = getString(R.string.dropped_pin)
            val snippet = getValueSnippet(it)
            pointOfInterest = PointOfInterest(it, droppedPin, snippet)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(it).title(getString(R.string.dropped_pin)).snippet(
                        getValueSnippet(it)
                    )
            )
            map.addCircle(
                CircleOptions().center(it).radius(250.0)
                    .strokeColor(Color.GREEN).fillColor(Color.WHITE).strokeWidth(3F)
            )
            marker.showInfoWindow()
        }
    }

    private fun getValueSnippet(it: LatLng?) = String.format(
        Locale.getDefault(),
        "LatLng: %1$.5f,Long:%2$.5f",
        it!!.latitude,
        it!!.longitude
    )


    private fun setPoiClickListener(map: GoogleMap) {
        map.setOnPoiClickListener {
            map.clear()
            pointOfInterest = it
            val poiMaker = map.addMarker(
                MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name)
            )
            map.addCircle(
                CircleOptions().center(pointOfInterest.latLng).radius(250.00)
                    .strokeColor(Color.WHITE).fillColor(Color.MAGENTA).strokeWidth(3F)
            )
            poiMaker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap?) {
        try {
            val success = map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success!!)
                Timber.d("Style parsng failed")

        } catch (e: Resources.NotFoundException) {
            Timber.d("Can't find style to parse , Error: ${e}")
        }
    }



    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locationPermissionEnabled()
            } else {
                Snackbar.make(
                    binding.root,
                    "Permission not granted to access location",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}

