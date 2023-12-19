package com.android.sitbak.home.shops

data class CartModel(
    val `data`: List<CartData>,
    val message: String,
    val status: Boolean
)

data class CartData(
    val id: Int,
    val image: String,
    val price: String,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val store_id: Int,
    val taxable: Int,
    val unit: String,
    val user_id: Int,
    val variant_id: String,
    val weight: Int
)