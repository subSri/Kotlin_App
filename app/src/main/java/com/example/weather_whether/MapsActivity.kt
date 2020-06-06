package com.example.weather_whether

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*






class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {
    override fun onMarkerClick(p0: Marker?) = false


    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setTitle("Pick Any Location")
        getSupportActionBar()!!.subtitle = "Long Press to put a marker"
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)




    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
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
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    var mMarker:Marker? = null
    var dropped_pin:String = "Pick This Location"

    private fun setInfoClick(map: GoogleMap) {
        map.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker) {
                //Convert LatLng to Location
                val location:Location? = Location("Test")
                location?.latitude = marker.position.latitude
                location?.longitude   = marker.position.longitude
//                val intent: Intent = getIntent()
                val resultIntent:Intent  = Intent()
                resultIntent.putExtra("lat",location?.latitude)
                resultIntent.putExtra("long",location?.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()





//            MainActivity().weatherTask("Wuhan",location,1).execute()
            }
//
            })

        }


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            if (mMarker == null) {
                mMarker = map.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(dropped_pin)
                    .snippet(snippet))
            } else {
                mMarker!!.setPosition(latLng)
            }
            Toast.makeText(this, "Tap on the marker and pick location", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
    }
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
//        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
        setUpMap()

        // 1

           if (isLocationEnabled()) {
        map.isMyLocationEnabled = true
// 2
               fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                   // Got last known location. In some rare situations this can be null.
                   // 3
                   if (location != null) {
                       lastLocation = location
                       val currentLatLng = LatLng(location.latitude, location.longitude)
                       map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                   }

               }
           }
               else{
                   Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
               }
         setMapLongClick(map)
            setInfoClick(map)

           }



    }



