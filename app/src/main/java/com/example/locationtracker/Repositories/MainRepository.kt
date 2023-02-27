package com.example.locationtracker.Repositories

import com.example.locationtracker.db.Run
import com.example.locationtracker.db.RunDao
import javax.inject.Inject


class MainRepository @Inject constructor(
    val runDao : RunDao
) {
    suspend fun insertRun(run : Run) = runDao.insertRun(run)

    suspend fun deleteRun(run : Run) = runDao.deleteRun(run) // suspend because we will be running them inside
    //coroutines

    fun getAllRunSortedByDate() = runDao.getAllRunsSortedByDate() //Because this function returns a Live Data
    //Which works asynchronously, so there is no need to make it suspend

    fun getAllRunSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunSortedByTimeInMillis() = runDao.getAllRunsSortedBytimeInMillis()

    fun getAllRunSortedByAverageSpeed() = runDao.getAllRunsSortedByAverageSpeed()

    fun getAllRunSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

}