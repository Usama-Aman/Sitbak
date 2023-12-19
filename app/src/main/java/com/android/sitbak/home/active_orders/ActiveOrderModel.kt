package com.android.sitbak.home.active_orders

import android.os.Parcel
import android.os.Parcelable

data class ActiveOrderModel(
    val `data`: ArrayList<ActiveOrderData>,
    val message: String,
    val status: Boolean
)

data class ActiveOrderData(
    val created_at: String,
    val delivery: ActiveOrdersDeliver,
    val delivery_charges: String,
    val discount: String,
    val driver_tip: String,
    val end_time: String,
    val grand_total: String,
    val id: Int,
    val items: Int,
    val start_time: String,
    var status: String,
    var updated_time: String,
//    val delivery_status: String,
    val store: ActiveOrderStore,
    val sub_total: String,
    val tax: String
)

data class ActiveOrderStore(
    val name: String,
    val photo_path: String
)

data class ActiveOrdersDeliver(
    val address: String,
    val driver: ActiveOrdersDriver,
    val driver_payment_status: String,
    val end_at: Any,
    val id: Int,
    var latitude: String,
    var longitude: String,
    val start_at: Any,
    val status: String
)

data class ActiveOrdersDriver(
    val email: String?,
    val id: Int,
    val name: String?,
    val phone_number: String?,
    val photo_path: String?,
    val region: String?,
    val verify_photo: String?,
    val verify_photo_path: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(phone_number)
        parcel.writeString(photo_path)
        parcel.writeString(region)
        parcel.writeString(verify_photo)
        parcel.writeString(verify_photo_path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActiveOrdersDriver> {
        override fun createFromParcel(parcel: Parcel): ActiveOrdersDriver {
            return ActiveOrdersDriver(parcel)
        }

        override fun newArray(size: Int): Array<ActiveOrdersDriver?> {
            return arrayOfNulls(size)
        }
    }
}