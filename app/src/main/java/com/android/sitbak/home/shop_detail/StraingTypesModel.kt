package com.android.sitbak.home.shop_detail

data class StraingTypeModel(
    val `data`: List<StraingTypeData>,
    val message: String,
    val status: Boolean
)

data class StraingTypeData(
    val id: Int,
    val image: String,
    val image_path: String,
    val type_description: String,
    val type_title: String
)