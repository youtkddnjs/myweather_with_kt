package mhha.sample.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import mhha.sample.myapplication.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit= Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        val baseDateTime = BaseDateTime.getBaseDateTime()
        service.getVillageForescast(
            servicekey = "djc2Y0AjqXY1scaDhW/GnRjusKgsTFTW70ThCP/x8E2f9XeA1dUDVhP6RypcGM67pNPxPvrFQuGpYI4hQkGVOw==",
            baseDate = baseDateTime.baseDate,
            baseTime = baseDateTime.baseTime,
            nx = 55,
            ny = 127
        ).enqueue(object : Callback<WeatherEntity>{
            override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>) {
                val forecastDataTimeMap = mutableMapOf<String, Forecast>()
                val forecastList = response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                for (i in forecastList){

//                    Log.i("Forecast", i.toString())

                    if(forecastDataTimeMap["${i.forecastDate}/${i.forecastTime}"] == null){
                        forecastDataTimeMap["${i.forecastDate}/${i.forecastTime}"] =
                            Forecast(forecastDate = i.forecastDate, forecastTime = i.forecastTime)
                    }

                    forecastDataTimeMap["${i.forecastDate}/${i.forecastTime}"]?.apply {
                        when(i.category){
                            Category.POP.toString() -> {precipitation = i.forecastValue.toInt()}
                            Category.PTY.toString() -> {precipitationType = transformRainType(i)}
                            Category.SKY.toString() -> {sky = transformSkyType(i)}
                            Category.TMP.toString() -> {temperature = i.forecastValue.toDouble()}
                            else -> {}
                        }
                    }//forecastDataTimeMap["${i.forecastDate}/${i.forecastTime}"].apply
                }//for (i in forecastList)
                Log.i("Forecast", forecastDataTimeMap.toString())
                Log.i("Forecast", "${baseDateTime.baseDate}, ${baseDateTime.baseTime }")
            }//override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>)

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                t.printStackTrace()
            }
        })

    }//override fun onCreate(savedInstanceState: Bundle?)


    private fun transformRainType(forecast: ForecastEntity): String {
       return when(forecast.forecastValue.toInt()){
            0 -> "없음"
            1 -> "비"
            2-> "비/눈"
            3-> "눈"
            4-> "소나기"
           else -> ""
       }
    } //private fun transformRainType(forecast: ForecastEntity): String

    private fun transformSkyType(forecast: ForecastEntity): String {
        return when(forecast.forecastValue.toInt()){
            0 -> "맑음"
            1 -> "구름많음"
            2-> "흐림"
            else -> ""
        }
    } //private fun transformSkyType(forecast: ForecastEntity): String

}//class MainActivity : AppCompatActivity()