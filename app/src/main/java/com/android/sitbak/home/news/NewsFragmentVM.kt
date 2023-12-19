package com.android.sitbak.home.news

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

class NewsFragmentVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getNews(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_NEWS)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_NEWS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_NEWS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_NEWS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_NEWS))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}