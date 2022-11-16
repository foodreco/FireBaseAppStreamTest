package com.dreamreco.firebaseappstreamtest

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings.Secure.getString
import android.util.Log
import android.widget.RemoteViews
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.NavDeepLinkBuilder
import java.text.SimpleDateFormat

class WidgetProvider : AppWidgetProvider() {

//    private val MY_ACTION = "android.action.MY_ACTION"

    lateinit var mContext : Context

    private fun setMyAction(context: Context?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT)
    }

    private fun goToCamera(context: Context?) : PendingIntent {
            val pendingIntent = NavDeepLinkBuilder(context!!)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.onlyFragment)
            .createPendingIntent()

        return pendingIntent
    }

    private fun addViews(context: Context?): RemoteViews {
        val views = RemoteViews(context?.packageName, R.layout.example_appwidget)
        views.setOnClickPendingIntent(R.id.take_picture, setMyAction(context))
        views.setOnClickPendingIntent(R.id.go_to_camera, goToCamera(context))
        return views
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (context != null) {
            mContext = context
        }

        appWidgetIds?.forEach { WidgetIds ->
            val views: RemoteViews = addViews(context)
            appWidgetManager?.updateAppWidget(WidgetIds, views)
        }
    }
}

object Actions {
    private const val prefix = "com.dreamreco.firebaseappstreamtest.widgetprovider.action."
    const val TEST_ACTION = prefix + "testaction"
}