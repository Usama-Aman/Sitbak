package com.android.sitbak.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import com.android.sitbak.auth.login.LoginData
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.network.SocketIO
import com.android.sitbak.onesignal.OneSignalNotificationHandler
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference
import com.google.gson.Gson
import com.onesignal.OneSignal

class AppController : Application() {

    companion object {
        var isGuest = false
        var isHomeActive = false

        var deviceAppUID = ""
        var inviteReferralCode: String? = ""

        var resetOTP = 0
        var pageCount = 10
        var goToTabFromWebView = 0

        var userDetails: LoginData? = null

        @SuppressLint("StaticFieldLeak")
        var instance: AppController? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var socket: SocketIO

        fun isSocketInitialized(): Boolean = ::socket.isInitialized
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        deviceAppUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        instance = this
        context = this

        OneSignal.startInit(this)
            .filterOtherGCMReceivers(true)
            .disableGmsMissingPrompt(false)
            .autoPromptLocation(false) // default call promptLocation later
            .setNotificationReceivedHandler(OneSignalNotificationHandler(this@AppController))
            .setNotificationOpenedHandler(OneSignalNotificationHandler(this@AppController))
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .init()

    }


}