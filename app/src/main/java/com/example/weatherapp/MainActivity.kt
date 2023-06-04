package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val API_KEY = "7bcf2f0452cdb22b1fa928e8bde613a2"
    private lateinit var etCityName: EditText
    private lateinit var btnGetWeather: Button
    private lateinit var tvWeatherData: TextView
    private lateinit var notification: TextView

    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                val cityName = etCityName.text.toString()
                val intent = Intent(this, WeatherDetailsActivity::class.java)
                intent.putExtra("city", cityName)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Standortfreigabe wurde verweigert",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCityName = findViewById(R.id.etCityName)
        btnGetWeather = findViewById(R.id.btnGetWeather)
        tvWeatherData = findViewById(R.id.tvWeatherData)
        notification = findViewById(R.id.notification)

        btnGetWeather.setOnClickListener {
            if (checkLocationPermission()) {
                val cityName = etCityName.text.toString()

                checkCityExists(cityName) { exists ->
                    if (exists) {
                        val intent = Intent(this, WeatherDetailsActivity::class.java)
                        intent.putExtra("city", cityName)
                        startActivity(intent)
                    } else {
                        notification.text = "Ungültige Stadt - Bitte Eingabe überprüfen"
                        Toast.makeText(this, "Ungültige Stadt", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestLocationPermission()
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun checkCityExists(cityName: String, callback: (Boolean) -> Unit) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API_KEY"

        val thread = Thread {
            var cityExists = false

            try {
                val connection = URL(url).openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    cityExists = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread {
                callback(cityExists)
            }
        }

        thread.start()
    }

}
