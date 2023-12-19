package com.android.sitbak.auth.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.age_verification.AgeVerificationActivity
import com.android.sitbak.auth.current_location.CurrentLocationActivity
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.auth.recover_email.RecoverByEmailActivity
import com.android.sitbak.auth.register.EmailConfirmationActivity
import com.android.sitbak.auth.register.RegisterActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.biometric.BioMetric
import com.android.sitbak.databinding.ActivityLoginBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.white)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        initListeners()
        boomViews()
    }

    private fun boomViews() {
        Boom(binding.btnStart)
        Boom(binding.btnSignUp)
        Boom(binding.btnForgotPassword)
    }

    private fun initViews() {
        onSetupViewGroup(binding.content)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LoginVM::class.java)
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
                        ApiTags.LOGIN -> {
//                            AppUtils.showToast(this, it.data?.message!!, true)
                            handleLoginResponse(it.data!!)
                        }
                    }
                }
            }
        })
    }

    private fun handleLoginResponse(loginModel: LoginModel) {
        AppUtils.saveUserModel(mContext, loginModel)
        SharedPreference.saveSimpleString(mContext, Constants.accessToken, loginModel.access_token)

        binding.etEmail.setText("")
        binding.etPassword.setText("")
        binding.etEmail.requestFocus()

        val data = loginModel.data

        var authProgress = 20
        if (data.email_verified_at != null)
            authProgress += 20
        if (data.phone_verified_at != null)
            authProgress += 20
        if (data.photo_path != null)
            authProgress += 20
        if (data.is_age_verified != 0)
            authProgress += 20

        SharedPreference.saveInteger(mContext, Constants.authProgress, authProgress)

        val intent: Intent
        when {
            data.email_verified_at == null -> {
                intent = Intent(mContext, EmailConfirmationActivity::class.java)
                intent.putExtra("email", data.email)
            }
            data.phone_verified_at == null -> {
                intent = Intent(mContext, MobileNumberActivity::class.java)
                intent.putExtra("type", "phone")
            }
            data.photo_path == null -> {
                intent = Intent(mContext, AddYourPhotoActivity::class.java)
            }
            data.is_age_verified == 0 -> {
                intent = Intent(mContext, AgeVerificationActivity::class.java)
            }
            data.default_location == null -> {
                intent = Intent(mContext, CurrentLocationActivity::class.java)
            }
            else -> {
                intent = Intent(mContext, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
            }
        }
//        Handler(Looper.getMainLooper()).postDelayed({
        startActivity(intent)
//        }, 1000)
    }

    @SuppressLint("HardwareIds")
    private fun initListeners() {
        binding.ivPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle) }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RecoverByEmailActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

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

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                password = binding.etPassword.text.trim().toString()
                validation()
            }

        })

        binding.btnStart.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.login(email, password, AppController.deviceAppUID)
                viewModel.loginUser(apiCall)
            }
        }

        binding.ivFacelogin.setOnClickListener {

            val bioMetric = BioMetric(this, object : BioMetric.BioMetricInterface {
                override fun isDeviceCompatible(isDeviceCompatible: Boolean) {
                    if (!isDeviceCompatible)
                        AppUtils.showToast(this@LoginActivity, "Device incompatible", false)
                }

                override fun bioMetricSuccess(result: BiometricPrompt.AuthenticationResult) {
                    if (SharedPreference.getBoolean(mContext, Constants.isFaceLoggedIn)) {
                        if (!SharedPreference.getSimpleString(mContext, Constants.accessToken).isNullOrEmpty()) {
                            SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                            intent = Intent(mContext, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        } else
                            AppUtils.showToast(this@LoginActivity, "Please log in one to use biometric", false)
                    } else
                        AppUtils.showToast(this@LoginActivity, "Please enable biometric login in the app", false)
                }

                override fun bioMetricFailed(errorCode: Int, errorString: String) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            AppUtils.showToast(this@LoginActivity, "Please enable biometric from phone settings first", false)
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                        }
                        else -> {
                            AppUtils.showToast(this@LoginActivity, "$errorString $errorCode", false)
                        }
                    }
                }
            })
            bioMetric.onTouchIdClick()
        }
    }

    private fun validation() {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            binding.llStart.setBackgroundResource(R.drawable.bg_btn_main)
            binding.btnStart.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.white))
            binding.btnStart.isEnabled = true
        } else {
            binding.llStart.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnStart.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.green_100))
            binding.btnStart.isEnabled = false
        }
    }

    private fun validate(): Boolean {
        if (!email.isValidEmail()) {
            AppUtils.showToast(this, "Email is invalid!", false)
            return false
        } else if (password.length < 8) {
            AppUtils.showToast(this, "Password must be at least 8 digits!", false)
            return false
        }
        return true
    }
}