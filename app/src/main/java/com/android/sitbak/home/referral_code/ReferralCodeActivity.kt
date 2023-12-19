package com.android.sitbak.home.referral_code

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityShareReferralCodeBinding
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Constants
import com.astritveliu.boom.Boom


class ReferralCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityShareReferralCodeBinding
    private lateinit var userData: LoginModel
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_share_referral_code)
        mContext = this

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        userData = AppUtils.getUserDetails(mContext)

        binding.topBar.tvTitle.text = resources.getString(R.string.referral_code_screen_title)
        binding.topBar.ivBack.setOnClickListener { finish() }

        binding.tvYourBonus.text = "$${userData.data.total_bonus}"
        binding.tvReferralCode.text = userData.data.referral_code

        Boom(binding.btnShareCode)
        binding.btnShareCode.setOnClickListener {
            createCode()
        }
    }

    private fun createCode() {
        val builder: Uri.Builder = Uri.Builder()
        builder.scheme("http")
            .authority("www.sitbak.com")
            .appendPath(Constants.referralCode)
            .appendQueryParameter(Constants.referralCode, userData.data.referral_code)
        val myUrl: String = builder.build().toString()


        val intent = Intent(Intent.ACTION_SEND)
        val shareBody = "Here is the link to sitbak app \n\n$myUrl"
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, "Please select one option"))
    }

}