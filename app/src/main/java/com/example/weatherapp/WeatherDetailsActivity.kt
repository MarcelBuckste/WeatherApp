package com.example.weatherapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WeatherDetailsActivity : AppCompatActivity() {
    private val API_KEY = "7bcf2f0452cdb22b1fa928e8bde613a2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)

        val cityName = intent.getStringExtra("city")
        getWeatherData(cityName)
    }

    private fun getWeatherData(cityName: String?) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API_KEY"

        val thread = Thread {
            try {
                val weatherJson = URL(url).readText(Charsets.UTF_8)
                val weatherData = JSONObject(weatherJson)
                val main = weatherData.getJSONObject("main")
                val sys = weatherData.getJSONObject("sys")
                val wind = weatherData.getJSONObject("wind")
                val weather = weatherData.getJSONArray("weather").getJSONObject(0)

                val temp = String.format("%.0f", main.getDouble("temp")) + "°C"
                val tempMin = "Tiefsttemperatur: " + String.format("%.0f", main.getDouble("temp_min")) + "°C"
                val tempMax = "Höchsttemperatur: " + String.format("%.0f", main.getDouble("temp_max")) + "°C"
                val address = weatherData.getString("name")
                val description = weather.getString("description")

                runOnUiThread {
                    findViewById<TextView>(R.id.location).text = address
                    findViewById<TextView>(R.id.temperatur).text = temp
                    findViewById<TextView>(R.id.temp_min).text = tempMin
                    findViewById<TextView>(R.id.temp_max).text = tempMax
                    findViewById<TextView>(R.id.weatherstates).text = description
                    findViewById<TextView>(R.id.weatherstates).text = description
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
