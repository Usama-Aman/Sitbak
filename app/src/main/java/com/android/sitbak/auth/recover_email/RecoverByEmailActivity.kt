package com.android.sitbak.auth.recover_email

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.email_confirmation.RecoverByEmailConfirmationActivity
import com.android.sitbak.auth.recover_phone.RecoverByPhoneActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityRecoverByEmailBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.base.AppController
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class RecoverByEmailActivity : BaseActivity() {

    private lateinit var binding: ActivityRecoverByEmailBinding
    private var fromProfile = false

    private lateinit var viewModel: RecoverByEmailVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverByEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initVar()
        listener()

        boomViews()
    }

    private fun boomViews() {
        Boom(binding.btnSend)
        Boom(binding.btnRecoverByPhone)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RecoverByEmailVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
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
                        ApiTags.RECOVER_BY_EMAIL -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val data = it.data.getJSONObject("data")
                            AppController.resetOTP = data.getInt("otp")
                            val intent = Intent(mContext, RecoverByEmailConfirmationActivity::class.java)
                            intent.putExtra("email", binding.etEmail.text.toString())
                            intent.putExtra("type", "resetPasswordByEmail")
                            intent.putExtra("fromProfile", fromProfile)
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(intent)
                                finish()
                            }, 1000)

                        }
                    }
                }
            }
        })
    }


    private fun initVar() {
        binding.btnSend.isEnabled = false
        fromProfile = intent?.getBooleanExtra("fromProfile", false)!!
        binding.topBarLayout.tvTitle.text = resources.getString(R.string.recover_by_email_screen_title)
    }

    private fun listener() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                email = binding.etEmail.text.trim().toString()
                validation()
            }

        })

        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.senOTPForgotPassword("", binding.etEmail.text.toString(), "email")
                viewModel.recoverByEmail(apiCall)
            }
        }
        binding.btnRecoverByPhone.setOnClickListener {
            val intent = Intent(this@RecoverByEmailActivity, RecoverByPhoneActivity::class.java)
            intent.putExtra("fromProfile", fromProfile)
            startActivity(intent)
        }
    }


    private fun validation() {
        if (email.isNotEmpty()) {
            binding.llSend.setBackgroundResource(R.drawable.bg_btn_main)
            binding.btnSend.setTextColor(ContextCompat.getColor(this@RecoverByEmailActivity, R.color.white))
            binding.btnSend.isEnabled = true
        } else {
            binding.llSend.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnSend.setTextColor(ContextCompat.getColor(this@RecoverByEmailActivity, R.color.green_100))
            binding.btnSend.isEnabled = false
        }
    }

    private fun validate(): Boolean {
        if (!email.isValidEmail()) {
            AppUtils.showToast(this, "Email is invalid!", false)
            return false
        }
        return true
    }
}