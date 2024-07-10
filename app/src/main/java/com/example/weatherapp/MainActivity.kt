package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.todolist.Comments
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.*

class MainActivity : ComponentActivity() {

    private val BASE_URL = "https://api.weatherapi.com/v1/"
    private val TAG : String = "CHECK_RESPONSE"
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set content to the XML layout



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val location:String = ""
        // Check for runtime permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, get location
            getLocation { cityName ->
                if (cityName != null) {
                    // Use the city name here
                    main(cityName)
                } else {
                    Log.i(TAG, "City not found or error occurred")
                    Toast.makeText(this, "City not found or error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }


        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

//            hideKeyboard()

    }

    private fun getLocation(callback: (String?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Use the location here
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val geocoder = Geocoder(this, Locale.getDefault())
                        try {
                            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
                            if (addresses.isNotEmpty()) {
                                val cityName: String = addresses[0].locality
                                callback(cityName)
//                                Toast.makeText(this, "City: $cityName", Toast.LENGTH_SHORT).show()
//                                Log.i(TAG, "city $cityName")
                            } else {
                                Toast.makeText(this, "No city found", Toast.LENGTH_SHORT).show()
                                callback(null)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            callback(null)
                        }
                    } else {
                        Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
        } catch (se: SecurityException) {
            se.printStackTrace()
            Toast.makeText(this, "SecurityException: ${se.message}", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun hideKeyboard() {
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }
    private fun main(location:String) {
        val tvLocation : TextView = findViewById(R.id.tvLocation)
        val clMain : ConstraintLayout = findViewById(R.id.clMain)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(myApi::class.java)
        val call = apiService.getWeather("ad4a30a1de8141f9a3c80956240907",location)

        call.enqueue(object : Callback<Comments> {
            override fun onResponse(call: Call<Comments>, response: Response<Comments>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if(weatherResponse?.current?.is_day == 1){
                        changeTextColor(clMain, Color.parseColor("#2E3441"))
                        clMain.setBackgroundColor(Color.parseColor("#d8dce4"))
                    }
                    else{
                        changeTextColor(clMain, Color.parseColor("#d8dce4"))
                        clMain.setBackgroundColor(Color.parseColor("#2E3441"))
                    }



                    tvLocation.text = "${weatherResponse?.location?.name}, ${weatherResponse?.location?.country}"
                    val imageUrl: String = "https:${weatherResponse?.current?.condition?.icon.toString()}"
                    Log.i(TAG, "ImageUrl: $imageUrl")
                    val ivWeatherIcon : ImageView = findViewById(R.id.ivWeatherIcon)
                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .into(ivWeatherIcon)
                    Log.i(TAG, "onResponse : ${weatherResponse?.current}")
                }
                else{
                    Log.e(TAG, "Response Code: ${response.code()}")
                    Log.e(TAG, "Error Message: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Comments>, t: Throwable) {
                Log.i(TAG, "onFailure : ${t.message}")
            }
        })
    }
    private fun changeTextColor(viewGroup: ViewGroup, color: Int) {
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            if (view is ViewGroup) {
                changeTextColor(view, color)
            } else if (view is TextView) {
                view.setTextColor(color)
            }
        }
    }
}
