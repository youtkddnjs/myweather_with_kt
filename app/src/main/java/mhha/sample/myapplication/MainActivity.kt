package mhha.sample.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import mhha.sample.myapplication.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            } else -> {
                Toast.makeText(this,"워치 권한 필요함",Toast.LENGTH_SHORT).show()
        }
        }
    }

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.


    private lateinit var binding : ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //위치권한

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            locationPermissionRequest.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            Log.d("lastLocation", it?.toString() ?: "none" )
        }

        val retrofit= Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        val baseDateTime = BaseDateTime.getBaseDateTime()

        val converter = GeoPointConverter()
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