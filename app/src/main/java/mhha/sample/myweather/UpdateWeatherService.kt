package mhha.sample.myweather

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices

class UpdateWeatherService: Service(){
    override fun onBind(intent: Intent?): IBinder? {


        return null
    }//override fun onBind(intent: Intent?): IBinder?


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //notification Channel 을 만들고 ForegroundService로 전환시켜줘야함.
        createChannel()
        startForeground(1,createNotification())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //todo 위젯을 권한 없을 상태로 표시하고, 클릭했을 때 팝업을 얻을 수 있도록 조정
            return onStartCommand(intent, flags, startId)
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)

        //위치를 가져와서 업데이트하기
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            WeatherRepository.getVillageForcate(
                longitude = location.longitude,
                latitude = location.latitude,
                succeccCallback = { forecast ->
                    var currentForecast = forecast.first()

                    //클릭 정보를 가져와서 위젯을 업데이트하기
                    var pendingServiceIntent: PendingIntent = Intent(this, UpdateWeatherService::class.java)
                        .let {
                            PendingIntent.getService(this,1, it,PendingIntent.FLAG_IMMUTABLE)
                        }


                    // 위젯 텍스트에 글 넣기
                    RemoteViews(packageName,R.layout.widget_weather).apply{
                        setTextViewText(
                            R.id.temperatureTextView,
                            getString(R.string.temperature_text, currentForecast.temperature)
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            currentForecast.weather
                        )

                        // 클릭하면 업데이트함.
                        setOnClickPendingIntent(R.id.temperatureTextView,pendingServiceIntent)
                        Log.d("setOnClickPendingIntent", "setOnClickPendingIntent")

                    }.also {remoteView ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName,remoteView)
                    }
                    //서비스종료
                    stopSelf()
                },
                failureCallback = {
                    //todo 위제을 에러 상태로 표시
                    stopSelf()
                }
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val channel = NotificationChannel(Word.NOTIFICATION_CHANNEL, "날씨앱", NotificationManager.IMPORTANCE_LOW)
        channel.description = "위젯을 업데이트하는 채널"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }//private fun createChannel()

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, Word.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("제목 날씨앱")
            .setContentText("업데이트")
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    object Word{
        const val NOTIFICATION_CHANNEL = "widget_refresh_channel"
    }

}//class UpdateWeatherService: Service()