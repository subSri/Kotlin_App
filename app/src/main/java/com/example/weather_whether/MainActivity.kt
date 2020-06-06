package com.example.weather_whether

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*



 class MainActivity : AppCompatActivity() {


    private lateinit var API: String
     lateinit var mFusedLocationClient: FusedLocationProviderClient
//     var PERMISSION_ID:Int = 71

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)




//         window.statusBarColor = ContextCompat.getColor(this,R.color. )
         setTitle("Kotlin Weather App")
//        PERMISSION_ID = 71
        API= "a9c40fe215c3bfb011f29cc8f66eaaab"
        var CITY: String?

        val search = findViewById<SearchView>(R.id.searchView)

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                CITY = query

                val sw1 = findViewById<Switch>(R.id.switch1)
//                 sw1.setOnCheckedChangeListener(null)
                sw1.setTag("TAG")
                sw1.setChecked(false)
                weatherTask(CITY,null,1).execute()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        val button = findViewById<ImageView>(R.id.imageView)
        button.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent,1)

        }

         val button2 = findViewById<ImageView>(R.id.imageView2)
         button2.setOnClickListener{
            recreate()

         }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sw1 = findViewById<Switch>(R.id.switch1)
        sw1?.setOnCheckedChangeListener({_, isChecked ->
            if (isChecked) {
                getLastLocation()
            }
            else{
//                recreate()
            }
        })




    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         if (requestCode == 1) {
             if (resultCode == Activity.RESULT_OK) {
                 var lat: Double = data!!.getDoubleExtra("lat", 0.0)
                 var long: Double = data!!.getDoubleExtra("long", 0.0)
                 val targetLocation:Location? = Location("returned")//provider name is unnecessary
                 targetLocation!!.latitude = lat
                 targetLocation!!.longitude = long

//                 Log.d("TAG", lat.toString())
                 val sw1 = findViewById<Switch>(R.id.switch1)
//                 sw1.setOnCheckedChangeListener(null)
                 sw1.setTag("TAG")
                 sw1.setChecked(false)
//                 sw1.setOnCheckedChangeListener(this)
                 weatherTask(null,targetLocation,2).execute()



             }

         }
     }



     private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }



    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {

        val PERMISSION_ID = 71
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val PERMISSION_ID = 71
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }



    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        weatherTask(null,location,2).execute()
                    }


                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }
        } else {
            requestPermissions()
        }

    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location? = locationResult.lastLocation
//            lat = mLastLocation.latitude.toString()
//            long  = mLastLocation.longitude.toString()
            weatherTask(null,mLastLocation,2).execute()
        }
    }

     inner class weatherTask(var CT:String?,var loc:Location?,var valu:Int) : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
//            findViewById<ImageView>(R.id.imageView2).visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                if(valu == 1) {
                    response =
                        URL("https://api.openweathermap.org/data/2.5/weather?q=$CT&units=metric&appid=$API").readText(
                            Charsets.UTF_8
                        )
                }
                else {
                    response =
                        URL("https://api.openweathermap.org/data/2.5/weather?lat=${loc?.latitude.toString()}&lon=${loc?.longitude.toString()}&appid=$API").readText(
                            Charsets.UTF_8
                        )
                }
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humid).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.imageView2).visibility = View.GONE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE

                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.imageView2).visibility = View.VISIBLE
            }

        }
    }


}