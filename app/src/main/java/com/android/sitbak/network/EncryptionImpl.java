package com.android.sitbak.network;

public class EncryptionImpl implements RetrofitClient.CryptoStrategy {

    @Override
    public String encrypt(String body) {
        String e = "";
        try {
            e = CryptoUtil.encrypt(body);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return e;
    }

    @Override
    public String decrypt(String data) {
        return null;
    }
}