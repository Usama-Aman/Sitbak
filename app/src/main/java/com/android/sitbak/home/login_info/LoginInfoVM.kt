package com.android.sitbak.home.login_info

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

class LoginInfoVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun updateUserPassword(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_PASSWORD)
    }

    fun updateUserEmail(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_EMAIL)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.UPDATE_PASSWORD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_PASSWORD))
            }
            ApiTags.UPDATE_EMAIL -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_EMAIL))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.UPDATE_PASSWORD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_PASSWORD))
            }
            ApiTags.UPDATE_EMAIL -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_EMAIL))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}