package edu.carleton.baskaufj.weatherapp

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import edu.carleton.baskaufj.weatherapp.adapter.CitiesListAdapter
import edu.carleton.baskaufj.weatherapp.data.AppDatabase
import edu.carleton.baskaufj.weatherapp.data.City
import edu.carleton.baskaufj.weatherapp.network.WeatherAPI
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), AddCityDialog.CityHandler {

    lateinit var citiesListAdapter: CitiesListAdapter

    private var editIndex: Int = 0

    lateinit var weatherAPI: WeatherAPI
    private val HOST_URL = "https://api.openweathermap.org/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            showAddCityDialog()
        }

        initRetrofit()
        initRecyclerView()
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
                .baseUrl(HOST_URL)  //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build()
        weatherAPI = retrofit.create(WeatherAPI::class.java) //Создаем объект, при помощи которого будем выполнять запросы
    }

    private fun initRecyclerView() {
        Thread {
            val cities = AppDatabase.getInstance(this@MainActivity).citiesListDao().findAllCities()

            //добавление элементов, загруженные из базы данных
            citiesListAdapter = CitiesListAdapter(this@MainActivity, cities)
            runOnUiThread {
                recyclerCities.adapter = citiesListAdapter
            }
        }.start()
    }

    private fun showAddCityDialog() {
        AddCityDialog().show(supportFragmentManager, "TAG_CREATE")
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun cityAdded(city: City) {
        Thread {
            val id = AppDatabase.getInstance(this).citiesListDao().insertCity(city)
            city.cityId = id

            runOnUiThread {
                //добавление объекта элемента в recycler view
                citiesListAdapter.addCity(city)
            }
        }.start()
    }

    override fun cityUpdated(city: City) {
        val dbThread = Thread {
            //обновление в базе данных
            AppDatabase.getInstance(this@MainActivity).citiesListDao().updateCity(city)

            //обновление в recycler view
            runOnUiThread { citiesListAdapter.updateCity(city, editIndex) }
        }
        dbThread.start()
    }
}
