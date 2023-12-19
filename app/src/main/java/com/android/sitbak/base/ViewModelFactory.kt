package com.android.sitbak.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.sitbak.activities.webview.WebViewVM
import com.android.sitbak.auth.add_photo.AddPhotoVM
import com.android.sitbak.auth.current_location.CurrentLocationVM
import com.android.sitbak.auth.login.LoginVM
import com.android.sitbak.auth.mobile_number.MobileNumberVM
import com.android.sitbak.auth.recover_email.RecoverByEmailVM
import com.android.sitbak.auth.recover_phone.RecoverByPhoneVM
import com.android.sitbak.auth.register.RegisterVM
import com.android.sitbak.auth.reset_password.ResetPasswordVM
import com.android.sitbak.auth.verification_code.VerificationCodeVM
import com.android.sitbak.home.active_orders.ActiveOrderFragmentVM
import com.android.sitbak.home.chat.ChatActivityVM
import com.android.sitbak.home.faq.FAQVM
import com.android.sitbak.home.home.HomeVM
import com.android.sitbak.home.location_list.LocationListVM
import com.android.sitbak.home.login_info.LoginInfoVM
import com.android.sitbak.home.news.NewsFragmentVM
import com.android.sitbak.home.notifications.NotificationsVM
import com.android.sitbak.home.order.OrderFragmentVM
import com.android.sitbak.home.past_orders.PastOrderFragment
import com.android.sitbak.home.past_orders.PastOrderFragmentVM
import com.android.sitbak.home.payment_method.PaymentMethodVM
import com.android.sitbak.home.profile.ProfileVM
import com.android.sitbak.home.shop_detail.ShopDetailVM
import com.android.sitbak.home.shop_search_result.ShopSearchResultVM
import com.android.sitbak.home.shops.ShopFragmentVM

@Suppress("UNCHECKED_CAST")
class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OrderFragmentVM::class.java) -> {
                OrderFragmentVM() as T
            }
            modelClass.isAssignableFrom(ShopFragmentVM::class.java) -> {
                ShopFragmentVM() as T
            }
            modelClass.isAssignableFrom(LoginVM::class.java) -> {
                LoginVM() as T
            }
            modelClass.isAssignableFrom(RegisterVM::class.java) -> {
                RegisterVM() as T
            }
            modelClass.isAssignableFrom(MobileNumberVM::class.java) -> {
                MobileNumberVM() as T
            }
            modelClass.isAssignableFrom(VerificationCodeVM::class.java) -> {
                VerificationCodeVM() as T
            }
            modelClass.isAssignableFrom(AddPhotoVM::class.java) -> {
                AddPhotoVM() as T
            }
            modelClass.isAssignableFrom(CurrentLocationVM::class.java) -> {
                CurrentLocationVM() as T
            }
            modelClass.isAssignableFrom(HomeVM::class.java) -> {
                HomeVM() as T
            }
            modelClass.isAssignableFrom(ProfileVM::class.java) -> {
                ProfileVM() as T
            }
            modelClass.isAssignableFrom(LoginInfoVM::class.java) -> {
                LoginInfoVM() as T
            }
            modelClass.isAssignableFrom(LocationListVM::class.java) -> {
                LocationListVM() as T
            }
            modelClass.isAssignableFrom(FAQVM::class.java) -> {
                FAQVM() as T
            }
            modelClass.isAssignableFrom(NewsFragmentVM::class.java) -> {
                NewsFragmentVM() as T
            }
            modelClass.isAssignableFrom(RecoverByEmailVM::class.java) -> {
                RecoverByEmailVM() as T
            }
            modelClass.isAssignableFrom(RecoverByPhoneVM::class.java) -> {
                RecoverByPhoneVM() as T
            }
            modelClass.isAssignableFrom(ResetPasswordVM::class.java) -> {
                ResetPasswordVM() as T
            }
            modelClass.isAssignableFrom(PastOrderFragmentVM::class.java) -> {
                PastOrderFragmentVM() as T
            }
            modelClass.isAssignableFrom(ShopSearchResultVM::class.java) -> {
                ShopSearchResultVM() as T
            }
            modelClass.isAssignableFrom(ShopDetailVM::class.java) -> {
                ShopDetailVM() as T
            }
            modelClass.isAssignableFrom(PaymentMethodVM::class.java) -> {
                PaymentMethodVM() as T
            }
            modelClass.isAssignableFrom(ActiveOrderFragmentVM::class.java) -> {
                ActiveOrderFragmentVM() as T
            }
            modelClass.isAssignableFrom(WebViewVM::class.java) -> {
                WebViewVM() as T
            }
            modelClass.isAssignableFrom(ChatActivityVM::class.java) -> {
                ChatActivityVM() as T
            }
            modelClass.isAssignableFrom(NotificationsVM::class.java) -> {
                NotificationsVM() as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}
