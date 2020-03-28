package com.app.codecraftassignment.util

import android.content.Context
import android.content.SharedPreferences


object SpLocation {

    private const val NAME = "SpLatLng"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val USER_LATITUDE = Pair("user_lat", 12.961736887805122)
    private val USER_LONGITUDE = Pair("user_lng", 77.71351234916602)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }


    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var lat: Float
        get() = preferences.getFloat(USER_LATITUDE.first, USER_LATITUDE.second.toFloat())

        set(value) = preferences.edit {
            it.putFloat(USER_LATITUDE.first, value)
        }


    var lng: Float
        get() = preferences.getFloat(USER_LONGITUDE.first, USER_LONGITUDE.second.toFloat())

        set(value) = preferences.edit {
            it.putFloat(USER_LONGITUDE.first, value)
        }

}