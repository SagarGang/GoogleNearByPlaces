package com.app.codecraftassignment.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RestaurantResponse(
    @SerializedName("next_page_token")
    val nextPageToken: String?,
    @SerializedName("results")
    val results: List<Result>,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Result(
        @SerializedName("geometry")
        val geometry: Geometry?,
        @SerializedName("icon")
        val icon: String?,
        @SerializedName("id")
        val id: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("photos")
        val photos: List<Photo?>?,
        @SerializedName("rating")
        val rating: Double?,
        @SerializedName("reference")
        val reference: String?
    ) : Parcelable {
        @Parcelize
        data class Geometry(
            @SerializedName("location")
            val location: Location?
        ) : Parcelable {
            @Parcelize
            data class Location(
                @SerializedName("lat")
                val lat: Double,
                @SerializedName("lng")
                val lng: Double
            ) : Parcelable
        }
        @Parcelize
        data class Photo(
            @SerializedName("height")
            val height: Int?,
            @SerializedName("photo_reference")
            val photoReference: String?,
            @SerializedName("width")
            val width: Int?
        ) : Parcelable
    }
}