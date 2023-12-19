package com.android.sitbak.network

import com.android.sitbak.base.AppController
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface Api {

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") device_id: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
        @Field("referral_code") referral_code: String,
        @Field("device_id") device_id: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/verify")
    fun emailPhoneConfirmation(
        @Field("type") type: String,
        @Field("otp") otp: String,
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("reset_password")
    fun resetPassword(
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
        @Field("email") email: String,
        @Field("phone_number") phone_number: String,
        @Field("otp") otp: String,
        @Field("type") type: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/update_phone_number")
    fun addUpdatePhoneNumber(
        @Field("country_code") country_code: String,
        @Field("phone_number") phone_number: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("resend_otp")
    fun sendPhoneOTP(
        @Field("phone_number") phone_number: String,
        @Field("email") email: String,
        @Field("type") type: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("reset_password/send_otp")
    fun senOTPForgotPassword(
        @Field("phone_number") phone_number: String,
        @Field("email") email: String,
        @Field("type") type: String,
    ): Call<ResponseBody>


    @Multipart
    @POST("user/update_photos")
    fun addUpdateUserPhotos(
        @Part("type") photo_type: RequestBody,
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/locations")
    fun addUserLocation(
        @Field("address") address: String,
        @Field("latitude") longitude: String,
        @Field("longitude") latitude: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/is_nineteen_plus")
    fun setIsNineteenPlus(
        @Field("date_of_birth") date_of_birth: String,
    ): Call<ResponseBody>

    @POST("user/logout")
    fun logoutUser(): Call<ResponseBody>

    @POST("user/delete_account")
    fun deleteAccount(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/password")
    fun updateUserPassword(
        @Field("old_password") old_password: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/update_email")
    fun updateUserEmail(
        @Field("email") email: String,
    ): Call<ResponseBody>

    @GET("user/locations")
    fun getUserLocations(): Call<ResponseBody>

    @DELETE("user/locations/{id}")
    fun deleteUserLocation(
        @Path("id") locationId: Int
    ): Call<ResponseBody>

    @POST("user/location/set_default")
    fun setDefaultUserLocation(
        @Query("id") locationId: Int
    ): Call<ResponseBody>

    @GET("user/faq")
    fun getFAQs(): Call<ResponseBody>

    @GET("user/news")
    fun getNews(): Call<ResponseBody>

    @GET("user/products_categories")
    fun getCategoriesUser(): Call<ResponseBody>

    @GET("guest/products_categories")
    fun getCategoriesGuest(): Call<ResponseBody>

    @GET("user/cart")
    fun getUserCart(): Call<ResponseBody>


    @GET("user/stores")
    fun getStoresUser(
        @Query("category") category: Int,
        @Query("name") name: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("sort_by") sort_by: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount,
    ): Call<ResponseBody>

    @GET("guest/stores")
    fun getStoresGuest(
        @Query("category") category: Int,
        @Query("name") name: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("sort_by") sort_by: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount,
    ): Call<ResponseBody>

    @GET("user/orders")
    fun getOrders(
        @Query("type") type: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount,
    ): Call<ResponseBody>

    @GET("user/favourite_order/{order_id}")
    fun setFavouriteOrder(@Path("order_id") order_id: Int): Call<ResponseBody>


    @GET("user/get_profile")
    fun getUserProfile(): Call<ResponseBody>

    @GET("user/store_categories/{id}")
    fun getProductCategoriesUser(@Path("id") store_id: Int): Call<ResponseBody>

    @GET("guest/store_categories/{id}")
    fun getProductCategoriesGuest(@Path("id") store_id: Int): Call<ResponseBody>

    @GET("user/products/{id}")
    fun getStoreProductsUser(
        @Path("id") store_id: Int,
        @Query("category_title") category_title: String,
        @Query("product_type") product_type: String,
        @Query("title") title: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount,
    ): Call<ResponseBody>

    @GET("guest/products/{id}")
    fun getStoreProductsGuest(
        @Path("id") store_id: Int,
        @Query("category_title") category_title: String,
        @Query("product_type") product_type: String,
        @Query("title") title: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount,
    ): Call<ResponseBody>

    @GET("user/products_types")
    fun getStraingTypesUser(): Call<ResponseBody>

    @GET("guest/products_types")
    fun getStraingTypesGuest(): Call<ResponseBody>


    @GET("user/cards")
    fun getUserCards(): Call<ResponseBody>


    @FormUrlEncoded
    @POST("user/cards")
    fun addUserCard(@Field("token") token: String): Call<ResponseBody>

    @DELETE("user/cards/{id}")
    fun deleteUserCard(@Path("id") id: Int): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/cards/update_default_card")
    fun updateUserDefaultCard(@Field("id") id: Int): Call<ResponseBody>


    @GET("user/pages/{page_name}")
    fun getWebPages(@Path("page_name") page_name: String): Call<ResponseBody>

    @GET("user/chats/{order_id}")
    fun getChats(@Path("order_id") order_id: Int): Call<ResponseBody>

    @GET("user/notifications")
    fun getNotifications(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/chats")
    fun sendChatMessage(
        @Field("type") type: String,
        @Field("order_id") order_id: Int,
        @Field("message") message: String,
        @Field("sender_type") sender_type: String = "user",
    ): Call<ResponseBody>

    @POST("user/rating")
    @FormUrlEncoded
    fun postOrderRating(
        @Field("retailer_rating") retailer_rating: Double,
        @Field("driver_rating") driver_rating: Double,
        @Field("order_id") order_id: Int,
    ): Call<ResponseBody>

}





















