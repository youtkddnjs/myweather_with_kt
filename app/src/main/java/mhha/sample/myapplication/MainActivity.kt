package mhha.sample.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import mhha.sample.myapplication.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit= Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        service.getVillageForescast(
            servicekey = "djc2Y0AjqXY1scaDhW/GnRjusKgsTFTW70ThCP/x8E2f9XeA1dUDVhP6RypcGM67pNPxPvrFQuGpYI4hQkGVOw==",
            baseDate = "20231121",
            baseTime = "1400",
            nx = 55,
            ny = 127
        ).enqueue(object : Callback<WeatherEntity>{
            override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>) {
                val forecastList = response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                for (i in forecastList){
                    Log.i("Forecast", i.toString())
                }
            }

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                t.printStackTrace()
            }
        })

    }//override fun onCreate(savedInstanceState: Bundle?)



}//class MainActivity : AppCompatActivity()