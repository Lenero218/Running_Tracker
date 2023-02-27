package com.example.locationtracker.UI.Fragments


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.Services.PolyLine
import com.example.locationtracker.Services.PolyLines
import com.example.locationtracker.Services.TrackingService
import com.example.locationtracker.UI.ViewModel.MainViewModel
import com.example.locationtracker.db.Run
import com.example.locationtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.locationtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.locationtracker.other.Constants.ACTION_STOP_SERVICE
import com.example.locationtracker.other.Constants.mapZoom
import com.example.locationtracker.other.Constants.polyLineColor
import com.example.locationtracker.other.Constants.polyLineWidth
import com.example.locationtracker.other.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG = "CancelDialog"
@AndroidEntryPoint
class TrackingFragment : androidx.fragment.app.Fragment(R.layout.fragment_tracking) {

        private val viewModel : MainViewModel by viewModels()

        private var isTracking = false

        private var pathPoints = mutableListOf<PolyLine>()

        private var map : GoogleMap? = null

        private var currTimeMillis = 0L

        private var menu : Menu? = null

        @set:Inject
        var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)



            mapView?.onCreate(savedInstanceState)
            btnToggleRun.setOnClickListener{
                toggleRun()
            }

            if(savedInstanceState!=null){
                val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                    CANCEL_TRACKING_DIALOG) as CancelTrackingDialog?
                cancelTrackingDialog?.setYesListener {
                    stopRun()
                }
            }

            btnFinishRun.setOnClickListener{
                zoomToSeeWholeTrack()
                endRunAndSaveToDb()
            }


            mapView?.getMapAsync{
                map = it
                addAllPolylines()
            }

            subscribeToObservers()
        }

        override fun onResume() {
            super.onResume()
            mapView?.onResume()

        }

        override fun onStart() {
            super.onStart()
            mapView?.onStart()
        }

        override fun onStop() {
            super.onStop()
            mapView?.onStop()
        }

        override fun onPause() {
            super.onPause()
            mapView?.onPause()
        }

        override fun onLowMemory() {
            super.onLowMemory()
            mapView?.onLowMemory()
        }

    //    override fun onDestroy() {
    //        super.onDestroy()
    //        mapView?.onDestroy()
    //    }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            mapView.onSaveInstanceState(outState)
        }

        private fun subscribeToObservers(){
            TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
                updateTracking(it)
            })
            TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
                pathPoints = it
                addLatestPolyLine()
                moveCameraToUsers()
            })

            TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
                currTimeMillis = it
                val formattedTime = TrackingUtility.getFormattedStopWatchTime(currTimeMillis,true)
                tvTimer.text = formattedTime
            })
        }

        private fun toggleRun(){ //To start the services
            if(isTracking){
                menu?.getItem(0)?.isVisible = true
                sendCommandToService(ACTION_PAUSE_SERVICE)
            }else{
                sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currTimeMillis>0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
       CancelTrackingDialog().apply {
           setYesListener {
               stopRun()
           }
       }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG)
    }

    private fun stopRun(){
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

        private fun updateTracking(isTracking : Boolean){ //Updating the buttons of the fragments
            this.isTracking = isTracking
            if(!isTracking && currTimeMillis > 0L){
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            }else if(isTracking){
                btnToggleRun.text = "Stop"
                menu?.getItem(0)?.isVisible = true
                btnFinishRun.visibility = View.GONE
            }
        }


        private fun moveCameraToUsers(){
            if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        pathPoints.last().last(),
                        mapZoom
                    )
                )
            }
        }

        private fun zoomToSeeWholeTrack(){
            val bounds = LatLngBounds.Builder()
            for(polyline in pathPoints){
                for(pos in polyline){
                    bounds.include(pos)
                }
            }
         map?.moveCamera(
              CameraUpdateFactory.newLatLngBounds(bounds.build(),
              mapView.width,
              mapView.height,
                  (mapView.height * 0.05f).toInt())
         )
        }

        private fun endRunAndSaveToDb(){
            map?.snapshot {bmap->
                var distanceInMeters = 0
                for(polyline in pathPoints){
                    distanceInMeters += TrackingUtility.calculatePolyLineLength(polyline).toInt()

                }

                val averageSpeed  = round((distanceInMeters/1000f) /(currTimeMillis/1000f/60/60) * 10)/10f
                val dateTimeStamp = Calendar.getInstance().timeInMillis
                val caloriesBurned = ((distanceInMeters/1000f) * weight).toInt()
                val run = Run(bmap,dateTimeStamp,averageSpeed,distanceInMeters,currTimeMillis,caloriesBurned)
                viewModel.insertRun(run)
                Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Run saved successfully",
                    Snackbar.LENGTH_LONG
                ).show()

                stopRun()

            }
        }
        private fun addAllPolylines(){ //When the activity is destroyed, to redraw all the lines
            for(polyline in pathPoints){
                val polylineOptions = PolylineOptions().color(polyLineColor).width(polyLineWidth).addAll(polyline)
                map?.addPolyline(polylineOptions)
            }
        }

        private fun addLatestPolyLine(){ //To constantly increase the polylines
            if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
                val preLastLatLng = pathPoints.last()[pathPoints.last().size-2]
                val lastLatlng = pathPoints.last().last()
                val polylineOptions = PolylineOptions().color(polyLineColor)
                    .width(polyLineWidth)
                    .add(preLastLatLng)
                    .add(lastLatlng)
                map?.addPolyline(polylineOptions)
            }
        }

        private fun sendCommandToService(action : String){
            Intent(requireContext(),TrackingService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }

        }

}