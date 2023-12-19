package com.android.sitbak.auth

import android.content.Intent
import android.os.Bundle
import com.android.sitbak.R
import com.android.sitbak.activities.webview.WebViewActivity
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.auth.register.RegisterActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityBoardingBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference
import com.astritveliu.boom.Boom

class BoardingActivity : BaseActivity() {
    private lateinit var binding: ActivityBoardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor(R.color.white)
        blackStatusBarIcons()
        listener()

        boomViews()
    }

    private fun boomViews() {
        Boom(binding.btnCreateAccount)
        Boom(binding.btnLogin)
        Boom(binding.btnGuest)
        Boom(binding.tvTermsOfService)
        Boom(binding.tvPrivacy)

    }

    private fun listener() {
        binding.btnLogin.setOnClickListener {
            AppController.isGuest = false
            startActivity(Intent(this@BoardingActivity, LoginActivity::class.java))

        }
        binding.btnCreateAccount.setOnClickListener {
            AppController.isGuest = false
            startActivity(Intent(this@BoardingActivity, RegisterActivity::class.java))
        }
        binding.btnGuest.setOnClickListener {
            AppController.isGuest = true

            val intent = Intent(this, HomeActivity::class.java)
            SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, false)
            startActivity(intent)
        }
        binding.tvTermsOfService.setOnClickListener {
            startWebView(resources.getString(R.string.terms_screen_title), "https://www.google.com")
        }

        binding.tvPrivacy.setOnClickListener {
            startWebView(resources.getString(R.string.privacy_screen_title), "https://www.google1.com")
        }
    }

    private fun startWebView(title: String, url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("url", url)
        startActivity(intent)
    }
}