package com.android.sitbak.auth.mobile_number

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

class MobileNumberVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<String>>()

    fun addUpdatePhoneNumber(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_UPDATE_PHONE)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.ADD_UPDATE_PHONE -> {
                apiResponse.postValue(Resource.success(jsonObject.getString("message"), ApiTags.ADD_UPDATE_PHONE))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.ADD_UPDATE_PHONE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_UPDATE_PHONE))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<String>> = apiResponse

}