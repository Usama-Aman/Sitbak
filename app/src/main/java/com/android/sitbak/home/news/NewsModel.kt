package com.android.sitbak.home.news

data class NewsModel(
    var `data`: ArrayList<NewsData>,
    var message: String,
    var status: Boolean
)

data class NewsData(
    var created_at: String,
    var detail: String,
    var id: Int,
    var image: String,
    var image_path: String,
    var is_heading: Int,
    var title: String,
)