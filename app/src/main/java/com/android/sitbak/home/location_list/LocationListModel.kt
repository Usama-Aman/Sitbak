package com.android.sitbak.home.location_list

data class LocationListModel(
    var `data`: ArrayList<LocationListData>,
    var message: String?,
    var status: Boolean?
)

data class LocationListData(
    var address: String?,
    var id: Int?,
    var is_default: Int?,
    var latitude: String?,
    var longitude: String?
)