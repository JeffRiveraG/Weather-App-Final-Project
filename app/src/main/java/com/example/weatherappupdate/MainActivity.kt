package com.example.weatherappupdate

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    private lateinit var cityEditText: EditText
    private lateinit var temperatureTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var rainChanceTextView: TextView
    private lateinit var conditionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityEditText = findViewById(R.id.idEdtCity) ?: throw NullPointerException("idEdtCity not found")
        temperatureTextView = findViewById(R.id.idTVTemperature) ?: throw NullPointerException("idTVTemperature not found")
        windSpeedTextView = findViewById(R.id.idTVWindSpeed) ?: throw NullPointerException("idTVWindSpeed not found")
        humidityTextView = findViewById(R.id.idTVHumidity) ?: throw NullPointerException("idTVHumidity not found")
        rainChanceTextView = findViewById(R.id.idTVRainChance) ?: throw NullPointerException("idTVRainChance not found")
        conditionTextView = findViewById(R.id.idTVCondition) ?: throw NullPointerException("idTVCondition not found")

        findViewById<ImageView>(R.id.idIVSearch).setOnClickListener {
            val cityName = cityEditText.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "City Name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState == null) {
            val weatherConditionsFragment = WeatherConditionsFragment()
            val weatherDetailsFragment = WeatherDetailsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.weather_fragment_container, weatherConditionsFragment, "WeatherConditionsFragment")
                .add(R.id.details_fragment_container, weatherDetailsFragment, "WeatherDetailsFragment")
                .commit()
        }
    }

    private fun fetchWeatherData(city: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val weatherResponse = weatherService.getWeather(city, "8a8509cd5d77ca6eef6520e383454eac")
                updateUI(weatherResponse)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to fetch weather data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUI(weatherResponse: WeatherResponse?) {
        runOnUiThread {
            weatherResponse?.let {
                val temperatureCelsius = it.main.temp - 273.15
                temperatureTextView.text = getString(R.string.temperature_text, temperatureCelsius.roundToInt())
                windSpeedTextView.text = getString(R.string.wind_speed_text, it.wind.speed)
                humidityTextView.text = getString(R.string.humidity_text, it.main.humidity)
                rainChanceTextView.text = getString(R.string.rain_chance_text, it.rain?.chance ?: 0.0)

                val condition = it.weather[0].description
                conditionTextView.text = getString(R.string.condition_text, condition)
            }
        }
    }
}
