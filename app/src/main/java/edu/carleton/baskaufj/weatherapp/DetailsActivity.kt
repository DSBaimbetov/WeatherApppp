package edu.carleton.baskaufj.weatherapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import edu.carleton.baskaufj.weatherapp.R.string.max_temp
import edu.carleton.baskaufj.weatherapp.adapter.CitiesListAdapter
import edu.carleton.baskaufj.weatherapp.data.WeatherResult
import edu.carleton.baskaufj.weatherapp.network.WeatherAPI
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.app_bar_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class DetailsActivity : AppCompatActivity() {

    lateinit var weatherAPI: WeatherAPI
    private val HOST_URL = "https://api.openweathermap.org/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val actionBar = supportActionBar
        actionBar!!.title = "Текущий город"
        actionBar.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra(CitiesListAdapter.KEY_DATA)) {
            tvCityName.text = intent.getStringExtra(CitiesListAdapter.KEY_DATA)
        }

        initRetrofit()

        makeAPICall(tvCityName.text.toString())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
                .baseUrl(HOST_URL) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build()
        weatherAPI = retrofit.create(WeatherAPI::class.java) //Создаем объект, при помощи которого будем выполнять запросы
    }

    fun makeAPICall(city: String) {
        val weatherCall = weatherAPI.getWeather(city)
        weatherCall.enqueue(object : Callback<WeatherResult> {
            override fun onFailure(call: Call<WeatherResult>, t: Throwable) {
                Toast.makeText(this@DetailsActivity, t.message, Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<WeatherResult>, response: Response<WeatherResult>) {
                val weatherResult = response.body()
                if (weatherResult == null) {
                    setTvValuesForNullResponseResult()
                }
                else {
                    resetTvDefaultSettings()
                    setTvValuesWithWeatherData(weatherResult)
                }
            }
        })
    }

    private fun setTvValuesWithWeatherData(weatherResult: WeatherResult?) {
        //изменения картинки в зависимости от погоды
        Glide.with(this@DetailsActivity).load("https://openweathermap.org/img/w/" +
                weatherResult?.weather?.get(0)?.icon
                + ".png").into(weatherIcon)

        val a = weatherResult?.sys?.sunrise?.toLong()

        if (a != null) {
            sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(a*1000))
        }

        val b = weatherResult?.sys?.sunset?.toLong()

        if (b != null) {
            sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(b*1000))
        }

        val c = weatherResult?.dt?.toLong()

        if (c != null) {
            updated_at.text = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(c*1000))
        }

        wind.text = String.format(getResources().getString(R.string.wind), weatherResult?.wind?.speed?.toString())
        tvTemperature.text = String.format(getResources().getString(R.string.temp), weatherResult?.main?.temp?.toString())
        tvWeatherDescription.text = weatherResult?.weather?.get(0)?.description.toString()
        tvMaxTemp.text = String.format(getResources().getString(max_temp), weatherResult?.main?.temp_max?.toString())
        tvMinTemp.text = String.format(getResources().getString(R.string.min_temp), weatherResult?.main?.temp_min?.toString())
        tvHumidity.text = String.format(getResources().getString(R.string.humidity), weatherResult?.main?.humidity?.toString())
        tvPressure.text = String.format(getResources().getString(R.string.pressure), weatherResult?.main?.pressure?.toString())
    }

    private fun resetTvDefaultSettings() {
        tvTemperature.textSize = 40F
        tvWeatherDescription.textSize = 25F
        weatherIcon.visibility = View.VISIBLE
        tvMaxTemp.visibility = View.VISIBLE
        tvMinTemp.visibility = View.VISIBLE
        tvPressure.visibility = View.VISIBLE
        tvHumidity.visibility = View.VISIBLE
        updated_at.visibility = View.VISIBLE
        line1.visibility = View.VISIBLE
        line2.visibility = View.VISIBLE
        line3.visibility = View.VISIBLE
    }

    private fun setTvValuesForNullResponseResult() {
        tvTemperature.text = getString(R.string.no_info_available)
        tvTemperature.textSize = 25F
        tvWeatherDescription.text = getString(R.string.did_you_misspell)
        tvWeatherDescription.textSize = 20F
        weatherIcon.visibility = View.GONE
        tvMaxTemp.visibility = View.GONE
        tvMinTemp.visibility = View.GONE
        tvPressure.visibility = View.GONE
        tvHumidity.visibility = View.GONE
        updated_at.visibility = View.GONE
        line1.visibility = View.GONE
        line2.visibility = View.GONE
        line3.visibility = View.GONE
    }
}
