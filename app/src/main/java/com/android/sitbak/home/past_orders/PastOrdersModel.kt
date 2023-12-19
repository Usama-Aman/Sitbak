package com.android.sitbak.home.past_orders

import java.math.BigInteger

data class PastOrdersModel(
    val `data`: ArrayList<PastOrdersData>,
    val message: String,
    val status: Boolean
)

data class PastOrdersData(
    val created_at: String,
    val delivery: PastOrderDelivery,
    val delivery_charges: String,
    val discount: String,
    val driver_tip: String,
    val grand_total: String,
    val id: Int,
    val items: Int,
    val product_id: BigInteger,
    val status: String,
    val store: PastOrdersStore,
    val sub_total: String,
    val tax: String,
    var is_favourite: Int,
)

data class PastOrderDelivery(
    val address: String,
    val end_at: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val start_at: String,
    val status: String
)

data class PastOrdersStore(
    val id: Int,
    val name: String,
    val photo_path: String
)