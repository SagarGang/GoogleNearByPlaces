package com.app.codecraftassignment.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.app.codecraftassignment.R
import com.app.codecraftassignment.model.RestaurantResponse
import com.app.codecraftassignment.ui.adapter.RestaurantListAdapter
import com.app.codecraftassignment.ui.adapter.ViewHolder
import com.app.codecraftassignment.util.Constants
import com.app.codecraftassignment.util.SpLocation
import com.app.codecraftassignment.viewmodel.RestaurantListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

class HomeActivity : AppCompatActivity(), ViewHolder.OnRestaurantSelectedListener {

    private lateinit var restaurantListViewModel: RestaurantListViewModel
    private lateinit var restaurantAdapter: RestaurantListAdapter
    private var request: StringBuilder? = null
    private lateinit var restaurantList: ArrayList<RestaurantResponse.Result>

    /* private val restaurantAdapter: RestaurantListAdapter by lazy {
         RestaurantListAdapter()
     }
 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SpLocation.init(application)
        restaurantListViewModel = ViewModelProvider(this).get(RestaurantListViewModel::class.java)

        restaurantAdapter = RestaurantListAdapter()
        rv_restaurant.adapter = restaurantAdapter
        restaurantAdapter.setClickListener(this)
        attachObserver()
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
            // Request for location permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLatLng()
            }
        }
    }


    private fun getLatLng() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lng = location?.longitude.toString()
            val lat = location?.latitude.toString()

            SpLocation.lat = lat.toFloat()
            SpLocation.lng = lng.toFloat()

            request = StringBuilder(lat).append(",").append(lng)
            progress_bar.visibility = View.VISIBLE
            restaurantListViewModel.getRestaurantResponse(request.toString())

        }
    }

    override fun onStart() {
        super.onStart()

        swipe_refresh_container.apply {
            setColorSchemeColors(
                ContextCompat.getColor(this@HomeActivity, R.color.colorPrimary),
                ContextCompat.getColor(this@HomeActivity, R.color.colorAccent),
                ContextCompat.getColor(this@HomeActivity, R.color.colorPrimaryDark)
            )
            setOnRefreshListener {
                restaurantAdapter.clear()
                restaurantListViewModel.getRestaurantResponse(request.toString())
                isRefreshing = false
            }
        }


    }

    private fun attachObserver() {
        restaurantListViewModel.getResponse()?.observe(this, Observer {
            progress_bar.visibility = View.GONE
            restaurantList = it.results as ArrayList<RestaurantResponse.Result>
            restaurantAdapter.restaurant = restaurantList
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
                    putParcelableArrayListExtra(Constants.LIST,restaurantList)
                }
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)


    }

    override fun onRestaurantSelected(restaurant: RestaurantResponse.Result) {

    }
}
