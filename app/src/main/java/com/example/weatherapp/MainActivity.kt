package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.data.LocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val API_KEY = "7bcf2f0452cdb22b1fa928e8bde613a2"
    private lateinit var etCityName: EditText
    private lateinit var btnGetWeather: Button
    private lateinit var dataInfo: TextView
    private lateinit var notification: TextView
    var visibilitystring = ""

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

    private fun getWeatherData(cityName: String?, tempTextView: TextView, timeTextView: TextView, stateTextView: TextView, iconImageView: ImageView) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&lang=de&units=metric&appid=$API_KEY"

        fun getWeatherIconFileName(weatherIconCode: String): String {
            return when (weatherIconCode) {
                "01d" -> "d01d" // Beispiel: Klares Himmel am Tag
                "01n" -> "d01n" // Beispiel: Klares Himmel in der Nacht
                "02d" -> "d02d" // Beispiel: Einige Wolken am Tag
                "02n" -> "d02n" // Beispiel: Einige Wolken in der Nacht
                "03d" -> "d03d" // Beispiel: Einige Wolken am Tag
                "03n" -> "d03n" // Beispiel: Einige Wolken in der Nacht
                "04d" -> "d04d" // Beispiel: Einige Wolken am Tag
                "04n" -> "d04n" // Beispiel: Einige Wolken in der Nacht
                "09d" -> "d09d" // Beispiel: Einige Wolken am Tag
                "09n" -> "d09n" // Beispiel: Einige Wolken in der Nacht
                "10d" -> "d10d" // Beispiel: Einige Wolken am Tag
                "10n" -> "d10n" // Beispiel: Einige Wolken in der Nacht
                "11d" -> "d11d" // Beispiel: Einige Wolken am Tag
                "11n" -> "d11n" // Beispiel: Einige Wolken in der Nacht
                "13d" -> "d13d" // Beispiel: Einige Wolken am Tag
                "13n" -> "d13n" // Beispiel: Einige Wolken in der Nacht
                "50d" -> "d50d" // Beispiel: Einige Wolken am Tag
                "50n" -> "d50n" // Beispiel: Einige Wolken in der Nacht
                else -> "unknown" // Beispiel: Standard-Icon für unbekannte Wetterbedingungen
            }
        }

        val thread = Thread {
            try {
                val weatherJson = URL(url).readText(Charsets.UTF_8)
                val weatherData = JSONObject(weatherJson)
                val main = weatherData.getJSONObject("main")
                val visibility = weatherData.getInt("visibility")
                val wind = weatherData.getJSONObject("wind")
                val sys = weatherData.getJSONObject("sys")
                val weather = weatherData.getJSONArray("weather").getJSONObject(0)
                val weatherIcon = weather.getString("icon")
                val weatherIconName = getWeatherIconFileName(weatherIcon)
                val weatherIconResourceId = resources.getIdentifier(weatherIconName, "drawable", packageName)
                val weatherIconDrawable = ContextCompat.getDrawable(this@MainActivity, weatherIconResourceId)



                val clouds = weatherData.getJSONObject("clouds")
                val cloudiness = clouds.getInt("all")

                val sunriseTimestamp = sys.getLong("sunrise")
                val sunsetTimestamp = sys.getLong("sunset")
                val timezoneOffset = weatherData.getInt("timezone")
                val dt = weatherData.getLong("dt")


                val dtTime = Date((dt + timezoneOffset) * 1000)
                val sunriseTime = Date((sunriseTimestamp + timezoneOffset) * 1000)
                val sunsetTime = Date((sunsetTimestamp + timezoneOffset) * 1000)
                val dateFormat = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
                val timeFormat2 = SimpleDateFormat("HH", Locale.GERMANY)
                val currentTime = timeFormat2.format(dtTime)
                val favcurrentTime = timeFormat.format(dtTime)
                val isNight = currentTime.toInt() < 6 || currentTime.toInt() >= 18


                val sunriseLocalTime = timeFormat.format(sunriseTime) +  " Uhr"
                val sunsetLocalTime = timeFormat.format(sunsetTime) + " Uhr"
                val temp = String.format("%.0f", main.getDouble("temp")) + "°C"
                val tempMin = "Tiefsttemperatur: " + String.format("%.0f", main.getDouble("temp_min")) + "°C"
                val tempMax = "Höchsttemperatur: " + String.format("%.0f", main.getDouble("temp_max")) + "°C"
                val humidity = main.getString("humidity") + " %"
                val address = weatherData.getString("name")
                val description = weather.getString("description")
                val visibilityformat = visibility/1000
                if (visibilityformat == 10)
                {
                    visibilitystring = "> "+visibilityformat.toString()+" Km"
                }
                else
                {
                    visibilitystring = visibilityformat.toString()+" Km"
                }


                val windpace = String.format("%.0f", wind.getDouble("speed"))+ " m/s"
                val country = sys.getString("country")
                val calculationDate = dateFormat.format(dtTime)
                val calculationTime = timeFormat.format(dtTime)



                runOnUiThread {
                    tempTextView.text = temp
                    timeTextView.text = favcurrentTime + " Uhr"
                    stateTextView.text = description
                    iconImageView.setImageDrawable(weatherIconDrawable)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Fehler", Toast.LENGTH_LONG).show()
                }
            }
        }
        thread.start()
    }


    private fun displayCitiesFromDatabase() {
        lifecycleScope.launch {
            val locations = withContext(Dispatchers.IO) {
                val locationDao = LocationDatabase.getDatabase(applicationContext).locationDao()
                locationDao.getAll()
            }

            val container = findViewById<LinearLayout>(R.id.citiesContainer)
            container.removeAllViews()

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 15)

            for (location in locations) {
                val cityLayout = layoutInflater.inflate(R.layout.city_layout, null)

                val textViewCity = cityLayout.findViewById<TextView>(R.id.textViewCity)
                textViewCity.text = location.city

                val favoritenTempTextView = cityLayout.findViewById<TextView>(R.id.favoriten_temp)
                favoritenTempTextView.text = "16°"

                val timeTextView = cityLayout.findViewById<TextView>(R.id.time)
                timeTextView.text = "10:30"

                val iconImageView = cityLayout.findViewById<ImageView>(R.id.FavoritenIcon)

                val favoritStateTextView = cityLayout.findViewById<TextView>(R.id.favorit_state)
                favoritStateTextView.text = "Wolkenlos"
                getWeatherData(location.city, favoritenTempTextView, timeTextView, favoritStateTextView, iconImageView)

                container.addView(cityLayout, layoutParams)
            }
        }
    }








    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etCityName = findViewById(R.id.etCityName)
        btnGetWeather = findViewById(R.id.btnGetWeather)
        dataInfo = findViewById(R.id.dataInfo)
        notification = findViewById(R.id.notification)
        displayCitiesFromDatabase()


        btnGetWeather.setOnClickListener {
            if (checkLocationPermission()) {
                val cityName = etCityName.text.toString()

                checkCityExists(cityName) { exists ->
                    if (exists) {
                        val intent = Intent(this, WeatherDetailsActivity::class.java)
                        intent.putExtra("city", cityName)
                        startActivity(intent)
                        etCityName.setText("")
                        notification.text = ("")
                    } else {
                        notification.text = "Ungültige Stadt - Bitte Eingabe überprüfen"
                        Toast.makeText(this, "Ungültige Stadt", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestLocationPermission()
            }
        }

        btnGetWeather.setOnClickListener {
            if (checkLocationPermission()) {
                val cityName = etCityName.text.toString()

                checkCityExists(cityName) { exists ->
                    if (exists) {
                        val intent = Intent(this, WeatherDetailsActivity::class.java)
                        intent.putExtra("city", cityName)
                        startActivity(intent)
                        etCityName.setText("")
                        notification.text = ("")
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
