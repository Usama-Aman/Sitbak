package com.android.sitbak.home.shops

data class StoresModel(
    var `data`: List<StoresData>,
    var message: String,
    var status: Boolean,
    var stores_count: Int
)

data class StoresData(
    var delivery: String,
    var delivery_time: Int,
    var distance: String,
    var email: String,
    var id: Int,
    var name: String,
    var rating: String,
    var photo_path: String,
)