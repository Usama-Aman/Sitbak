package com.android.sitbak.auth.current_location

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

class CurrentLocationVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<LoginModel>>()

    fun addUserLocation(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_USER_LOCATION)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.ADD_USER_LOCATION -> {
                val model = Gson().fromJson(jsonObject.toString(), LoginModel::class.java)
                apiResponse.postValue(Resource.success(model, ApiTags.ADD_USER_LOCATION))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.ADD_USER_LOCATION -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_USER_LOCATION))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<LoginModel>> = apiResponse

}