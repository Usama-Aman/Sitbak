package com.android.sitbak.home.payment_method

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

class PaymentMethodVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun addUserCard(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_USER_CARD)
    }

    fun getUserCards(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_USER_CARDS)
    }
    fun deleteUserCard(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_USER_CARD)
    }
    fun updateUserDefaultCard(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_USER_DEFAULT_CARD)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.ADD_USER_CARD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_USER_CARD))
            }
            ApiTags.GET_USER_CARDS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_USER_CARDS))
            }
            ApiTags.DELETE_USER_CARD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_USER_CARD))
            }
            ApiTags.UPDATE_USER_DEFAULT_CARD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_USER_DEFAULT_CARD))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.ADD_USER_CARD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_USER_CARD))
            }
            ApiTags.GET_USER_CARDS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_USER_CARDS))
            }
            ApiTags.DELETE_USER_CARD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.DELETE_USER_CARD))
            }
            ApiTags.UPDATE_USER_DEFAULT_CARD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_USER_DEFAULT_CARD))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}