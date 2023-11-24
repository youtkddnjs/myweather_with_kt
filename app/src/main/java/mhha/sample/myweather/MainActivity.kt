package mhha.sample.myweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import mhha.sample.myweather.databinding.ActivityMainBinding
import mhha.sample.myweather.databinding.ItemForecastBinding
import java.util.Locale

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




    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //위치권한
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            locationPermissionRequest.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            Thread{
                try {

                    val addressList = Geocoder(this, Locale.KOREA).getFromLocation(
                        it.latitude, it.longitude, 1)
//                        37.51, 127.04, 1)
                    Log.d("addressList", "${addressList.toString()} ${it.latitude} ${it.longitude}")

                    runOnUiThread{
                        Log.d("address", "${addressList?.get(0)?.thoroughfare.isNullOrEmpty()}")
                        if(addressList?.get(0)?.thoroughfare.isNullOrEmpty()){
                            binding.locationTextView.text = "없음"
                        }else{
                            binding.locationTextView.text = addressList?.get(0)?.thoroughfare.orEmpty()
                        }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }.start()
            Log.i("ManintActivity", "getVillageForcate")
            WeatherRepository.getVillageForcate(
                longitude = it.longitude,
                latitude =  it.latitude,
                succeccCallback = {list ->
                    val currentForecast = list.first()
                    Log.i("currentForecast", currentForecast.toString())
                    binding.temperatureTextView.text= getString(R.string.temperature_text, currentForecast.temperature)
                    binding.skyTextView.text = currentForecast.weather
                    binding.precipitationTextView.text = getString(R.string.precipitation_text, currentForecast.precipitation)

                    binding.childForecastLayout.apply {
                        list.forEachIndexed { index, forecast ->
                            if(index ==0){
                                return@forEachIndexed
                            }
                            val itemView = ItemForecastBinding.inflate(layoutInflater)

                            itemView.timeTextView.text = "${forecast.forecastDate} ${forecast.forecastTime}"
                            itemView.weatherTextView.text = forecast.weather
                            itemView.temperatureTextView.text =
                                getString(R.string.temperature_text, forecast.temperature)
                            addView(itemView.root)
                        }
                    }//binding.childForecastLayout.apply
                    Log.i("Forecast", list.toString())
                },
                failureCallback = {
                    it.printStackTrace()
                }
            )//WeatherRepository.getVillageForcate

        }//fusedLocationClient?.lastLocation?.addOnSuccessListener
    }//private fun updateLocation()

}//class MainActivity : AppCompatActivity()