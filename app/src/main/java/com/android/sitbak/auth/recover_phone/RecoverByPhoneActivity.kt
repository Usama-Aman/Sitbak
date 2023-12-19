package com.android.sitbak.auth.recover_phone

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
import com.android.sitbak.R
import com.android.sitbak.auth.verification_code.VerificationCodeActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityRecoverByPhoneBinding
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

class RecoverByPhoneActivity : BaseActivity() {

    private lateinit var binding: ActivityRecoverByPhoneBinding
    private var fromProfile = false

    var phoneNumber = ""

    private lateinit var viewModel: RecoverByPhoneVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverByPhoneBinding.inflate(layoutInflater)
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
        boomViews()
    }

    private fun boomViews() {
        Boom(binding.btnSend)
        Boom(binding.btnRecoverByEmail)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RecoverByPhoneVM::class.java)
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
                        ApiTags.RECOVER_BY_PHONE -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val data = it.data.getJSONObject("data")
                            AppController.resetOTP = data.getInt("otp")
                            val intent = Intent(mContext, VerificationCodeActivity::class.java)
                            intent.putExtra("type", "resetPasswordByPhone")
                            intent.putExtra(
                                "phoneOrEmail",
                                binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString()
                            )
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
        binding.topBarLayout.tvTitle.text = resources.getString(R.string.recover_by_phone_screen_title)

        binding.countryCodePicker.registerCarrierNumberEditText(binding.etPhoneNumber)
    }

    @SuppressLint("SetTextI18n")
    private fun listener() {
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                phoneNumber = binding.etPhoneNumber.text.trim().toString()
                validation()
            }

        })

        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.btnRecoverByEmail.setOnClickListener {
            finish()
        }
        binding.btnSend.setOnClickListener {

            if (validate()) {
                apiCall = retrofitClient.senOTPForgotPassword(
                    binding.countryCodePicker.selectedCountryCodeWithPlus +
                            binding.etPhoneNumber.text.toString().replace(" ", "").replace("-", ""),
                    "",
                    "phone_number"
                )
                viewModel.recoverByPhone(apiCall)
            }
        }
    }

    private fun validation() {
        if (phoneNumber.isNotEmpty()) {
            binding.llSend.setBackgroundResource(R.drawable.bg_btn_main)
            binding.btnSend.setTextColor(ContextCompat.getColor(this@RecoverByPhoneActivity, R.color.white))
            binding.btnSend.isEnabled = true
        } else {
            binding.llSend.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnSend.setTextColor(ContextCompat.getColor(this@RecoverByPhoneActivity, R.color.green_100))
            binding.btnSend.isEnabled = false
        }
    }

    private fun validate(): Boolean {
        if (!phoneNumber.isValidMobileNumber()) {
            AppUtils.showToast(this, "Phone number is invalid!", false)
            return false
        }
        return true
    }

}