package com.android.sitbak.activities.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityWebviewBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import okhttp3.ResponseBody
import retrofit2.Call

class WebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityWebviewBinding
    private var titleText = ""
    private var url = ""
    private lateinit var viewModel: WebViewVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(WebViewVM::class.java)
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
                        ApiTags.GET_PRIVACY -> {
                            val data = it.data?.getJSONObject("data")
                            binding.webView.loadData(data?.getString("content")!!, "text/html", "UTF-8")
                        }
                        ApiTags.GET_TERMS_CONDITION -> {
                            val data = it.data?.getJSONObject("data")
                            binding.webView.loadData(data?.getString("content")!!, "text/html", "UTF-8")
                        }
                    }
                }
            }
        })
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        titleText = intent?.getStringExtra("title")!!
        url = intent?.getStringExtra("url")!!


        binding.topBar.tvTitle.text = titleText
        binding.topBar.ivBack.setOnClickListener { finish() }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.app_background))


        when (titleText) {
            mContext.resources.getString(R.string.terms_screen_title) -> {
                apiCall = retrofitClient.getWebPages("terms_of_service")
                viewModel.getTermsConditions(apiCall)
            }
            mContext.resources.getString(R.string.privacy_screen_title) -> {
                apiCall = retrofitClient.getWebPages("privacy_policy")
                viewModel.getPrivacy(apiCall)
            }
        }

    }
}