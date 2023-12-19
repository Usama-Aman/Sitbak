package com.android.sitbak.utils

enum class OrdersEnum(private val stringValue: String) {

    MALE("Male"),
    FEMALE("Female");

    override fun toString(): String {
        return stringValue
    }
}