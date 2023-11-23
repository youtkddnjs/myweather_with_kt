package mhha.sample.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WeatherAppWidgetProvider: AppWidgetProvider(){
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each widget that belongs to this
        // provider.
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */  1,
                /* intent = */ Intent(context, MainActivity::class.java),
//                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                /* flags = */ PendingIntent.FLAG_IMMUTABLE
            )

            // Get the layout for the widget and attach an onClick listener to
            // the button.
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_weather
            ).apply {
                setOnClickPendingIntent(R.id.temperatureTextView, pendingIntent)
            }

            // Tell the AppWidgetManager to perform an update on the current
            // widget.
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }//override fun onUpdate

}//class WeatherAppWidgetProvider: AppWidgetProvider()