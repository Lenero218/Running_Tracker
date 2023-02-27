package com.example.locationtracker.Services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location


import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.locationtracker.R
import com.example.locationtracker.UI.MainActivity
import com.example.locationtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.locationtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.locationtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.locationtracker.other.Constants.ACTION_STOP_SERVICE
import com.example.locationtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.locationtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.locationtracker.other.Constants.NOTIFICATION_ID
import com.example.locationtracker.other.Constants.fastest_location_interval
import com.example.locationtracker.other.Constants.locationUpdateInterval
import com.example.locationtracker.other.Constants.timerUpdateInterval
import com.example.locationtracker.other.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

//{ {l1l2l3,lo1,lo2,lo3}, {}, {}, {}      }


@AndroidEntryPoint
class TrackingService : LifecycleService() { //Life cycle service is used because we will be using Live Data to observe the values, for
    //it we need to have a life cycle owner, that's why we used LifeCycleService

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()


    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var curNotificationBuilder  : NotificationCompat.Builder

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLines>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()

        curNotificationBuilder = baseNotificationBuilder

        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })


    }


    private fun killService(){
        serviceKilled = true
        isFirstRun  = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{

                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        Timber.d("Resumed service")
                        startTimer()
                    }
                }

                ACTION_PAUSE_SERVICE ->{
                    Timber.d("Paused service")
                    pauseService()
                }

                ACTION_STOP_SERVICE ->{
                    Timber.d("Stopped service")
                    killService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificaitonActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking){
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }

            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)

        }else{
            val resumeIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp,notificaitonActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest()
                    .apply {
                        interval = locationUpdateInterval
                        fastestInterval = fastest_location_interval
                        priority = PRIORITY_HIGH_ACCURACY //means highest accuracy of the location recieved
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,locationCallback, Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }




    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let{locations->
                    for(location in locations){
                        addpathPoint(location)
                        Timber.d("New Location : ${location.latitude}, ${location.longitude}")
                    }
                }

            }
        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
        }
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyLine()
        isTracking.postValue(
            true
        )
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!){
                //Time difference between now and time started
                lapTime = System.currentTimeMillis()-timeStarted

                //Post the new lap time
                timeRunInMillis.postValue(timeRun + lapTime)

                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }

                delay(timerUpdateInterval)
            }
            timeRun += lapTime
        }
    }

    private fun addpathPoint(location : Location?){
        location?.let {
            val position = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(
                    this
                )
            }
        }
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply{
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService(){

        startTimer()

        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }


        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {

            if(!serviceKilled){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it*1000))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }

        })

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager : NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

    }


}