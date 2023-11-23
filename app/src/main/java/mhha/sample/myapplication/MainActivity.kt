package mhha.sample.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import mhha.sample.myapplication.databinding.ActivityMainBinding
import mhha.sample.myapplication.databinding.ItemForecastBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                updateLocation()
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                updateLocation()
            } else -> {
                Toast.makeText(this,"워치 권한 필요함",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply{
                    data = Uri.fromParts("lackage",packageName,null)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private lateinit var binding : ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocation(){
        //위치권한
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            locationPermissionRequest.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            val retrofit= Retrofit.Builder()
                .baseUrl("http://apis.data.go.kr/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(WeatherService::class.java)

            val baseDateTime = BaseDateTime.getBaseDateTime()

            val converter = GeoPointConverter()
//            val point = converter.convert(lon = it.longitude , lat = it.latitude)
            val point = converter.convert(lon = 127.04 , lat = 37.51)
            service.getVillageForescast(
                servicekey = "djc2Y0AjqXY1scaDhW/GnRjusKgsTFTW70ThCP/x8E2f9XeA1dUDVhP6RypcGM67pNPxPvrFQuGpYI4hQkGVOw==",
                baseDate = baseDateTime.baseDate,
                baseTime = baseDateTime.baseTime,
                nx = point.nx,
                ny = point.ny
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

                    val list = forecastDataTimeMap.values.toMutableList()
                    list.sortWith{f1, f2, ->
                        val f1DateTime = "${f1.forecastDate}${f1.forecastTime}"
                        val f2DateTime = "${f2.forecastDate}${f2.forecastTime}"
                        return@sortWith f1DateTime.compareTo(f2DateTime)
                    }

                    val currentForecast = list.first()
                    binding.temperatureTextView.text= getString(R.string.temperature_text, currentForecast.temperature)
                    binding.skyTextView.text = currentForecast.weather
                    binding.precipitationTextView.text = getString(R.string.precipitation_text, currentForecast.precipitation)

                    binding.childForecastLayout.apply {
                        list.forEachIndexed { index, forecast ->
                            if(index ==0){
                                return@forEachIndexed
                            }
                            val itemView = ItemForecastBinding.inflate(layoutInflater)

                            itemView.timeTextView.text = forecast.forecastTime
                            itemView.weatherTextView.text = forecast.weather
                            itemView.temperatureTextView.text =
                                getString(R.string.temperature_text, forecast.temperature)
                            addView(itemView.root)
                        }
                    }//binding.childForecastLayout.apply

                    Log.i("Forecast", forecastDataTimeMap.toString())
                    Log.i("Forecast", "${baseDateTime.baseDate}, ${baseDateTime.baseTime }")
                }//override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>)

                override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }//fusedLocationClient?.lastLocation?.addOnSuccessListener
    }//private fun updateLocation()

}//class MainActivity : AppCompatActivity()