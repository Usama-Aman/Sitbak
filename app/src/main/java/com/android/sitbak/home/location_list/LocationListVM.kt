package com.android.sitbak.home.location_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.home.faq.FAQData
import com.android.sitbak.home.faq.FAQModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class LocationListVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getUserLocations(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_USER_LOCATIONS)
    }

    fun deleteUserLocation(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_USER_LOCATION)
    }

    fun addUserLocation(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_USER_LOCATION)
    }

    fun setAsDefault(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.SET_AS_DEFAULT_LOCATION)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_USER_LOCATIONS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_USER_LOCATIONS))
            }
            ApiTags.DELETE_USER_LOCATION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_USER_LOCATION))
            }
            ApiTags.ADD_USER_LOCATION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_USER_LOCATION))
            }
            ApiTags.SET_AS_DEFAULT_LOCATION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SET_AS_DEFAULT_LOCATION))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }
//
//    fun removeLocation(selectedPosition: Int) {
//        locationListData.value?.removeAt(selectedPosition)
//        locationListData.postValue(locationListData.value)
//    }
//
//    fun clearLocationsList() {
//        locationListData.value?.clear()
//        locationListData.postValue(locationListData.value)
//    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}