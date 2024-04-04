package com.radroid.weatherapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.radroid.weatherapp.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_CODE = 345453
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        it will open settings if location not enabled
        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, "the location is not enable", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE && grantResults.size > 0) {
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            requestLocationData()
        } else {
            Toast.makeText(this, "the permission was not granted", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()
        mFusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                getLocationWeatherDetails(
                    locationResult.lastLocation?.latitude!!,
                    locationResult.lastLocation?.longitude!!
                )
            }
        }, Looper.myLooper())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(this)) {
            val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val serviceApi = retrofit.create(WeatherServiceApi::class.java)
            val call = serviceApi.getWeatherDetails(
                latitude,
                longitude,
                Constants.APP_ID,
                Constants.METRIC_UNIT
            )
            val currentTime=LocalDateTime.now()
            val dateFormat=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss",Locale.UK)
            val formattedDateTime=currentTime.format(dateFormat)
            call.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weather = response.body()
//                        Toast.makeText(this@MainActivity,"$weather",Toast.LENGTH_SHORT).show()
                        for (i in weather!!.weather.indices){
                            findViewById<TextView>(R.id.text_view_sunset).text=convertTime(weather.sys.sunset.toLong())
                            findViewById<TextView>(R.id.text_view_sunrise).text=convertTime(weather.sys.sunrise.toLong())
                            findViewById<TextView>(R.id.text_view_status).text=weather.weather[i].description
                            findViewById<TextView>(R.id.text_view_address).text=weather.sys.country.toString()
//                            findViewById<TextView>(R.id.text_view_address).text=weather.name
                            findViewById<TextView>(R.id.text_view_temp_max).text="Max Temp: "+ weather.main.temp_max.toString()
                            findViewById<TextView>(R.id.text_view_temp_min).text="Mix Temp: "+weather.main.temp_min.toString()
                            findViewById<TextView>(R.id.text_view_temp).text=weather.main.temp.toString()
                            findViewById<TextView>(R.id.text_view_humidity).text=weather.main.humidity.toString()
                            findViewById<TextView>(R.id.text_view_pressure).text=weather.main.pressure.toString()
                            findViewById<TextView>(R.id.text_view_wind).text=weather.wind.speed.toString()
                            findViewById<TextView>(R.id.text_view_updated_at).text=formattedDateTime
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }

            })
        } else {
            Toast.makeText(this, "there is no internet network connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun convertTime(time:Long):String{
        val date=Date(time * 1000L)
        val timeFormatted=SimpleDateFormat("HH:mm", Locale.UK)
        timeFormatted.timeZone= TimeZone.getDefault()
        return timeFormatted.format(date)
    }



    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {

//            if user denied the permission of access the location permission the below dialog will show
            showRequestDialog()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            requestPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_LOCATION_CODE

            )
        }
    }

    private fun showRequestDialog() {
        AlertDialog.Builder(this)
            .setPositiveButton("GO TO SETTING") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("CLOSE") { dialog, _ ->
                dialog.cancel()
            }.setTitle("Location permission needed")
            .setMessage("This permission is needed for accessing the location.It can be enabled under the application setting")
            .show()
    }
}