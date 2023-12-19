package com.android.sitbak.home.shop_detail

data class ShopDetailCategoryModel(
    val `data`: List<ShopDetailCategoryData>,
    val message: String,
    val status: Boolean
)

data class ShopDetailCategoryData(
    val category_description: String,
    val category_title: String,
    val id: Int,
    val image: String,
    val image_path: String
)