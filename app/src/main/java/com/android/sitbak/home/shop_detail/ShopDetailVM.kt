package com.android.sitbak.home.shop_detail

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

class ShopDetailVM : ViewModel(), ResponseCallBack {


    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getProductCategories(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_PRODUCT_CATEGORIES)
    }
    fun getStoreProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_STORE_PRODUCTS)
    }
    fun getStraingTypes(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_STRAING_TYPES)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_STORE_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STORE_PRODUCTS))
            }
            ApiTags.GET_PRODUCT_CATEGORIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PRODUCT_CATEGORIES))
            }
            ApiTags.GET_STRAING_TYPES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STRAING_TYPES))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_STORE_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_STORE_PRODUCTS))
            }
            ApiTags.GET_PRODUCT_CATEGORIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PRODUCT_CATEGORIES))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse


}