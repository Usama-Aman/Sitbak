package com.android.sitbak.home.login_info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.recover_email.RecoverByEmailActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentInfoPasswordBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.AppUtils.hideShowPassword
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class InfoPasswordFragment : Fragment() {

    private lateinit var binding: FragmentInfoPasswordBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: LoginInfoVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInfoPasswordBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        initViews()
        initListeners()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LoginInfoVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(viewLifecycleOwner, {
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
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.UPDATE_PASSWORD -> {
                            binding.etCurrentPassword.setText("")
                            binding.etNewPassword.setText("")
                            binding.etRepeatPassword.setText("")
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }
                        ApiTags.UPDATE_EMAIL -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }
                    }
                }
            }
        })
    }

    private fun initViews() {
        Boom(binding.btnChangePassword)
        Boom(binding.llForgotPassword)

        binding.etCurrentPassword.addTextChangedListener(CustomTextWatcher())
        binding.etNewPassword.addTextChangedListener(CustomTextWatcher())
        binding.etRepeatPassword.addTextChangedListener(CustomTextWatcher())

        binding.switchButton.isChecked = SharedPreference.getBoolean(mContext, Constants.isFaceLoggedIn)
    }


    private fun initListeners() {
        binding.ivCurrentPasswordToggle.setOnClickListener { hideShowPassword(binding.etCurrentPassword, binding.ivCurrentPasswordToggle) }
        binding.ivNewPasswordToggle.setOnClickListener { hideShowPassword(binding.etNewPassword, binding.ivNewPasswordToggle) }
        binding.ivRepeatPasswordToggle.setOnClickListener { hideShowPassword(binding.etRepeatPassword, binding.ivRepeatPasswordToggle) }

        binding.llForgotPassword.setOnClickListener {
            val intent = Intent(mContext, RecoverByEmailActivity::class.java)
            intent.putExtra("fromProfile", true)
            startActivity(intent)
        }

        binding.btnChangePassword.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.updateUserPassword(
                    binding.etCurrentPassword.text.toString(),
                    binding.etNewPassword.text.toString(),
                    binding.etRepeatPassword.text.toString(),
                )
                viewModel.updateUserPassword(apiCall)
            }
        }

        binding.switchButton.setOnCheckedChangeListener { view, isChecked ->
            SharedPreference.saveBoolean(mContext, Constants.isFaceLoggedIn, isChecked)
        }
    }


    inner class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (binding.etCurrentPassword.text.toString().isNotEmpty() &&
                binding.etNewPassword.text.toString().isNotEmpty() &&
                binding.etRepeatPassword.text.toString().isNotEmpty()
            ) {
                binding.llChangePassword.setBackgroundResource(R.drawable.bg_btn_main)
                binding.btnChangePassword.setTextColor(ContextCompat.getColor(mContext, R.color.white))
                binding.btnChangePassword.isEnabled = true
            } else {
                binding.llChangePassword.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                binding.btnChangePassword.setTextColor(ContextCompat.getColor(mContext, R.color.green_100))
                binding.btnChangePassword.isEnabled = false
            }
        }

    }

    private fun validate(): Boolean {
        return when {
            binding.etNewPassword.text.toString().length < 8 -> {
                AppUtils.showToast(mContext as Activity, "Password must be at least 8 digits!", false)
                false
            }
            binding.etCurrentPassword.text == binding.etNewPassword.text -> {
                AppUtils.showToast(mContext as Activity, "New password cannot be same as current password!", false)
                false
            }
            binding.etRepeatPassword.text.toString() != binding.etNewPassword.text.toString() -> {
                AppUtils.showToast(mContext as Activity, "Password does not match!", false)
                false
            }
            else -> true
        }

    }

}