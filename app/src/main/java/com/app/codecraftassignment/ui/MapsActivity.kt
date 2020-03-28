package com.app.codecraftassignment.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.codecraftassignment.R
import com.app.codecraftassignment.model.RestaurantResponse
import com.app.codecraftassignment.util.Constants
import com.app.codecraftassignment.util.SpLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private var list: ArrayList<RestaurantResponse.Result>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (intent.hasExtra(Constants.LIST)) {
            list = intent.getParcelableArrayListExtra(Constants.LIST)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        list?.let {
            for (i in 0 until it.size) {

                val title = it[i].name
                it[i].geometry?.location?.let { addMarker(it.lat, it.lng, title) }
            }
        }


        mMap.setOnMarkerClickListener(this)
    }

    private fun addMarker(lat: Double, lng: Double, title: String?) {

        val rest = LatLng(lat, lng)
        mMap.addMarker(MarkerOptions().position(rest).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rest, 18.0f))

    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        val saddr = SpLocation.lat.toString() + "," + SpLocation.lng
        val daddr = marker?.position?.latitude.toString() + "," + marker?.position?.longitude

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?saddr=$saddr&daddr=$daddr")
        )
        startActivity(intent)
        return true
    }

}
