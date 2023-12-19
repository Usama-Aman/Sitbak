package com.android.sitbak.base

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.sitbak.R
import com.android.sitbak.utils.HideUtil
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import java.util.*

open class BaseActivity : AppCompatActivity() {

    private var alert: androidx.appcompat.app.AlertDialog? = null
    lateinit var mViewGroup: ViewGroup
    var fontBold: Typeface? = null
    var fontSemiBold: Typeface? = null
    var fontRegular: Typeface? = null

//    open lateinit var sp: SharedPreferences

    open fun onSetupViewGroup(mVG: ViewGroup) {
        mViewGroup = mVG
        HideUtil.init(this, mViewGroup)
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect1(customView: Any): AnimatorSet {

        var animatorSet = AnimatorSet();
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeIn).after(fadeOut)


        return animatorSet


    }

    fun String.isValidEmail(): Boolean =
        this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidMobileNumber(): Boolean = this.isNotEmpty() && Patterns.PHONE.matcher(this).matches()

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect2(customView: Any) {

        var animatorSet = AnimatorSet()
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeOut)
        animatorSet.start()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        initFonts()

    }

    public fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }


    fun fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /*  val actionBar: ActionBar? = supportActionBar
          actionBar?.hide()*/
    }

    private fun initFonts() {
        fontBold = ResourcesCompat.getFont(this, R.font.inter_bold)
        fontSemiBold = ResourcesCompat.getFont(this, R.font.inter_semibold)
        fontRegular =
            ResourcesCompat.getFont(this, R.font.inter_regular)
    }

    fun clearLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.setStatusBarColor(
                ContextCompat
                    .getColor(activity, R.color.black)
            )
        }
    }

    fun blackStatusBarIcons() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun showInfoToast(context: Context, message: String) {
        Toasty.info(context, "" + message, Toast.LENGTH_SHORT, true).show();
    }

    @SuppressLint("InlinedApi")
    fun changeStatusBarColor(resourseColor: Int) {

        val window: Window = window

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(applicationContext, resourseColor)
        }

    }


    open fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }

    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showGPSNotEnabledDialog(context: Context) {
        alert = androidx.appcompat.app.AlertDialog.Builder(context)
            .setMessage("Please enable Gps")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                alert?.dismiss()
            }
            .show()
    }

}