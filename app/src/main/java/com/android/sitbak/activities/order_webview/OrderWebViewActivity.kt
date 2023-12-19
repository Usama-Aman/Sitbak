package com.android.sitbak.activities.order_webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityOrderWebviewBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.payment_method.PaymentMethodActivity
import com.android.sitbak.utils.*


class OrderWebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderWebviewBinding
    private lateinit var mContext: Context

    companion object {
        const val to_catalog = "http://178.128.29.7/to_catalog"
        const val to_active_orders = "http://178.128.29.7/to_active_orders"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_webview)
        mContext = this

        changeStatusBarColor(R.color.white)
        blackStatusBarIcons()

        initViews()
        initListeners()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initViews() {
        val url: String? = intent.getStringExtra("url")

        if (url.isNullOrBlank())
            finish()

        Log.d("ORDER URL", url!!)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(url)


        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Loader.showLoader(mContext)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Loader.hideLoader()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url.toString())

                if (url.toString().contains("web_views_product")) {
                    binding.ivBack.viewVisible()
                } else
                    binding.ivBack.viewGone()

                when {
                    url.toString().contains("to_active_orders") -> {
                        AppController.goToTabFromWebView = 1

                        val intent = Intent(mContext, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                        startActivity(intent)
                    }
                    url.toString().contains("to_catalog") -> {
                        AppController.goToTabFromWebView = 0

                        val intent = Intent(mContext, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                        startActivity(intent)
                    }
                    url.toString().contains("web_view_add_payment_methods") -> {

                        if (binding.webView.url?.contains("web_view_add_payment_methods") == true)
                            binding.webView.goBack()

                        val intent = Intent(mContext, PaymentMethodActivity::class.java)
                        startActivity(intent)
                    }
                    url.toString().contains("return_back") -> {
                        if (binding.webView.canGoBack())
                            binding.webView.goBack()
                        else
                            finish()
                    }
                }
                return false
            }
        }

    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            finish()
    }

}

