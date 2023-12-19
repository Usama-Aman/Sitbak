package com.android.sitbak.auth.register

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.android.sitbak.R
import com.android.sitbak.auth.verification_code.VerificationCodeActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivitySignUpWithEmailConfirmationBinding
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference

class EmailConfirmationActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpWithEmailConfirmationBinding
    private lateinit var mContext: Context

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpWithEmailConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()

        initViews()
        initListeners()
    }

    private fun initViews() {
        binding.topBarLayout.tvTitle.text = resources.getString(R.string.sign_up_with_email)
        userEmail = intent?.getStringExtra("email")!!

        if (userEmail.isNotBlank())
            binding.tvEmail.text = userEmail

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            binding.authProgressBar.setProgress(SharedPreference.getInteger(mContext, Constants.authProgress, 0), true)
        else
            binding.authProgressBar.progress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)
    }

    private fun initListeners() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.llContinue.setOnClickListener {
            val intent = Intent(mContext, VerificationCodeActivity::class.java)
            intent.putExtra("phoneOrEmail", binding.tvEmail.text.toString())
            intent.putExtra("type", "email")
            startActivity(intent)
            finish()
        }

    }
}