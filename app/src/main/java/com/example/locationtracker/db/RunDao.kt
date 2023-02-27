package com.example.locationtracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run : Run)

    @Delete
    suspend fun deleteRun(run : Run)

    @Query("SELECT * FROM running_table Order By timeStamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table Order By timeInMillis DESC")
    fun getAllRunsSortedBytimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table Order By caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table Order By avgSpeedInKMH DESC")
    fun getAllRunsSortedByAverageSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table Order By distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("Select SUM(timeInMillis) From running_table")
    fun getTotalTimeInMillis() : LiveData<Long>

    @Query("Select SUM(caloriesBurned) From running_table")
    fun getTotalCaloriesBurned() : LiveData<Int>

    @Query("Select SUM(distanceInMeters) From running_table")
    fun getTotalDistance() : LiveData<Int>

    @Query("Select AVG(avgSpeedInKMH) From running_table")
    fun getTotalAvgSpeed() : LiveData<Float>


}