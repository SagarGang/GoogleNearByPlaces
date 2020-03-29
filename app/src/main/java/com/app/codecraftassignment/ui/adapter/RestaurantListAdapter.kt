package com.app.codecraftassignment.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import calculateDistance
import com.app.codecraftassignment.R
import com.app.codecraftassignment.model.RestaurantResponse
import kotlinx.android.synthetic.main.restaurant_item.view.*

class RestaurantListAdapter :
    RecyclerView.Adapter<ViewHolder>() {

    lateinit var onSelectedListener: ViewHolder.OnRestaurantSelectedListener

    var restaurant: MutableList<RestaurantResponse.Result> = mutableListOf()
        set(restaurant) {
            field.addAll(restaurant)
            notifyDataSetChanged()
        }

    override fun getItemCount() = restaurant.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindRepo(restaurant[position], onSelectedListener)
    }

    fun setClickListener(onSelectedListener: ViewHolder.OnRestaurantSelectedListener) {
        this.onSelectedListener = onSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView =
            LayoutInflater.from(parent.context).inflate(R.layout.restaurant_item, parent, false)
        return ViewHolder(inflatedView)
    }

    fun clear() {
        restaurant.clear()
        notifyDataSetChanged()
    }
}

class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    fun bindRepo(restaurant: RestaurantResponse.Result, onSelected: OnRestaurantSelectedListener) {
        itemView.setOnClickListener {
            onSelected.onRestaurantSelected(restaurant)
        }
        itemView.tv_rest_name.text = restaurant.name
        itemView.tv_rest_address.text = restaurant.rating.toString()
        itemView.tv_rest_distance.text =
            restaurant?.geometry?.location?.let { calculateDistance(it.lat, it.lng) }.toString() + "m"

    }

    interface OnRestaurantSelectedListener {
        fun onRestaurantSelected(restaurant: RestaurantResponse.Result)
    }
}