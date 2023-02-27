package com.example.locationtracker.UI.Fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SimpleCursorAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.locationtracker.R
import com.example.locationtracker.UI.ViewModel.MainViewModel
import com.example.locationtracker.UI.ViewModel.StatisticsViewModel
import com.example.locationtracker.other.CustomMarkerView
import com.example.locationtracker.other.TrackingUtility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import timber.log.Timber
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {


    private val viewModel : StatisticsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun setupBarChart(){
        BarChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.BLUE
            textColor = Color.BLUE
            setDrawGridLines(false)
        }

        BarChart.axisLeft.apply {
            axisLineColor = Color.BLUE
            textColor = Color.BLUE
            setDrawGridLines(false)
        }


        BarChart.axisRight.apply {
            axisLineColor = Color.BLUE
            textColor = Color.BLUE
            setDrawGridLines(false)
        }

        BarChart.apply{
            description.text = "Avg Speed over time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let{
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
                Timber.tag("TimeRun").d(totalTimeRun)

            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let{
                val km = it/1000f
                val totalDistance = round(km*10f)/10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
                Timber.tag("TotalDistanceString").d(totalDistanceString)

            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner,Observer{
            it?.let{
                val avgSpeed = round(it*10f)/10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
                Timber.tag("Average Speed").d(avgSpeedString)
            }

        })

        viewModel.totalCaloriedBurned.observe(viewLifecycleOwner, Observer {
            it?.let{
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
                Timber.tag("Total Calories").d(totalCalories)
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let{
                val allAverageSpeeds = it.indices.map{i->BarEntry(i.toFloat(),it[i].avgSpeedInKMH)}
                val barDataSet = BarDataSet(allAverageSpeeds,"Avg Speed Over Time").apply{
                    valueTextColor = Color.BLUE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }

                BarChart.data = BarData(barDataSet)
                BarChart.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                BarChart.invalidate()

            }
        })
    }


}