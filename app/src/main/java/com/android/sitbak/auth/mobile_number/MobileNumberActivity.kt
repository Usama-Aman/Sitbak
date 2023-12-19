package com.android.sitbak.auth.mobile_number

import android.annotation.SuppressLint
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
import com.android.sitbak.auth.register.EmailConfirmationActivity
import com.android.sitbak.auth.verification_code.VerificationCodeActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityMobileNumberBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class MobileNumberActivity : BaseActivity() {

    private lateinit var binding: ActivityMobileNumberBinding
    private lateinit var viewModel: MobileNumberVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var fromProfileScreen = false
    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileNumberBinding.inflate(layoutInflater)
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
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(MobileNumberVM::class.java)
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
                        ApiTags.ADD_UPDATE_PHONE -> {
                            AppUtils.showToast(this, it.data!!, true)

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(mContext, VerificationCodeActivity::class.java)
                                intent.putExtra("type", type)
                                intent.putExtra("fromProfile", fromProfileScreen)
                                intent.putExtra(
                                    "phoneOrEmail",
                                    binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString()
                                )
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
        Boom(binding.btnSendCode)
        fromProfileScreen = intent?.getBooleanExtra("fromProfile", false)!!
        type = intent?.getStringExtra("type")!!

        binding.countryCodePicker.registerCarrierNumberEditText(binding.etPhoneNumber)

        if (fromProfileScreen) {
            binding.tvDescription.viewGone()
            binding.authProgressBar.viewGone()
            binding.viewTopMain.viewGone()

            binding.btnSendCode.text = resources.getString(R.string.change_phone)

            val userData = AppUtils.getUserDetails(mContext).data
            binding.etPhoneNumber.setText(userData.phone_number.substring(userData.country_code.length))

            binding.countryCodePicker.setCountryForPhoneCode(userData.country_code.toInt())

        } else {
            binding.tvDescription.viewVisible()
            binding.authProgressBar.viewVisible()
            binding.viewTopMain.viewVisible()
            binding.btnSendCode.text = resources.getString(R.string.send_code)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                binding.authProgressBar.setProgress(SharedPreference.getInteger(mContext, Constants.authProgress, 0), true)
            else
                binding.authProgressBar.progress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun listener() {
        binding.topBarLayout.tvTitle.text = "Phone Number"
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isValidMobileNumber()) {
                    binding.llSend.setBackgroundResource(R.drawable.bg_btn_main)
                    binding.btnSendCode.setTextColor(
                        ContextCompat.getColor(
                            this@MobileNumberActivity,
                            R.color.white
                        )
                    )
                    binding.btnSendCode.isEnabled = true

                } else {
                    binding.btnSendCode.isEnabled = false
                    binding.llSend.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                    binding.btnSendCode.setTextColor(
                        ContextCompat.getColor(
                            this@MobileNumberActivity,
                            R.color.green_100
                        )
                    )
                }
            }

        })

        binding.btnSendCode.setOnClickListener {
            val phoneNumber = binding.countryCodePicker.selectedCountryCodeWithPlus +
                    binding.etPhoneNumber.text.toString().replace(" ", "").replace("-", "")
            apiCall = retrofitClient.addUpdatePhoneNumber(binding.countryCodePicker.selectedCountryCodeWithPlus, phoneNumber)
            viewModel.addUpdatePhoneNumber(apiCall)
        }
    }
}