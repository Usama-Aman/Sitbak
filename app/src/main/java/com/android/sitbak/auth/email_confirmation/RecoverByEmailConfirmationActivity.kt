package com.android.sitbak.auth.email_confirmation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.android.sitbak.R
import com.android.sitbak.auth.verification_code.VerificationCodeActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityRecoverByEmailSuccessBinding
import com.astritveliu.boom.Boom

class RecoverByEmailConfirmationActivity : BaseActivity() {

    private lateinit var binding: ActivityRecoverByEmailSuccessBinding
    private lateinit var mContext: Context
    private var fromProfile = false
    private var type = ""
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverByEmailSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()
        listener()
        initVar()
    }

    private fun initVar() {
        Boom(binding.btnContinue)
        fromProfile = intent?.getBooleanExtra("fromProfile", false)!!
        userEmail = intent?.getStringExtra("email").toString()
        type = intent?.getStringExtra("type").toString()

        binding.topBarLayout.tvTitle.text = resources.getString(R.string.recover_by_email_screen_title)

        if (userEmail.isNotBlank()) {
            binding.tvEmail.text = userEmail
        }
    }

    private fun listener() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            val intent = Intent(mContext, VerificationCodeActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("fromProfile", fromProfile)
            intent.putExtra("phoneOrEmail", userEmail)
            startActivity(intent)
            finish()

        }
    }
}