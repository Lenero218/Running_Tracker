package com.example.locationtracker.other

import android.graphics.Color

object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "tracking"
    const val NOTIFICATION_ID = 1
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val locationUpdateInterval = 5000L
    const val  fastest_location_interval = 2000L
    const val polyLineColor = Color.RED
    const val polyLineWidth = 8f
    const val mapZoom = 15f
    const val timerUpdateInterval = 50L
    const val sharedPreferencesname = "sharedPref"
    const val KEY_FIRST_TIME_TOGEL = "KEY_FIRST_TIME_TOGEL"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"


}