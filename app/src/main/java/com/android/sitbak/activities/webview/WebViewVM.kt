package com.android.sitbak.activities.webview

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

class WebViewVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getPrivacy(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_PRIVACY)
    }

    fun getTermsConditions(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_TERMS_CONDITION)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_PRIVACY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PRIVACY))
            }
            ApiTags.GET_TERMS_CONDITION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_TERMS_CONDITION))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }


    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}