package com.android.sitbak.auth.recover_email

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

class RecoverByEmailVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun recoverByEmail(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.RECOVER_BY_EMAIL)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.RECOVER_BY_EMAIL -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RECOVER_BY_EMAIL))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.RECOVER_BY_EMAIL -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RECOVER_BY_EMAIL))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}