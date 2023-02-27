package com.example.locationtracker.UI.ViewModel

import androidx.lifecycle.ViewModel
import com.example.locationtracker.Repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
    //We haven't created the main repository in the App module still injecting it is working fine, the reason
    //behind is that the MainRepository is inject runDao and DAGGER knows how to create that, that's how it also
    //knows how to create the Main Repository, therefore no need to worry
) : ViewModel(){

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriedBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunSortedByDate()

}