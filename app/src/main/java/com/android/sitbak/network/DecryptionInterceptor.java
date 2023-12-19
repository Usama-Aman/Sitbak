package com.android.sitbak.network;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DecryptionInterceptor implements Interceptor {

    private final RetrofitClient.CryptoStrategy mDecryptionStrategy;

    //injects the type of decryption to be used
    public DecryptionInterceptor(RetrofitClient.CryptoStrategy mDecryptionStrategy) {
        this.mDecryptionStrategy = mDecryptionStrategy;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Log.i("", " == =============DECRYPTING RESPONSE===============");

        Response response = chain.proceed(chain.request());
        if (response.isSuccessful()) {
            Response.Builder newResponse = response.newBuilder();
            String contentType = response.header("Content-Type");
            if (TextUtils.isEmpty(contentType)) contentType = "application/json";
            String responseString = response.body().string();


            String decryptedString = null;
            if (mDecryptionStrategy != null) {
                try {
                    decryptedString = mDecryptionStrategy.decrypt(responseString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.i("Response string => %s", responseString);
                Log.i("Decrypted BODY=> %s", decryptedString);
            } else {
                throw new IllegalArgumentException("No decryption strategy!");
            }
            newResponse.body(ResponseBody.create(MediaType.parse(contentType), decryptedString));
            return newResponse.build();
        }
        return response;
    }
}