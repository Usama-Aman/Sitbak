package com.android.sitbak.auth.register

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.R
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.age_verification.AgeVerificationActivity
import com.android.sitbak.auth.current_location.CurrentLocationActivity
import com.android.sitbak.auth.login.LoginData
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivitySignUpWithEmailBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpWithEmailBinding
    private lateinit var viewModel: RegisterVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    var name = ""
    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpWithEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClientNoToken(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()
        onSetupViewGroup(binding.content)

        initVM()
        initObservers()
        initVar()
        listener()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RegisterVM::class.java)
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
                        ApiTags.REGISTER -> {
                            AppUtils.showToast(this, it.data?.message!!, true)

                            val data = it.data.data

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
                            SharedPreference.saveSimpleString(mContext, Constants.accessToken, it.data.access_token)
                            AppUtils.saveUserModel(mContext, it.data)

                            checkForUserData(data)
                        }
                    }
                }
            }
        })
    }

    private fun checkForUserData(data: LoginData) {
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
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 1000)
    }


    @SuppressLint("SetTextI18n")
    private fun initVar() {
        Boom(binding.btnCreateAccount)
        onSetupViewGroup(binding.content)
        binding.btnCreateAccount.isEnabled = false
        binding.topBarLayout.tvTitle.text = "Sign Up with email"

        binding.tvInvitationCode.text = if (AppController.inviteReferralCode == "null") "" else AppController.inviteReferralCode
    }

    private fun listener() {
        binding.ivPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle) }

        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                name = binding.etName.text.toString()
                validation()
            }

        })
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                email = binding.etEmail.text.toString()
                validation()
            }

        })
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                password = binding.etPassword.text.toString()
                validation()
            }

        })

        binding.btnCreateAccount.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.register(
                    name, email, password, password,
                    AppController.inviteReferralCode!!, AppController.deviceAppUID
                )
                viewModel.register(apiCall)
            }
        }
    }

    private fun validation() {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            binding.llCreateAccount.setBackgroundResource(R.drawable.bg_btn_main)
            binding.btnCreateAccount.setTextColor(
                ContextCompat.getColor(
                    this@RegisterActivity,
                    R.color.white
                )
            )
            binding.btnCreateAccount.isEnabled = true
        } else {
            binding.btnCreateAccount.isEnabled = false
            binding.llCreateAccount.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnCreateAccount.setTextColor(
                ContextCompat.getColor(
                    this@RegisterActivity,
                    R.color.green_100
                )
            )
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