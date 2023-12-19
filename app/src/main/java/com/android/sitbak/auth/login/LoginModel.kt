package com.android.sitbak.auth.login

import android.os.Parcel
import android.os.Parcelable

data class LoginModel(
    val `data`: LoginData,
    val message: String,
    val status: Boolean,
    var access_token: String,
)

data class LoginData(
    var allow_notifications: Int,
    var default_location: DefaultLocation?,
    var default_payment_method: DefaultPaymentMethod,
    var email: String,
    var email_verified_at: String?,
    var id: Int,
    var id_photo_path: String?,
    var is_age_verified: Int,
    var is_nineteen_plus: Int,
    var name: String,
    var phone_number: String,
    var phone_verified_at: String?,
    var photo_path: String?,
    var referral_code: String,
    var country_code: String,
    var total_bonus: String
)

data class DefaultLocation(
    val address: String,
    val id: Int,
    val is_default: Int,
    val latitude: String,
    val longitude: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeInt(id)
        parcel.writeInt(is_default)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DefaultLocation> {
        override fun createFromParcel(parcel: Parcel): DefaultLocation {
            return DefaultLocation(parcel)
        }

        override fun newArray(size: Int): Array<DefaultLocation?> {
            return arrayOfNulls(size)
        }
    }
}

data class DefaultPaymentMethod(
    val card_type: String,
    val exp_month: Int,
    val exp_year: Int,
    val id: Int,
    val is_default: Int,
    val last4: String
)