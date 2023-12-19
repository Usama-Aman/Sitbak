package com.android.sitbak.home.shop_search_result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.home.shops.CategoriesData
import com.android.sitbak.home.shops.CategoriesModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ShopSearchResultVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getStores(call: Call<ResponseBody>, tag: String) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, tag)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_STORES_SEARCH -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STORES_SEARCH))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}