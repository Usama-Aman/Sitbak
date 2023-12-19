package com.android.sitbak.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginModel
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter


object AppUtils {

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun createDrawableFromView(context: Context, imageUrl: String): BitmapDescriptor? {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_marker_with_image, null)
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay
            .getMetrics(displayMetrics)

        val ivMarkerImageView: ImageView = view.findViewById(R.id.ivMarkerImage)

        Glide.with(context).load(imageUrl).placeholder(R.drawable.ic_image_placeholder).into(ivMarkerImageView)

        view.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(
            0, 0, displayMetrics.widthPixels,
            displayMetrics.heightPixels
        )
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    fun hideKeyBoardFromEdittext(editext: EditText, context: Context) {
        val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editext.windowToken, 0)
    }

    fun showKeyboardOnEdittext(editText: EditText, context: Context) {
        val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    //Hide show password
    fun hideShowPassword(editText: EditText, toggle: ImageView) {
        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            toggle.setImageResource(R.drawable.ic_hide_password)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            toggle.setImageResource(R.drawable.ic_show_password)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        editText.setSelection(editText.text.toString().length)
    }

    fun showToast(activity: Activity, text: String, isSuccess: Boolean) {
        if (isSuccess) {
            Alerter.create(activity)
                .setTitle(activity.resources.getString(R.string.toast_success))
                .setTitleTypeface(Typeface.createFromAsset(activity.assets, "inter_bold.ttf"))
                .setTextTypeface(Typeface.createFromAsset(activity.assets, "inter_regular.ttf"))
                .setText(text)
                .setDuration(1000)
                .setIcon(R.drawable.ic_toast_success)
                .setBackgroundColorRes(R.color.app_color)
                .show()
        } else {
            Alerter.create(activity)
                .setTitle(activity.resources.getString(R.string.toast_error))
                .setTitleTypeface(Typeface.createFromAsset(activity.assets, "inter_bold.ttf"))
                .setTextTypeface(Typeface.createFromAsset(activity.assets, "inter_regular.ttf"))
                .setText(text)
                .setDuration(1000)
                .setIcon(R.drawable.ic_toast_error)
                .setBackgroundColorRes(R.color.red)
                .show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return (netInfo != null && netInfo.isConnectedOrConnecting
                && cm.activeNetworkInfo!!.isAvailable
                && cm.activeNetworkInfo!!.isConnected)
    }

    fun preventDoubleClick(view: View) {
        view.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            view.isEnabled = true
        }, 200)
    }

    fun touchScreenEnableDisable(context: Context, isTouchEnable: Boolean) {
        if (isTouchEnable)
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        else
            (context as Activity).window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun getUserDetails(context: Context): LoginModel {
        val loginModel = SharedPreference.getSimpleString(context, Constants.userModel)
        return Gson().fromJson(loginModel, LoginModel::class.java)
    }

    fun saveUserModel(mContext: Context, data: LoginModel) {
        SharedPreference.saveSimpleString(mContext, Constants.userModel, Gson().toJson(data))
    }

    fun isOnline(): Boolean {
        try {
            val p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com")
            val returnVal = p1.waitFor()
            return returnVal == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}