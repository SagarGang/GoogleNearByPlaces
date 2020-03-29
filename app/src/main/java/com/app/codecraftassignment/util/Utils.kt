package com.app.codecraftassignment.util
import android.content.Context
import android.location.Location
import android.widget.Toast

fun showToast(context: Context, msg: String?) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun calculateDistance(restLat: Double, restLng: Double): Int {


    val distance = FloatArray(2)

    Location.distanceBetween(
        SpLocation.lat.toDouble(), SpLocation.lng.toDouble(),
        restLat, restLng, distance
    )

    return distance[0].toInt()

}
