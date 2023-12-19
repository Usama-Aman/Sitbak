package com.android.sitbak.home.payment_method

data class PaymentCardModel(
    val `data`: List<PaymentCardData>,
    val message: String,
    val status: Boolean
)

data class PaymentCardData(
    val card_type: String,
    val exp_month: Int,
    val exp_year: Int,
    val id: Int,
    var is_default: Int,
    val last4: String,
    val isAddCard: Boolean = false,
)