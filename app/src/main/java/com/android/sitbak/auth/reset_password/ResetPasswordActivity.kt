package com.android.sitbak.auth.reset_password


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.BoardingActivity
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityResetPasswordBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.login_info.LoginInfoActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class ResetPasswordActivity : com.android.sitbak.base.BaseActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

    private var fromProfile = false
    private var type = ""
    private var phoneOrEmail = ""
    private var otp = ""


    private lateinit var viewModel: ResetPasswordVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()
        onSetupViewGroup(binding.content)

        initVM()
        initObservers()
        initVar()
        listener()
    }

    private fun listener() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.ivNewPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etNewPassword, binding.ivNewPasswordToggle) }
        binding.ivRepeatPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etRepeatPassword, binding.ivRepeatPasswordToggle) }

        binding.btnResetPassword.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.resetPassword(
                    binding.etNewPassword.text.toString(), binding.etRepeatPassword.text.toString(),
                    phoneOrEmail, phoneOrEmail, otp, type
                )
                viewModel.resetPassword(apiCall)
            }
        }
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ResetPasswordVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this)
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
                        ApiTags.RESET_PASSWORD -> {
                            when (type) {
                                "email" -> {
                                    AppUtils.showToast(this, it.data?.getString("message")!!, true)
                                    if (fromProfile) {

//                                        SharedPreference.getBoolean(this, Constants.isUserLoggedIn)
//                                        val intent = Intent(this, LoginInfoActivity::class.java)
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        Handler(Looper.getMainLooper()).postDelayed({
//                                            startActivity(intent)
                                            finish()
                                        }, 1000)
                                    } else {
                                        SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, false)
                                        val intent = Intent(this, BoardingActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            startActivity(intent)
                                            finish()
                                        }, 1000)
                                    }
                                }
                                "phone_number" -> {
                                    AppUtils.showToast(this, it.data?.getString("message")!!, true)
                                    if (fromProfile) {
//                                        SharedPreference.getBoolean(this, Constants.isUserLoggedIn)
//                                        val intent = Intent(this, LoginInfoActivity::class.java)
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        Handler(Looper.getMainLooper()).postDelayed({
//                                            startActivity(intent)
                                            finish()
                                        }, 1000)
                                    } else {
                                        SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, false)
                                        val intent = Intent(this, BoardingActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            startActivity(intent)
                                            finish()
                                        }, 1000)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun initVar() {
        Boom(binding.btnResetPassword)
        fromProfile = intent.getBooleanExtra("fromProfile", false)
        type = intent.getStringExtra("type").toString()
        phoneOrEmail = intent.getStringExtra("phoneOrEmail").toString()
        otp = intent.getStringExtra("otp").toString()

        binding.topBarLayout.tvTitle.text = resources.getString(R.string.btn_reset_password)
        binding.etNewPassword.addTextChangedListener(CustomTextWatcher())
        binding.etRepeatPassword.addTextChangedListener(CustomTextWatcher())
    }

    inner class CustomTextWatcher() : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {

            if (binding.etNewPassword.text.toString().isNotEmpty() &&
                binding.etRepeatPassword.text.toString().isNotEmpty() &&
                binding.etNewPassword.text.toString() == binding.etRepeatPassword.text.toString()
            ) {
                binding.llResetPassword.setBackgroundResource(R.drawable.bg_btn_main)
                binding.btnResetPassword.setTextColor(ContextCompat.getColor(this@ResetPasswordActivity, R.color.white))
                binding.btnResetPassword.isEnabled = true
            } else {
                binding.llResetPassword.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                binding.btnResetPassword.setTextColor(ContextCompat.getColor(this@ResetPasswordActivity, R.color.green_100))
                binding.btnResetPassword.isEnabled = false
            }
        }

    }

    private fun validate(): Boolean {
        if (binding.etNewPassword.text.toString().length < 8) {
            AppUtils.showToast(this, "Password must be at least 8 digits!", false)
            return false
        } else if (binding.etRepeatPassword.text != binding.etNewPassword.text) {
            AppUtils.showToast(this, "Password doesn't match!", false)
            return false
        }
        return true
    }
}