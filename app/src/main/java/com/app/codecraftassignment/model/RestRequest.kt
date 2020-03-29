package com.app.codecraftassignment.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RestRequest(var location: String, var nextToken: String) : Parcelable
