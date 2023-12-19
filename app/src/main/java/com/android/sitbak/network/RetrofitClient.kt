package com.android.sitbak.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.sitbak.R
import com.android.sitbak.auth.BoardingActivity
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    private const val BASE_URL = "http://178.128.29.7/sitbak/api/v1/"
    private lateinit var context: Context

    interface CryptoStrategy {
        fun encrypt(body: String): String
        fun decrypt(data: String): String
    }

    fun getClient(context: Context): Retrofit {

        val token = SharedPreference.getSimpleString(context, Constants.accessToken)
        RetrofitClient.context = context

        // HttpLoggingInterceptor
        val httpLoggingInterceptor = HttpLoggingInterceptor { message -> Log.i("", message) }
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                    .build()
                chain.proceed(newRequest)
            }.build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getClientNoToken(context: Context): Retrofit {
        RetrofitClient.context = context

        // HttpLoggingInterceptor
        val httpLoggingInterceptor = HttpLoggingInterceptor { message -> Log.i("", message) }
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun apiCall(call: Call<ResponseBody>, responseCallBack: ResponseCallBack, tag: String) {

        if (!AppUtils.isNetworkAvailable(context)) {
            responseCallBack.onError(context.resources.getString(R.string.no_internet_connection), tag)
            return
        }

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.isSuccessful) {
                        responseCallBack.onSuccess(JSONObject(response.body()!!.string()), tag)
                    } else {
                        println("Exception in api $response")
                        val errorJSONObject = JSONObject(response.errorBody()!!.string())

                        if (response.code() == 401) {
                            val intent = Intent(context, BoardingActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            SharedPreference.saveBoolean(context, Constants.isUserLoggedIn, false)
                            context.startActivity(intent)
                        } else
                            if (!call.isCanceled)
                                responseCallBack.onError(errorJSONObject.getString("message"), tag)
                        Log.d("Exception", errorJSONObject.getString("message"))

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Response Failure", t.localizedMessage!!)
                if (!call.isCanceled)
                    responseCallBack.onError(t.localizedMessage!!, tag)
            }
        })
    }


}