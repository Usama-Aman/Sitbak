package com.android.sitbak.network;

public class DecryptionImpl implements RetrofitClient.CryptoStrategy {
    @Override
    public String encrypt(String body) {
        return null;
    }

    @Override
    public String decrypt(String data) {
        String d = "";
        try {
            d = CryptoUtil.decrypt(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }


}