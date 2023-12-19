package com.android.sitbak.home.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ProfileVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun logoutUser(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.LOGOUT)
    }

    fun deleteAccount(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_ACCOUNT)
    }

    fun getProfile(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_PROFILE)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.LOGOUT))
            }
            ApiTags.DELETE_ACCOUNT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_ACCOUNT))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PROFILE))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.LOGOUT))
            }
            ApiTags.DELETE_ACCOUNT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.DELETE_ACCOUNT))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PROFILE))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}