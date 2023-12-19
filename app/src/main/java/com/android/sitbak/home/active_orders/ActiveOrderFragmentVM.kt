package com.android.sitbak.home.active_orders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.base.AppController
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.network.SocketIO
import com.android.sitbak.utils.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ActiveOrderFragmentVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()
    private val orderStatusResponse = MutableLiveData<JSONObject>()
    private val driverLocationResponse = MutableLiveData<JSONObject>()

    init {
        if (!AppController.isGuest)
            listenSocket()
    }

    fun getOrders(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_ACTIVE_ORDERS)
    }

    fun postOrderRating(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.POST_ORDER_RATING)
    }

    private fun listenSocket() {
        try {

            AppController.socket.on(SocketIO.socketOrderGetStatus) { obj ->
                Log.e("socket order", obj.toString())
                try {
                    val jsonObject = JSONObject(obj.toString())
                    orderStatusResponse.postValue(jsonObject)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            AppController.socket.on(SocketIO.socketGetDriverLocation) { obj ->
                Log.e("socket driver location", obj.toString())
                try {
                    val jsonObject = JSONObject(obj.toString())
                    driverLocationResponse.postValue(jsonObject)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_ACTIVE_ORDERS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_ACTIVE_ORDERS))
            }
            ApiTags.POST_ORDER_RATING -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.POST_ORDER_RATING))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
    fun getOrderStatusResponse(): LiveData<JSONObject> = orderStatusResponse
    fun getDriverLocationResponse(): LiveData<JSONObject> = driverLocationResponse

}