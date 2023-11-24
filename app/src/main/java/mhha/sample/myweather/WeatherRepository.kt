package mhha.sample.myweather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRepository {

    private val retrofit= Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WeatherService::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getVillageForcate(
        longitude: Double,
        latitude : Double,
        succeccCallback: (List<Forecast>) -> Unit,
        failureCallback: (Throwable) -> Unit,
    ){
        val baseDateTime = BaseDateTime.getBaseDateTime()
        val converter = GeoPointConverter()
        val point = converter.convert(lon = longitude , lat = latitude)
//            val point = converter.convert(lon = 127.04 , lat = 37.51)
        service.getVillageForescast(
            servicekey = "djc2Y0AjqXY1scaDhW/GnRjusKgsTFTW70ThCP/x8E2f9XeA1dUDVhP6RypcGM67pNPxPvrFQuGpYI4hQkGVOw==",
            baseDate = baseDateTime.baseDate,
            baseTime = baseDateTime.baseTime,
            nx = point.nx,
            ny = point.ny
        ).enqueue(object : Callback<WeatherEntity> {
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

                val list = forecastDataTimeMap.values.toMutableList()
                list.sortWith{f1, f2, ->
                    val f1DateTime = "${f1.forecastDate}${f1.forecastTime}"
                    val f2DateTime = "${f2.forecastDate}${f2.forecastTime}"
                    return@sortWith f1DateTime.compareTo(f2DateTime)
                }
                Log.d("succeccCallback_1", list.toString())
                if(list.isNotEmpty()){
                    Log.d("succeccCallback", "succeccCallback")
                    succeccCallback(list)
                }else{
                    Log.d("succeccCallback", "failureCallback")
                    failureCallback(NullPointerException())
                }


            }//override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>)

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                failureCallback(t)
            }
        })
    } //fun getVillageForcate()

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
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> "-"
        }
    } //private fun transformSkyType(forecast: ForecastEntity): String
} //object WeatherRepository