package com.app.codecraftassignment.ui

import com.app.codecraftassignment.util.Connection
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.codecraftassignment.R
import com.app.codecraftassignment.model.RestRequest
import com.app.codecraftassignment.model.RestaurantResponse
import com.app.codecraftassignment.ui.adapter.RestaurantListAdapter
import com.app.codecraftassignment.ui.adapter.ViewHolder
import com.app.codecraftassignment.util.Constants
import com.app.codecraftassignment.util.SpLocation
import com.app.codecraftassignment.viewmodel.RestaurantListViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import com.app.codecraftassignment.util.showToast


class HomeActivity : AppCompatActivity(), ViewHolder.OnRestaurantSelectedListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var restaurantListViewModel: RestaurantListViewModel
    private var request: StringBuilder? = null
    private  var restaurantList =  ArrayList<RestaurantResponse.Result>()
    private var nextToken: String? = null
    private var resultCount: Int = 0
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null


    private val restaurantAdapter: RestaurantListAdapter by lazy {
        RestaurantListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SpLocation.init(application)


        mGoogleApiClient =
            GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000.toLong())
            .setFastestInterval(1 * 1000.toLong())


        restaurantListViewModel = ViewModelProvider(this).get(RestaurantListViewModel::class.java)
        checkLocationPermission()

    }


    private fun checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLatLng()

        } else {
            ActivityCompat.requestPermissions(
                this@HomeActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.PERMISSION_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLatLng()
            }
        }
    }


    private fun getLatLng() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val lng = location.longitude.toString()
                val lat = location.latitude.toString()

                SpLocation.lat = lat.toFloat()
                SpLocation.lng = lng.toFloat()

                request = StringBuilder(SpLocation.lat.toString()).append(",").append(SpLocation.lng.toString())
                if (Connection.hasNetwork(this)) {
                    progress_bar.visibility = View.VISIBLE
                    restaurantListViewModel.getRestaurantResponse(RestRequest(request.toString(),""))
                } else
                    showToast(this@HomeActivity, getString(R.string.no_internet_connection))

            }
        }


    }

    override fun onStart() {
        super.onStart()

        rv_restaurant.adapter = restaurantAdapter
        restaurantAdapter.setClickListener(this)
        attachObserver()


        swipe_refresh_container.apply {
            setColorSchemeColors(
                ContextCompat.getColor(this@HomeActivity, R.color.colorPrimary),
                ContextCompat.getColor(this@HomeActivity, R.color.colorAccent),
                ContextCompat.getColor(this@HomeActivity, R.color.colorPrimaryDark)
            )
            setOnRefreshListener {
                if (Connection.hasNetwork(this@HomeActivity)) {
                    progress_bar.visibility = View.VISIBLE
                    restaurantList.clear()
                    restaurantAdapter.clear()
                    nextToken?.let {
                        restaurantListViewModel.getRestaurantResponse(RestRequest(request.toString(),it))
                    }
                    isRefreshing = false
                } else
                    showToast(this@HomeActivity, getString(R.string.no_internet_connection))
            }
        }

        rv_restaurant.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 2
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= resultCount
                    ) {
                        if (Connection.hasNetwork(this@HomeActivity)) {
                            // consume the next token API.
                            progress_bar.visibility = View.VISIBLE
                            restaurantListViewModel.getRestaurantResponse(RestRequest(request.toString(),""))
                        } else
                            showToast(this@HomeActivity, getString(R.string.no_internet_connection))
                    }

                }
            })
        }


    }

    override fun onResume() {
        super.onResume()
        mGoogleApiClient?.connect()
    }

    override fun onPause() {
        super.onPause()
        mGoogleApiClient?.let {
            if (it.isConnected)
                it.disconnect()
        }
    }


    private fun attachObserver() {
        restaurantListViewModel.getResponse()?.observe(this, Observer {
            progress_bar.visibility = View.GONE

            restaurantList.addAll(it.results as ArrayList<RestaurantResponse.Result>)
            restaurantAdapter.restaurant = restaurantList
            resultCount = restaurantList.size
            nextToken = it.nextPageToken
        }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu -> {

                val intent = Intent(this, MapsActivity::class.java).apply {
                    putParcelableArrayListExtra(Constants.LIST, restaurantList)
                }
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)


    }


    override fun onRestaurantSelected(restaurant: RestaurantResponse.Result) {
            // Zoom screen with photo
    }

    override fun onConnected(p0: Bundle?) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            SpLocation.lat = location.latitude.toFloat()
            SpLocation.lng = location.longitude.toFloat()
            request = StringBuilder(SpLocation.lat.toString()).append(",").append(SpLocation.lng.toString())
        }

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


}
