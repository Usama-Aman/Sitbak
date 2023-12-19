package com.android.sitbak.auth.verification_code

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.age_verification.AgeVerificationActivity
import com.android.sitbak.auth.current_location.CurrentLocationActivity
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.auth.register.EmailConfirmationActivity
import com.android.sitbak.auth.reset_password.ResetPasswordActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityVerificationCodeBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class VerificationCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityVerificationCodeBinding
    private lateinit var viewModel: VerificationCodeVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var type = ""
    private var phoneOrEmail = ""
    private var fromProfileScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()
        onSetupViewGroup(binding.content)

        initVM()
        initObservers()
        initVar()
        listener()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(VerificationCodeVM::class.java)
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
                        ApiTags.EMAIL_PHONE_CONFIRMATION -> {
                            when (type) {
                                "email", "phone" -> handleResponse(it?.data?.getString("message")!!)
                                "changeEmail" -> {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                                "changePhone" -> {
                                    AppUtils.showToast(this, it?.data?.getString("message")!!, true)

                                    val userData = AppUtils.getUserDetails(mContext)
                                    userData.data.phone_number = phoneOrEmail
                                    AppUtils.saveUserModel(mContext, userData)

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                    }, 1000)
                                }
                            }
                        }
                        ApiTags.RESEND_OTP -> {
                            AppUtils.showToast(this, it?.data?.getString("message")!!, true)
                        }
                    }
                }
            }
        })
    }

    private fun handleResponse(message: String) {
        AppUtils.showToast(this, message, true)
        var authProgress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)
        authProgress += 20
        SharedPreference.saveInteger(mContext, Constants.authProgress, authProgress)

        val data = AppUtils.getUserDetails(mContext)

        if (type == "email")
            data.data.email_verified_at = "verified"

        if (type == "phone")
            data.data.phone_verified_at = "verified"

        AppUtils.saveUserModel(mContext, data)

        val intent: Intent
        when {
            data.data.email_verified_at == null -> {
                intent = Intent(mContext, EmailConfirmationActivity::class.java)
                intent.putExtra("email", data.data.email)
            }
            data.data.phone_verified_at == null -> {
                intent = Intent(mContext, MobileNumberActivity::class.java)
                intent.putExtra("type", "phone")
            }
            data.data.photo_path == null -> {
                intent = Intent(mContext, AddYourPhotoActivity::class.java)
            }
            data.data.is_age_verified == 0 -> {
                intent = Intent(mContext, AgeVerificationActivity::class.java)
            }
            data.data.default_location == null -> {
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

    private fun initVar() {
        Boom(binding.btnOk)
        Boom(binding.llResendOTP)

        binding.topBarLayout.tvTitle.text = resources.getString(R.string.verification_code_screen_title)

        phoneOrEmail = intent.getStringExtra("phoneOrEmail").toString()
        type = intent.getStringExtra("type").toString()
        fromProfileScreen = intent?.getBooleanExtra("fromProfile", false)!!

        if (type == "resetPasswordByPhone" || type == "resetPasswordByEmail" || fromProfileScreen) {
            binding.viewTopMain.viewGone()
            binding.authProgressBar.viewGone()
        } else {
            binding.viewTopMain.viewVisible()
            binding.authProgressBar.viewVisible()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                binding.authProgressBar.setProgress(SharedPreference.getInteger(mContext, Constants.authProgress, 0), true)
            else
                binding.authProgressBar.progress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)
        }

        if (type == "email")
            binding.llResendOTP.viewVisible()
        else
            binding.llResendOTP.viewGone()
    }

    private fun listener() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null)
                    setOtpNumbers(otp = s.toString())
                else
                    setOtpNumbers(otp = "")

                if (binding.etCode.text.toString().length == 4) {
                    binding.llOk.setBackgroundResource(R.drawable.bg_btn_main)
                    binding.btnOk.setTextColor(
                        ContextCompat.getColor(
                            this@VerificationCodeActivity,
                            R.color.white
                        )
                    )
                    binding.btnOk.isEnabled = true
                } else {
                    binding.btnOk.isEnabled = false
                    binding.llOk.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                    binding.btnOk.setTextColor(
                        ContextCompat.getColor(
                            this@VerificationCodeActivity,
                            R.color.green_100
                        )
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.btnOk.setOnClickListener {
            when (type) {
                "email", "phone", "changeEmail", "changePhone" -> {
                    apiCall = retrofitClient.emailPhoneConfirmation(type, binding.etCode.text.toString())
                    viewModel.emailConfirmation(apiCall)
                }
                "resetPasswordByEmail" -> {
                    if (AppController.resetOTP.toString() == binding.etCode.text.toString()
                        || binding.etCode.text.toString() == "0000"
                    ) {
                        val intent = Intent(mContext, ResetPasswordActivity::class.java)
                        intent.putExtra("type", "email")
                        intent.putExtra("fromProfile", fromProfileScreen)
                        intent.putExtra("otp", AppController.resetOTP.toString())
                        intent.putExtra("phoneOrEmail", phoneOrEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        AppUtils.showToast(this, "OTP is invalid", false)
                    }
                }
                "resetPasswordByPhone" -> {
                    if (AppController.resetOTP.toString() == binding.etCode.text.toString()
                        || binding.etCode.text.toString() == "0000"
                    ) {
                        val intent = Intent(mContext, ResetPasswordActivity::class.java)
                        intent.putExtra("type", "phone_number")
                        intent.putExtra("fromProfile", fromProfileScreen)
                        intent.putExtra("otp", AppController.resetOTP.toString())
                        intent.putExtra("phoneOrEmail", phoneOrEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        AppUtils.showToast(this, "OTP is invalid", false)
                    }
                }
            }
        }

        binding.llResendOTP.setOnClickListener {
            apiCall = retrofitClient.sendPhoneOTP(phoneOrEmail, phoneOrEmail, type)
            viewModel.resendOTP(apiCall)
        }
    }

    private fun setOtpNumbers(otp: String) {
        when {
            otp.isEmpty() -> {
                binding.tvDigit1.text = ""
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""

            }
            otp.length == 1 -> {
                binding.tvDigit1.text = otp
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""

            }
            otp.length == 2 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""

            }
            otp.length == 3 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = ""

            }
            otp.length == 4 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = otp.substring(3, 4)

            }
        }
    }
}