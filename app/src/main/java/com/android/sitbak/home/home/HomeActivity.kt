package com.android.sitbak.home.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.sitbak.R
import com.android.sitbak.auth.BoardingActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityHomeBinding
import com.android.sitbak.home.news.NewsFragment
import com.android.sitbak.home.order.OrderFragment
import com.android.sitbak.home.popups.AgeAlertPopup
import com.android.sitbak.home.popups.GuestAccessPopUp
import com.android.sitbak.home.profile.ProfileFragment
import com.android.sitbak.home.shops.CartModel
import com.android.sitbak.home.shops.ShopsFragment
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.network.SocketIO
import com.android.sitbak.onesignal.OneSignalNotificationManager
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class HomeActivity : BaseActivity(), View.OnClickListener, GuestAccessPopUp.GuestAccessInterface, NetworkChangeReceiver.ConnectivityReceiverListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var doubleBackToExitPressedOnce = false

    private var mNetworkReceiver: BroadcastReceiver? = null
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)
        AppController.isHomeActive = true

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        setUpViewPager()

        if (!AppController.isGuest) {
            OneSignalNotificationManager.sendUserTags(mContext)
            AppController.userDetails = AppUtils.getUserDetails(mContext).data

            if (AppController.goToTabFromWebView == 1)
                binding.orderTab.callOnClick()

            AppController.socket = SocketIO()
        }

        mNetworkReceiver = NetworkChangeReceiver(this)
        registerNetworkBroadcastForNougat()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_CART)
                        Loader.showLoader(mContext)
                    Loader.progressKHUD?.setCancellable {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.NINETEEN_PLUS -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val data = AppUtils.getUserDetails(mContext)
                            data.data.is_nineteen_plus = 1
                            AppUtils.saveUserModel(mContext, data)
                        }
                        ApiTags.GET_CART -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), CartModel::class.java)

                            if (!model.data.isNullOrEmpty())
                                if (ShopsFragment.binding != null) {
                                    ShopsFragment.binding.topBarLayout.tvCartCount.text = model.data.size.toString()
                                    ShopsFragment.binding.topBarLayout.tvCartCount.viewVisible()
                                }
                        }
                    }
                }
            }
        })
    }

    private fun initViews() {
        checkForIntent(intent)
        boomViews()
        binding.shopsTab.setOnClickListener(this)
        binding.orderTab.setOnClickListener(this)
        binding.newsTab.setOnClickListener(this)
        binding.profileTab.setOnClickListener(this)

        if (!AppController.isGuest) {
            val loginModel = AppUtils.getUserDetails(mContext)
            if (loginModel.data.is_nineteen_plus == 0)
                showAgeAlert()
        }
    }

    private fun boomViews() {
        Boom(binding.shopsTab)
        Boom(binding.orderTab)
        Boom(binding.newsTab)
        Boom(binding.profileTab)
    }

    private fun setUpViewPager() {
        binding.viewPager.adapter = HomeViewPager(this)
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.isUserInputEnabled = false
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.shopsTab -> {
                bottomBarClick(v, 0)
            }
            R.id.orderTab -> {
                if (!AppController.isGuest)
                    bottomBarClick(v, 1)
                else
                    showGuestAccess()
            }
            R.id.newsTab -> {
                bottomBarClick(v, 2)
            }
            R.id.profileTab -> {
                if (!AppController.isGuest)
                    bottomBarClick(v, 3)
                else
                    showGuestAccess()
            }
        }
    }

    private fun bottomBarClick(v: View, position: Int) {
        updateBottomBar(v)
        binding.viewPager.setCurrentItem(position, false)
    }

    private fun updateBottomBar(v: View) {

        when (v.id) {
            R.id.shopsTab -> {
                binding.ivShopsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_shops_selected))
                binding.ivOrderTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_order_unselected))
                binding.ivNewsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_news_unselected))
                binding.ivProfileTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_profile_unselected))
                binding.tvShopsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_selected))
                binding.tvOrderTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvNewsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvProfileTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
            }
            R.id.orderTab -> {
                binding.ivShopsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_shops_unselected))
                binding.ivOrderTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_order_selected))
                binding.ivNewsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_news_unselected))
                binding.ivProfileTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_profile_unselected))
                binding.tvShopsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvOrderTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_selected))
                binding.tvNewsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvProfileTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
            }
            R.id.newsTab -> {
                binding.ivShopsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_shops_unselected))
                binding.ivOrderTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_order_unselected))
                binding.ivNewsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_news_selected))
                binding.ivProfileTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_profile_unselected))
                binding.tvShopsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvOrderTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvNewsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_selected))
                binding.tvProfileTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
            }
            R.id.profileTab -> {
                binding.ivShopsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_shops_unselected))
                binding.ivOrderTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_order_unselected))
                binding.ivNewsTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_news_unselected))
                binding.ivProfileTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_bar_profile_selected))
                binding.tvShopsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvOrderTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvNewsTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_unselected))
                binding.tvProfileTab.setTextColor(ContextCompat.getColor(this, R.color.bottom_bar_selected))
            }
        }
    }

    fun gotoShopping() {
        binding.shopsTab.callOnClick()
    }


    inner class HomeViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ShopsFragment()
                1 -> OrderFragment()
                2 -> NewsFragment()
                else -> ProfileFragment()
            }
        }
    }


    private fun showAgeAlert() {
        val dialog = AgeAlertPopup(this)
        dialog.show(supportFragmentManager, "AgeAlertPopup")
    }

    fun gotoBoarding() {
        val intent = Intent(mContext, BoardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    fun closeApp() {
        finish()
    }

    fun hideOrderTooltip() {
        binding.tvOrderTooltip.viewGone()
    }

    fun showOrderTooltip(text: String, drawable: Int) {
        binding.tvOrderTooltip.viewVisible()
        binding.tvOrderTooltip.text = text
        binding.tvOrderTooltip.background = ContextCompat.getDrawable(mContext, drawable)
    }

    fun setNineteenPlus(date: String) {
        apiCall = retrofitClient.setIsNineteenPlus(date)
        viewModel.setIsNineteenPlus(apiCall)
    }

    private fun showGuestAccess() {
        val dialog = GuestAccessPopUp(this)
        dialog.show(supportFragmentManager, "GuestAccessPopUp")
    }

    override fun guestClickedLogin() {
        gotoBoarding()
    }

    override fun onBackPressed() {
        if (AppController.isGuest) {
            super.onBackPressed()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForIntent(intent)
    }

    private fun checkForIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.hasExtra(Constants.notificationType)) {
                when (intent.getStringExtra(Constants.notificationType)) {
                    NotificationTypes.DriverAcceptDelivery -> {
                        binding.orderTab.callOnClick()
                    }
                }
            }
        }
    }

    private fun getUserCart() {
        apiCall = retrofitClient.getUserCart()
        viewModel.getUserCart(apiCall)
    }


    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        AppController.isHomeActive = true

        if (!AppController.isGuest)
            getUserCart()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppController.isHomeActive = false
        if (AppController.isSocketInitialized())
            AppController.socket.disconnect()

        unregisterNetworkChanges()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (this::snackBar.isInitialized)
                if (snackBar.isShown)
                    snackBar.dismiss()
        } else {
            snackBar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
            snackBar.show()
        }
    }

}