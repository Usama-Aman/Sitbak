package com.android.sitbak.home.shops

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ShopFragmentVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getCategories(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_CATEGORIES)
    }


    fun getStores(call: Call<ResponseBody>, tag: String) {
        RetrofitClient.apiCall(call, this, tag)
    }

    fun getUserCart(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CART)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CART)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_CATEGORIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CATEGORIES))
            }
            ApiTags.GET_STORES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STORES))
            }
            ApiTags.GET_STORES_SEARCH -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STORES_SEARCH))
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