package com.android.sitbak.home.past_orders

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

class PastOrderFragmentVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getOrders(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_PAST_ORDERS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_PAST_ORDERS)
    }

   fun setFavouriteOrder(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.SET_FAV_ORDER)
        RetrofitClient.apiCall(call, this, ApiTags.SET_FAV_ORDER)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_PAST_ORDERS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PAST_ORDERS))
            }
            ApiTags.SET_FAV_ORDER -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SET_FAV_ORDER))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}