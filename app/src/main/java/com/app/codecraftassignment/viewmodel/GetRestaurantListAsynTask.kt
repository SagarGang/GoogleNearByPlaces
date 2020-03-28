package com.app.codecraftassignment.viewmodel

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.codecraftassignment.model.RestaurantResponse
import com.app.codecraftassignment.util.Constants
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class GetRestaurantRepository {

    private val LOG_TAG = GetRestaurantListAsynTask::class.java.simpleName
    val restaurantResponseMutableLive = MutableLiveData<RestaurantResponse>()

    val resLiveData: LiveData<RestaurantResponse> get() = restaurantResponseMutableLive

    fun getResponse(): LiveData<RestaurantResponse> {
        return resLiveData
    }

    fun callRestaurant(request: String) {
        GetRestaurantListAsynTask().execute(request)
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetRestaurantListAsynTask : AsyncTask<String, Void, RestaurantResponse>() {


        override fun doInBackground(vararg params: String?): RestaurantResponse? {
            var conn: HttpURLConnection? = null
            val jsonResults: String?

            var restaurantResponse: RestaurantResponse? = null
            try {
                val sb = StringBuilder(Constants.BASE_URL)
                sb.append(Constants.OUTPUT)
                sb.append(Constants.RANK_BY)
                sb.append(Constants.LOCATION + params[0])
                sb.append(Constants.TYPE)
                sb.append(Constants.API_KEY)

                val url = URL(sb.toString())
                conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 7000
                conn.readTimeout = 7000

                Log.e(LOG_TAG, "Url Generated $url")

                val responseCode = conn.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    val inputStream = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuffer()
                    var line: String?
                    do {
                        line = inputStream.readLine()
                        if (line == null)
                            break
                        response.append(line)

                    } while (true)

                    inputStream.close()

                    jsonResults = response.toString()

                    Log.e(LOG_TAG, "Response from API $jsonResults")

                    restaurantResponse = parseResponse(jsonResults)

                } else {
                    Log.e(LOG_TAG, "Error from Connection ${conn.responseCode}")
                }

            } catch (e: MalformedURLException) {
                Log.e(LOG_TAG, "Error processing Places API URL", e)
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Error connecting to Places API", e)
            } finally {
                conn?.disconnect()
            }

            Log.d(LOG_TAG, "Return from DoIn Background")
            return restaurantResponse
        }


        override fun onPostExecute(result: RestaurantResponse?) {
            super.onPostExecute(result)
            restaurantResponseMutableLive.postValue(result)
            Log.d(LOG_TAG, "onPostExecute")

        }

        private fun parseResponse(jsonResults: String): RestaurantResponse? {
            var restaurantResponse: RestaurantResponse? = null
            try {

                val jsonObj = JSONObject(jsonResults)
                val nextToken = jsonObj.getString(Constants.NEXT_TOKEN)
                val resultList = jsonObj.getJSONArray(Constants.RESULTS)
                val status = jsonObj.getString(Constants.STATUS)

                val resultListObj: MutableList<RestaurantResponse.Result> = mutableListOf()
                if (resultList.length() > 0) {
                    for (i in 0 until resultList.length()) {

                        val geometry = resultList.getJSONObject(i).getJSONObject(Constants.GEOMETRY)
                        val location = geometry.getJSONObject(Constants.LOCATION_OBJ)
                        val lat = location.getString(Constants.LAT).toDouble()
                        val lng = location.getString(Constants.LNG).toDouble()

                        val locationObj = RestaurantResponse.Result.Geometry.Location(lat, lng)

                        val geometryObj = RestaurantResponse.Result.Geometry(locationObj)
                        val name = resultList.getJSONObject(i).getString(Constants.NAME)
                        var rating = 0.0
                        if (resultList.getJSONObject(i).has(Constants.RATING)) {
                            rating = resultList.getJSONObject(i).getInt(Constants.RATING).toDouble()
                        }
                        val icon = resultList.getJSONObject(i).getString(Constants.ICON)
                        val id = resultList.getJSONObject(i).getString(Constants.ID)
                        val reference = resultList.getJSONObject(i).getString(Constants.REFERENCE)

                        val photoList: MutableList<RestaurantResponse.Result.Photo> =
                            mutableListOf()
                        if (resultList.getJSONObject(i).has(Constants.PHOTOS)) {
                            val photos = resultList.getJSONObject(i).getJSONArray(Constants.PHOTOS)
                            for (j in 0 until photos.length()) {
                                val width =
                                    photos.getJSONObject(j).getString(Constants.WIDTH).toInt()
                                val height =
                                    photos.getJSONObject(j).getString(Constants.HEIGHT).toInt()
                                val photoReference =
                                    photos.getJSONObject(j).getString(Constants.PHOTO_REFERENCE)
                                val photo =
                                    RestaurantResponse.Result.Photo(height, photoReference, width)
                                photoList.add(photo)
                            }
                        }

                        val result = RestaurantResponse.Result(
                            geometryObj,
                            icon,
                            id,
                            name,
                            photoList,
                            rating,
                            reference
                        )

                        resultListObj.add(result)
                    }
                }
                restaurantResponse = RestaurantResponse(nextToken, resultListObj, status)
                Log.e(LOG_TAG, "Parse Result $restaurantResponse")

            } catch (e: JSONException) {
                Log.e(LOG_TAG, "Error processing JSON results", e)
            }
            Log.d(LOG_TAG, "Response Parse Successfully")
            return restaurantResponse
        }
    }
}

