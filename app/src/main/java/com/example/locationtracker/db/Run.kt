package com.example.locationtracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "running_table"

)
data class Run(
    var img : Bitmap? = null, //Create typeConverters for it
    var timeStamp : Long = 0L, //When our run was
    var avgSpeedInKMH : Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis : Long = 0L, //For how long our run was
    var caloriesBurned : Int = 0

){
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null

}