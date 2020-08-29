package com.dontsu.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Timber initialize
        Timber.plant(Timber.DebugTree())

        checkPermission()
    }

    private fun startProcess() {
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun confirmAgain() {
        AlertDialog.Builder(this@MapsActivity)
            .setTitle(getString(R.string.confirm_title))
            .setMessage(getString(R.string.confirm_again))
            .setPositiveButton("네") { _, _ ->
                requestPermission()
            }
            .setNegativeButton("아니요") { _, _ ->
                Toast.makeText(this@MapsActivity, getString(R.string.confirm_again2), Toast.LENGTH_SHORT).show()
                finish()
            }.create()
            .show()
    }

    private fun checkPermission() {
        var permitted_all = true
        for (permission in permissions) {
            val permissionState = ContextCompat.checkSelfPermission(this@MapsActivity, permission)
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                permitted_all = false
                requestPermission()
                break
            }
        }
        if (permitted_all) {
            startProcess()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@MapsActivity, permissions, 99)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
      when(requestCode) {
          99 -> {
              var granted_all = true
              for (grant in grantResults) {
                  if (grant != PackageManager.PERMISSION_GRANTED) {
                      granted_all = false
                      break
                  }
              }
              if (granted_all) {
                  startProcess()
              } else {
                  confirmAgain()
              }
          }
      }
    }

    fun setLastLocation(lastLocation: Location) {
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val markerOptions = MarkerOptions()
            .position(LATLNG)
            .title("Here!")
        val cameraPosition = CameraPosition.Builder()
            .target(LATLNG)
            .zoom(15.0f)
            .build()
        mMap.clear()
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    @SuppressLint("MissingPermission")
    fun updateLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
               locationResult?.let {
                   for ((i, location) in it.locations.withIndex()) {
                        Timber.d("Location = $i ${location.latitude}, ${location.longitude}")
                       setLastLocation(location)
                   }
               }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)
        updateLocation()
        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }
}