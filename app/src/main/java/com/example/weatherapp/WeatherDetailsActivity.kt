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
            "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$API_KEY"

        val thread = Thread {
            try {
                val weatherJson = URL(url).readText(Charsets.UTF_8)
                val weatherData = JSONObject(weatherJson)
                val main = weatherData.getJSONObject("main")
                val sys = weatherData.getJSONObject("sys")
                val wind = weatherData.getJSONObject("wind")
                val weather = weatherData.getJSONArray("weather").getJSONObject(0)

                val temp = String.format("%.0f", main.getDouble("temp") - 273.15) + "°C"
                val tempMinMax = "H: " + String.format("%.0f", main.getDouble("temp_min") - 273.15) + "°C" + " T: " + String.format("%.0f", main.getDouble("temp_max") - 273.15) + "°C"
                val address = weatherData.getString("name")
                val description = weather.getString("description")

                runOnUiThread {
                    findViewById<TextView>(R.id.location).text = address
                    findViewById<TextView>(R.id.temperatur).text = temp
                    findViewById<TextView>(R.id.highandlow).text = tempMinMax
                    findViewById<TextView>(R.id.weatherState).text = description
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
