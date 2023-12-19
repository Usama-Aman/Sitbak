package com.android.sitbak.home.location_bottom_sheet

data class LocationBottomSheetModel(
    var `data`: ArrayList<LocationBottomSheetData>,
    var message: String?,
    var status: Boolean?
)

data class LocationBottomSheetData(
    var address: String?,
    var id: Int?,
    var is_default: Int?,
    var latitude: String?,
    var longitude: String?,
)