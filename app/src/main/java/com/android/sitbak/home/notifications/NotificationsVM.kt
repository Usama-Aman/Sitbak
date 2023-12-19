package com.android.sitbak.home.notifications

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

class NotificationsVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getNotifications(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_NOTIFICATIONS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_NOTIFICATIONS)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_NOTIFICATIONS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_NOTIFICATIONS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}