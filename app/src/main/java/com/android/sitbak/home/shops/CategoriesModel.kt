package com.android.sitbak.home.shops

data class CategoriesModel(
    var `data`: ArrayList<CategoriesData>,
    var message: String,
    var status: Boolean
)

data class CategoriesData(
    var category_description: String,
    var category_title: String,
    var id: Int,
    var image: String,
    var image_path: String,
    var isSelected: Int = 0
)