package com.android.sitbak.home.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class HomeVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun setIsNineteenPlus(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.NINETEEN_PLUS)
    }

    fun getUserCart(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CART)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CART)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.NINETEEN_PLUS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.NINETEEN_PLUS))
            }
            ApiTags.GET_CART -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CART))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}