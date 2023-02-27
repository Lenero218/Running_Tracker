package com.example.locationtracker.UI.ViewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.Repositories.MainRepository
import com.example.locationtracker.db.Run
import com.example.locationtracker.other.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
    //We haven't created the main repository in the App module still injecting it is working fine, the reason
    //behind is that the MainRepository is inject runDao and DAGGER knows how to create that, that's how it also
    //knows how to create the Main Repository, therefore no need to worry
) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunSortedByDistance()

    private val runsSortedByCaloriesBurned = mainRepository.getAllRunSortedByCaloriesBurned()

    private val runsSortedByTime = mainRepository.getAllRunSortedByTimeInMillis()

    private val runsSortedByAverageSpeed = mainRepository.getAllRunSortedByAverageSpeed()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE

    init{
        runs.addSource(runsSortedByDate){result ->
            if(sortType == SortType.DATE){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByAverageSpeed){result ->
            if(sortType == SortType.AVERAGE_SPEED){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByCaloriesBurned){result ->
            if(sortType == SortType.CALORIES_BURNED){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByDistance){result ->
            if(sortType == SortType.DISTANCE){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByTime){result ->
            if(sortType == SortType.RUNNING_TIME){
                result?.let{
                    runs.value = it
                }
            }
        }


    }


    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE ->runsSortedByDate.value?.let{
            runs.value = it
        }
        SortType.RUNNING_TIME ->runsSortedByTime.value?.let{
            runs.value = it
        }
        SortType.AVERAGE_SPEED ->runsSortedByAverageSpeed.value?.let{
            runs.value = it
        }
        SortType.DISTANCE ->runsSortedByDistance.value?.let{
            runs.value = it
        }
        SortType.CALORIES_BURNED ->runsSortedByCaloriesBurned.value?.let{
            runs.value = it
        }


    }.also{
        this.sortType = sortType
    }



    fun insertRun(run : Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

}