package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.data.Location
import com.example.weatherapp.data.LocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)
        val cityName = intent.getStringExtra("city")

        btngetlist = findViewById(R.id.btngetlist)
        btnadd = findViewById(R.id.btnadd)

        btngetlist.setOnClickListener {

            finish()
        }

        btnadd.setOnClickListener {
            val cityName = intent.getStringExtra("city")
            val location = Location(null, cityName)

            val locationDao = LocationDatabase.getDatabase(applicationContext).locationDao()

            GlobalScope.launch(Dispatchers.IO) {
                locationDao.insert(location)
            }
            Toast.makeText(this, "Eintrag hinzugefügt", Toast.LENGTH_SHORT).show()
            btnadd.visibility = View.GONE
        }





        getWeatherData(cityName)
    }

    private fun getWeatherData(cityName: String?) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&lang=de&units=metric&appid=$API_KEY"

        val thread = Thread {
            try {
                val weatherJson = URL(url).readText(Charsets.UTF_8)
                val weatherData = JSONObject(weatherJson)
                val main = weatherData.getJSONObject("main")
                val visibility = weatherData.getInt("visibility")
                val wind = weatherData.getJSONObject("wind")
                val sys = weatherData.getJSONObject("sys")
                val weather = weatherData.getJSONArray("weather").getJSONObject(0)
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
                val sunriseLocalTime = timeFormat.format(sunriseTime) +  " Uhr"
                val sunsetLocalTime = timeFormat.format(sunsetTime) + " Uhr"
                val temp = String.format("%.0f", main.getDouble("temp")) + "°C"
                val tempMin = "Tiefsttemperatur: " + String.format("%.0f", main.getDouble("temp_min")) + "°C"
                val tempMax = "Höchsttemperatur: " + String.format("%.0f", main.getDouble("temp_max")) + "°C"
                val humidity = main.getString("humidity") + " %"
                val address = weatherData.getString("name")
                val description = weather.getString("description")
                val visibilityformat = visibility/1000
                val visibilitystring = visibilityformat.toString()+" Km"
                val windpace = String.format("%.0f", wind.getDouble("speed"))+ " m/s"
                val country = sys.getString("country")
                val calculationDate = dateFormat.format(dtTime)
                val calculationTime = timeFormat.format(dtTime)

                val forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&lang=de&units=metric&appid=$API_KEY"
                val forecastJson = URL(forecastUrl).readText(Charsets.UTF_8)
                val forecastData = JSONObject(forecastJson)
                val forecastList = forecastData.getJSONArray("list")

                // Get forecast data for the next two days
                val forecastDay1 = forecastList.getJSONObject(8)
                val forecastDay2 = forecastList.getJSONObject(16)

                // Get day of the week for the next two days
                val forecastDay1Timestamp = forecastDay1.getLong("dt")
                val forecastDay2Timestamp = forecastDay2.getLong("dt")
                val forecastDay1Time = Date((forecastDay1Timestamp + timezoneOffset) * 1000)
                val forecastDay2Time = Date((forecastDay2Timestamp + timezoneOffset) * 1000)
                val forecastDay1Format = SimpleDateFormat("EEEE", Locale.GERMANY)
                val forecastDay2Format = SimpleDateFormat("EEEE", Locale.GERMANY)
                val forecastDay1OfWeek = forecastDay1Format.format(forecastDay1Time)
                val forecastDay2OfWeek = forecastDay2Format.format(forecastDay2Time)

                val forecastDay1MinTemp = forecastDay1.getJSONObject("main").getDouble("temp_min")
                val forecastDay1MaxTemp = forecastDay1.getJSONObject("main").getDouble("temp_max")
                val forecastDay2MinTemp = forecastDay2.getJSONObject("main").getDouble("temp_min")
                val forecastDay2MaxTemp = forecastDay2.getJSONObject("main").getDouble("temp_max")



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
                    findViewById<TextView>(R.id.weekday).text = "$forecastDay1OfWeek: $forecastDay1MinTemp°C/$forecastDay1MaxTemp°C, $forecastDay2OfWeek: $forecastDay2MinTemp°C/$forecastDay2MaxTemp°C"
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
