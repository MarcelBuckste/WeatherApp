package com.example.weatherapp

import android.content.Intent
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import com.example.weatherapp.data.Location
import com.example.weatherapp.data.LocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.w3c.dom.Text
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class WeatherDetailsActivity : AppCompatActivity() {
    private val API_KEY = "7bcf2f0452cdb22b1fa928e8bde613a2"
    private lateinit var btngetlist: Button
    private lateinit var btnadd: Button
    var visibilitystring = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_weather_details)
        val cityName = intent.getStringExtra("city")
        btngetlist = findViewById(R.id.btngetlist)
        btnadd = findViewById(R.id.btnadd)


        btngetlist.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val locationDao = LocationDatabase.getDatabase(applicationContext).locationDao()
        GlobalScope.launch(Dispatchers.IO) {
            val locations = locationDao.getAll()
            val cityExists = locations.any { it.city == cityName }

            runOnUiThread {
                if (cityExists) {
                    btnadd.text = "Löschen"
                    btnadd.visibility = View.VISIBLE
                } else {
                    btnadd.visibility = View.VISIBLE
                }
            }
        }

        btnadd.setOnClickListener {
            val buttonText = btnadd.text.toString()
            if (buttonText == "Löschen") {
                GlobalScope.launch(Dispatchers.IO) {
                    locationDao.deleteByCity(cityName.toString())
                }
                Toast.makeText(this@WeatherDetailsActivity, "Eintrag gelöscht", Toast.LENGTH_SHORT).show()
                btnadd.visibility = View.GONE
            } else if (buttonText == "Hinzufügen") {
                GlobalScope.launch(Dispatchers.IO) {
                    val currentEntries = locationDao.getAll()
                    if (currentEntries.size < 6) {
                        val location = Location(null, cityName)
                        locationDao.insert(location)
                        runOnUiThread {
                            Toast.makeText(this@WeatherDetailsActivity, "Eintrag hinzugefügt", Toast.LENGTH_SHORT).show()
                            btnadd.text = "Löschen"
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@WeatherDetailsActivity, "Maximale Anzahl an Einträgen erreicht", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        getWeatherData(cityName)
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
                val weatherIconDrawable = ContextCompat.getDrawable(this@WeatherDetailsActivity, weatherIconResourceId)



                val clouds = weatherData.getJSONObject("clouds")
                val cloudiness = clouds.getInt("all")
                //Zeitabfragen
                val sunriseTimestamp = sys.getLong("sunrise")
                val sunsetTimestamp = sys.getLong("sunset")
                val timezoneOffset = weatherData.getInt("timezone")
                val dt = weatherData.getLong("dt")

                //Zeitformate *1000 in Milisekunden umwandeln, dass das Date arbeiten kann
                val dtTime = Date((dt + timezoneOffset) * 1000)
                val sunriseTime = Date((sunriseTimestamp + timezoneOffset) * 1000)
                val sunsetTime = Date((sunsetTimestamp + timezoneOffset) * 1000)
                val dateFormat = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
                val timeFormat2 = SimpleDateFormat("HH", Locale.GERMANY)
                val currentTime = timeFormat2.format(dtTime)
                val sunriseHour = timeFormat.format(sunriseTime)
                val sunsetHour = timeFormat.format(sunsetTime)

                //Überprüfung ob Nacht ist
                val isNight = currentTime < sunriseHour || currentTime >= sunsetHour
                val backgroundId = if (isNight) R.drawable.background_night else R.drawable.gradient_background

                //Zuordnung Daten
                val sunriseLocalTime = timeFormat.format(sunriseTime) +  " Uhr"
                val sunsetLocalTime = timeFormat.format(sunsetTime) + " Uhr"
                val temp = String.format("%.0f", main.getDouble("temp")) + "°C"
                val humidity = main.getString("humidity") + " %"
                val address = weatherData.getString("name")
                val description = weather.getString("description")
                val windpace = String.format("%.0f", wind.getDouble("speed"))+ " m/s"
                val country = sys.getString("country")
                val calculationDate = dateFormat.format(dtTime)
                val calculationTime = timeFormat.format(dtTime)

                //Sichtbarkeit wird nur bis 10.000 Meter angezeigt
                val visibilityformat = visibility/1000
                if (visibilityformat == 10)
                {
                    visibilitystring = "> "+visibilityformat.toString()+" Km"
                }
                else
                {
                    visibilitystring = visibilityformat.toString()+" Km"
                }
                //Abfrage für die nächsten Tage
                val forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&lang=de&units=metric&appid=$API_KEY"
                val forecastJson = URL(forecastUrl).readText(Charsets.UTF_8)
                val forecastData = JSONObject(forecastJson)
                val forecastList = forecastData.getJSONArray("list")

                val forecastDateFormat = SimpleDateFormat("EEEE", Locale.GERMAN)
                val forecastDays = arrayListOf<String>()
                val forecaststate = arrayListOf<String>()
                val forecastTemperatures = arrayListOf<String>()
                val weatherIconCodes = arrayListOf<String>()

                for (i in 0 until 5) {
                    val forecastItem = forecastList.getJSONObject(i * 8)
                    val weather = forecastItem.getJSONArray("weather").getJSONObject(0)
                    val iconCode = weather.getString("icon")
                    weatherIconCodes.add(iconCode)
                }

                val weatherIcons = arrayListOf<Drawable?>()
                //Jede 3 Stunden gibt es eine Aktualisierung deswegen * 8. 8 Aktualisierungen am Tag um die Daten pro Tag zu bekommen
                // 8*3 = 24 Stunden 16*3 = 48 Stunden...
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
}
