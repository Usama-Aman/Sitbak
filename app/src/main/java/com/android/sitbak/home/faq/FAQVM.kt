package com.android.sitbak.home.faq

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

class FAQVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<FAQModel>>()

    fun getFAQs(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_FAQ)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_FAQ -> {
                val model = Gson().fromJson(jsonObject.toString(), FAQModel::class.java)
                apiResponse.postValue(Resource.success(model, ApiTags.GET_FAQ))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_FAQ -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_FAQ))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<FAQModel>> = apiResponse

}