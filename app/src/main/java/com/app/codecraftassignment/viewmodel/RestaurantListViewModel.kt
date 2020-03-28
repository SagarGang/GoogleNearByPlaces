package com.app.codecraftassignment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.app.codecraftassignment.model.RestaurantResponse

class RestaurantListViewModel(application: Application) : AndroidViewModel(application) {

    var restaurantRepository: GetRestaurantRepository? = null


    init {
        restaurantRepository = GetRestaurantRepository()
    }

    fun getRestaurantResponse(location: String) {
        restaurantRepository?.callRestaurant(location)
    }


    fun getResponse(): LiveData<RestaurantResponse>? {
        return restaurantRepository?.getResponse()
    }



}