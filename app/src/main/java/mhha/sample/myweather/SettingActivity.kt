package mhha.sample.myweather

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mhha.sample.myweather.databinding.ActivitySettingBinding

class SettingActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    @RequiresApi(Build.VERSION_CODES.O)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                ContextCompat.startForegroundService(this,Intent(this,UpdateWeatherService::class.java))
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                ContextCompat.startForegroundService(this,Intent(this,UpdateWeatherService::class.java))
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                ContextCompat.startForegroundService(this,Intent(this,UpdateWeatherService::class.java))
            }else -> {
            Toast.makeText(this,"워치 권한 필요함", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            locationPermissionRequest.launch(
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS)
            )
        }else{
            locationPermissionRequest.launch(
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        }
    }//override fun onStart()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply{
                data = Uri.fromParts("lackage",packageName,null)
            }
            startActivity(intent)
        }

    }//override fun onCreate(savedInstanceState: Bundle?) {
}//class SettingActivity: AppCompatActivity()