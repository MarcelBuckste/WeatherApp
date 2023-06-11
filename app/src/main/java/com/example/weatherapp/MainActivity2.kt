package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity2 : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val API_KEY = "7bcf2f0452cdb22b1fa928e8bde613a2"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var btnsearch: Button
    private lateinit var btnadd: Button
    var visibilitystring = ""
    private lateinit var progressBar: ProgressBar




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

            } else {
                Toast.makeText(
                    this,
                    "Standortfreigabe wurde verweigert",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getWeatherData(cityName: String?) {
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
                val weatherIconDrawable = ContextCompat.getDrawable(this@MainActivity2, weatherIconResourceId)



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
                val isNight = currentTime.toInt() < 6 || currentTime.toInt() >= 22


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

                val forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&lang=de&units=metric&appid=$API_KEY"
                val forecastJson = URL(forecastUrl).readText(Charsets.UTF_8)
                val forecastData = JSONObject(forecastJson)
                val forecastList = forecastData.getJSONArray("list")

                val forecastDateFormat = SimpleDateFormat("EEEE", Locale.GERMAN)
                val forecastDays = mutableListOf<String>()
                val forecaststate = arrayListOf<String>()
                val forecastTemperatures = ArrayList<String>()
                val weatherIconCodes = mutableListOf<String>()

                for (i in 0 until 5) {
                    val forecastItem = forecastList.getJSONObject(i * 8)
                    val weather = forecastItem.getJSONArray("weather").getJSONObject(0)
                    val iconCode = weather.getString("icon")
                    weatherIconCodes.add(iconCode)
                }

                val weatherIcons = mutableListOf<Drawable?>()

                for (iconCode in weatherIconCodes) {
                    val iconFileName = getWeatherIconFileName(iconCode)
                    val resourceId = resources.getIdentifier(iconFileName, "drawable", packageName)

                    if (resourceId != 0) {
                        val iconDrawable = ContextCompat.getDrawable(this, resourceId)
                        weatherIcons.add(iconDrawable)
                    } else {
                        weatherIcons.add(null) // Füge null hinzu, wenn das Icon nicht gefunden wurde
                    }
                }




                for (i in 0 until 5) {
                    val forecastItem = forecastList.getJSONObject(i * 8)
                    val forecastTimestamp = forecastItem.getLong("dt")
                    val forecastTime = Date((forecastTimestamp + timezoneOffset) * 1000)
                    val forecastDayOfWeek = forecastDateFormat.format(forecastTime)
                    forecastDays.add(forecastDayOfWeek)
                }
                for (i in 0 until 5) {
                    val forecastDay = forecastList.getJSONObject(i * 8)
                    val temperature = forecastDay.getJSONObject("main")
                    val avgTemp = temperature.getDouble("temp").toInt()
                    val temperatureString = "$avgTemp°C"
                    forecastTemperatures.add(temperatureString)
                }




                for (i in 0 until 5) {
                    val forecastDay = forecastList.getJSONObject(i * 8)
                    val weather = forecastDay.getJSONArray("weather").getJSONObject(0)
                    val forecastdescription = weather.getString("description")
                    forecaststate.add(forecastdescription)
                }


                val backgroundId = if (isNight) R.drawable.background_night else R.drawable.gradient_background


                runOnUiThread {

                    findViewById<ImageView>(R.id.overviewIcon).setImageDrawable(weatherIconDrawable)
                    findViewById<View>(R.id.overall).setBackgroundResource(backgroundId)
                    findViewById<TextView>(R.id.forecastday1).text = "Heute"
                    findViewById<TextView>(R.id.forecastday2).text = forecastDays[1].substring(0, 2)
                    findViewById<TextView>(R.id.forecastday3).text = forecastDays[2].substring(0, 2)
                    findViewById<TextView>(R.id.forecastday4).text = forecastDays[3].substring(0, 2)
                    findViewById<TextView>(R.id.forecastday5).text = forecastDays[4].substring(0, 2)
                    findViewById<TextView>(R.id.forecastday1state).text = forecaststate[0]
                    findViewById<TextView>(R.id.forecastday2state).text = forecaststate[1]
                    findViewById<TextView>(R.id.forecastday3state).text = forecaststate[2]
                    findViewById<TextView>(R.id.forecastday4state).text = forecaststate[3]
                    findViewById<TextView>(R.id.forecastday5state).text = forecaststate[4]
                    findViewById<TextView>(R.id.forecastday1Temp).text = forecastTemperatures[0]
                    findViewById<TextView>(R.id.forecastday2Temp).text = forecastTemperatures[1]
                    findViewById<TextView>(R.id.forecastday3Temp).text = forecastTemperatures[2]
                    findViewById<TextView>(R.id.forecastday4Temp).text = forecastTemperatures[3]
                    findViewById<TextView>(R.id.forecastday5Temp).text = forecastTemperatures[4]
                    findViewById<ImageView>(R.id.forecastday1Icon).setImageDrawable(weatherIcons[0])
                    findViewById<ImageView>(R.id.forecastday2Icon).setImageDrawable(weatherIcons[1])
                    findViewById<ImageView>(R.id.forecastday3Icon).setImageDrawable(weatherIcons[2])
                    findViewById<ImageView>(R.id.forecastday4Icon).setImageDrawable(weatherIcons[3])
                    findViewById<ImageView>(R.id.forecastday5Icon).setImageDrawable(weatherIcons[4])

                }



                runOnUiThread {
                    findViewById<TextView>(R.id.location).text = address +", " + country
                    findViewById<TextView>(R.id.datetime).text = calculationDate + ", "+ calculationTime + " Uhr"
                    findViewById<TextView>(R.id.temperatur).text = temp
                    findViewById<TextView>(R.id.weatherstates).text = description
                    findViewById<TextView>(R.id.weatherstates).text = description
                    findViewById<TextView>(R.id.visibility).text = visibilitystring
                    findViewById<TextView>(R.id.windpace).text = windpace
                    findViewById<TextView>(R.id.humidity).text = humidity
                    findViewById<TextView>(R.id.sunrise).text = sunriseLocalTime
                    findViewById<TextView>(R.id.sunset).text = sunsetLocalTime
                    findViewById<TextView>(R.id.cloudiness).text = "$cloudiness %"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Fehler", Toast.LENGTH_LONG).show()
                }
            }
        }

        thread.start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        progressBar = findViewById(R.id.progressbar)
        val mainContainer: RelativeLayout = findViewById(R.id.mainContainer)
        mainContainer.visibility= View.INVISIBLE

        progressBar.visibility = View.VISIBLE
        geocoder = Geocoder(this, Locale.getDefault())
        btnsearch = findViewById(R.id.btngetlist)
        btnadd = findViewById(R.id.btnadd)
        btnadd.visibility = View.INVISIBLE

        btnsearch.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }




        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
                .addOnSuccessListener { location: Location? ->
                    if (location == null)
                        Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                    else {
                        val lat = location.latitude
                        val lon = location.longitude
                        val address = this.geocoder.getFromLocation(lat, lon, 1)
                        val cityName = address?.get(0)?.locality.toString()
                        getWeatherData(cityName)

                        progressBar.visibility = View.GONE
                        mainContainer.visibility = View.VISIBLE



                    }
                }

        } else {
            requestLocationPermission()
        }


    }
}
