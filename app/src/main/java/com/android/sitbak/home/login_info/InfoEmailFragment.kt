package com.android.sitbak.home.login_info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.verification_code.VerificationCodeActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentInfoEmailBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class InfoEmailFragment : Fragment() {

    private lateinit var binding: FragmentInfoEmailBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: LoginInfoVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInfoEmailBinding.inflate(inflater, container, false)
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
                    binding.changeEmailConstraint.viewGone()
                    binding.emailConfirmationConstraint.viewVisible()
                    binding.tvEmail.text = binding.etEmail.text.toString()
                }
            }
        })
    }

    private fun initViews() {
        Boom(binding.btnChangeEmail)
        binding.etEmail.addTextChangedListener(CustomTextWatcher())
    }


    private fun initListeners() {
        binding.btnChangeEmail.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.updateUserEmail(binding.etEmail.text.toString())
                viewModel.updateUserEmail(apiCall)
            }
        }

        binding.llContinue.setOnClickListener {
            val intent = Intent(mContext, VerificationCodeActivity::class.java)
            intent.putExtra("phoneOrEmail", binding.etEmail.text.toString())
            intent.putExtra("type", "changeEmail")
            intent.putExtra("fromProfile", true)
            startActivityForResult(intent, 2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000)
            if (resultCode == Activity.RESULT_OK) {
                AppUtils.showToast(requireActivity(), "Your email is updated", true)
                binding.changeEmailConstraint.viewVisible()
                binding.emailConfirmationConstraint.viewGone()

                val userData = AppUtils.getUserDetails(mContext)
                userData.data.email = binding.etEmail.text.toString()
                AppUtils.saveUserModel(mContext, userData)

                binding.etEmail.setText("")
                (activity as LoginInfoActivity).onBackPressed()
            }
    }


    inner class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (binding.etEmail.text.toString().isNotEmpty()) {
                binding.llChangeEmail.setBackgroundResource(R.drawable.bg_btn_main)
                binding.btnChangeEmail.setTextColor(ContextCompat.getColor(mContext, R.color.white))
                binding.btnChangeEmail.isEnabled = true
            } else {
                binding.llChangeEmail.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                binding.btnChangeEmail.setTextColor(ContextCompat.getColor(mContext, R.color.green_100))
                binding.btnChangeEmail.isEnabled = false
            }
        }

    }

    private fun validate(): Boolean {
        if (!binding.etEmail.text.toString().isValidEmail()) {
            AppUtils.showToast(mContext as Activity, "Email is invalid!", false)
            return false
        }
        return true
    }


}