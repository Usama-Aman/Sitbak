package com.android.sitbak.home.faq

data class FAQModel(
    var `data`: ArrayList<FAQData>?,
    var message: String?,
    var status: Boolean?
)

data class FAQData(
    var answer: String?,
    var question: String?
)