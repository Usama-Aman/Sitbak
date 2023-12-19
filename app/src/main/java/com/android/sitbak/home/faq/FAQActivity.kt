package com.android.sitbak.home.faq

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityFAQBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import okhttp3.ResponseBody
import retrofit2.Call

class FAQActivity : BaseActivity() {

    private lateinit var binding: ActivityFAQBinding
    private lateinit var faqAdapter: FAQAdapter
    private lateinit var viewModel: FAQVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var faqList: MutableList<FAQData> = ArrayList()

    companion object {
        var previousGroup = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFAQBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initVar()
        listener()
        initAdapter()

        getFAQ()
    }

    private fun getFAQ() {
        apiCall = retrofitClient.getFAQs()
        viewModel.getFAQs(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(FAQVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (!binding.pullToRefresh.isRefreshing)
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
                        ApiTags.GET_FAQ -> {
                            if (binding.pullToRefresh.isRefreshing) binding.pullToRefresh.isRefreshing = false
                            it.data?.data?.let { it1 -> faqList.addAll(it1) }
                            faqAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private fun initVar() {
        binding.topBar.tvTitle.text = resources.getString(R.string.faq_screen_title)
    }

    private fun listener() {
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.pullToRefresh.setOnRefreshListener {
            faqList.clear()
            getFAQ()
        }
    }

    private fun initAdapter() {
        faqAdapter = FAQAdapter(faqList, this)
        binding.expandableList.setAdapter(faqAdapter)
    }
}