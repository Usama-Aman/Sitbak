package com.android.sitbak.home.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.activities.webview.WebViewActivity
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentProfileBinding
import com.android.sitbak.home.chat.ChatActivity
import com.android.sitbak.home.faq.FAQActivity
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.location_list.LocationListActivity
import com.android.sitbak.home.login_info.LoginInfoActivity
import com.android.sitbak.home.payment_method.PaymentMethodActivity
import com.android.sitbak.home.popups.DeleteLogoutAccountPopup
import com.android.sitbak.home.referral_code.ReferralCodeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags.DELETE_ACCOUNT
import com.android.sitbak.network.ApiTags.GET_PROFILE
import com.android.sitbak.network.ApiTags.LOGOUT
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.onesignal.OneSignalNotificationManager
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.onesignal.OneSignal
import okhttp3.ResponseBody
import retrofit2.Call

class ProfileFragment : Fragment(), View.OnClickListener, LogoutDeleteInterface {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
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
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ProfileVM::class.java)
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
                        LOGOUT -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)

                        }
                        DELETE_ACCOUNT -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                (activity as HomeActivity).gotoBoarding()
                            }, 1000)

                            SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, false)
                            SharedPreference.saveBoolean(mContext, Constants.isFaceLoggedIn, false)
                            SharedPreference.saveSimpleString(mContext, Constants.userModel, "")
                            SharedPreference.saveSimpleString(mContext, Constants.accessToken, "")
                        }
                        GET_PROFILE -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.saveUserModel(mContext, model)
                            updateProfileView()
                        }
                    }
                }
            }
        })
    }

    private fun initViews() {
        boomViews()
        binding.topBarLayout.ivBack.viewGone()
        binding.topBarLayout.tvTitle.text = mContext.resources.getString(R.string.profile)
    }

    private fun boomViews() {
        Boom(binding.llReferral)
        Boom(binding.llLocation)
        Boom(binding.llPhone)
        Boom(binding.llPaymentMethods)
        Boom(binding.llLoginInfo)
        Boom(binding.llNotifications)
        Boom(binding.llVip)
        Boom(binding.llFaq)
        Boom(binding.llChatWithSupport)
        Boom(binding.llCallToSupport)
        Boom(binding.llTerms)
        Boom(binding.llPrivacy)
        Boom(binding.llLogOut)
        Boom(binding.llDeleteAccount)
    }

    private fun updateProfileView() {
        val data = AppUtils.getUserDetails(mContext).data

        if (data.name.isNotBlank())
            binding.tvProfileName.text = data.name
        else
            binding.tvProfileName.text = ""

        if (data.photo_path != null) {
            Glide.with(mContext).load(data.photo_path).placeholder(R.drawable.ic_person).into(binding.ivProfileImage)
            binding.ivPlaceHolder.viewGone()
        } else
            binding.ivPlaceHolder.viewVisible()

        if (data.default_payment_method != null) {
            if (data.default_payment_method.last4.isNotBlank())
                binding.tvPaymentMethod.text = mContext.resources.getString(
                    R.string.payment_card_number,
                    data.default_payment_method.last4
                )
        } else
            binding.tvPaymentMethod.text = "Add Payment Method"

        if (data.referral_code.isNotBlank())
            binding.tvBonus.text = mContext.resources.getString(R.string.your_bonus_profile, data.total_bonus)
        else
            binding.tvBonus.text = ""

        if (data.default_location != null)
            if (data.default_location?.address?.isNotBlank() == true)
                binding.tvLocation.text = data.default_location?.address
            else
                binding.tvLocation.text = ""
        else
            binding.tvLocation.text = ""

        if (data.phone_number.isNotBlank())
            binding.tvPhone.text = data.phone_number
        else
            binding.tvPhone.text = ""
        if (data.email.isNotBlank())
            binding.tvLoginInfo.text = data.email
        else
            binding.tvLoginInfo.text = data.email

        binding.tvNotification.text = if (data.allow_notifications == 0) "Off" else "On"

    }

    private fun initListeners() {
        binding.llFaq.setOnClickListener(this)
        binding.llLogOut.setOnClickListener(this)
        binding.llDeleteAccount.setOnClickListener(this)
        binding.llReferral.setOnClickListener(this)
        binding.llLocation.setOnClickListener(this)
        binding.llTerms.setOnClickListener(this)
        binding.llPrivacy.setOnClickListener(this)
        binding.llPhone.setOnClickListener(this)
        binding.llLoginInfo.setOnClickListener(this)
        binding.llPaymentMethods.setOnClickListener(this)
        binding.llChatWithSupport.setOnClickListener(this)
        binding.llCallToSupport.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        AppUtils.preventDoubleClick(v!!)
        when (v.id) {
            R.id.llFaq -> {
                mContext.startActivity(Intent(mContext, FAQActivity::class.java))
            }
            R.id.llLogOut -> {
                /* Check for dialog to reuse class view
                * 0 - Delete account
                * 1 - Logout account
                * 2 - Delete/logout account
                * 3 - Delete Payment Card
                * */
                val dialog = DeleteLogoutAccountPopup(this, 1)
                dialog.show(activity?.supportFragmentManager!!, "DeleteLogoutAccountPopup")
            }
            R.id.llDeleteAccount -> {
                /* Check for dialog to reuse class view
                * 0 - Delete account
                * 1 - Logout account
                * 2 - Delete/logout account
                * 3 - Delete Payment Card
                * */
                val dialog = DeleteLogoutAccountPopup(this, 0)
                dialog.show(activity?.supportFragmentManager!!, "DeleteLogoutAccountPopup")
            }
            R.id.llReferral -> {
                mContext.startActivity(Intent(mContext, ReferralCodeActivity::class.java))
            }
            R.id.llLocation -> {
                mContext.startActivity(Intent(activity, LocationListActivity::class.java))
            }
            R.id.llTerms -> {
                startWebView(mContext.resources.getString(R.string.terms_screen_title), "https://www.google.com")
            }
            R.id.llPrivacy -> {
                startWebView(mContext.resources.getString(R.string.privacy_screen_title), "https://www.google1.com")
            }
            R.id.llPhone -> {
                val intent = Intent(activity, MobileNumberActivity::class.java)
                intent.putExtra("fromProfile", true)
                intent.putExtra("type", "changePhone")
                mContext.startActivity(Intent(intent))
            }
            R.id.llLoginInfo -> {
                mContext.startActivity(Intent(activity, LoginInfoActivity::class.java))
            }
            R.id.llPaymentMethods -> {
                mContext.startActivity(Intent(activity, PaymentMethodActivity::class.java))
            }
            R.id.llChatWithSupport -> {
//                mContext.startActivity(Intent(activity, ChatActivity::class.java))
            }
            R.id.llCallToSupport -> {
                val phone = "+920000000000"
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                startActivity(intent)
            }
        }
    }

    private fun startWebView(title: String, url: String) {
        val intent = Intent(activity, WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("url", url)
        mContext.startActivity(Intent(intent))
    }

    override fun deleteAccount() {
        apiCall = retrofitClient.deleteAccount()
        viewModel.deleteAccount(apiCall)
    }

    override fun logoutAccount() {
        OneSignalNotificationManager.removeUserTags()
        OneSignal.clearOneSignalNotifications()
        SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, false)
        (activity as HomeActivity).gotoBoarding()
    }

    override fun deleteCard() {}

    override fun onResume() {
        super.onResume()
//        updateProfileView()

        getProfileData()
    }

    private fun getProfileData() {
        apiCall = retrofitClient.getUserProfile()
        viewModel.getProfile(apiCall)
    }
}