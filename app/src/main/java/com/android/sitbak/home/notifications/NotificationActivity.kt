package com.android.sitbak.home.notifications

import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityNotificationsBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class NotificationActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: NotificationsVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var notificationsAdapter: NotificationsAdapter

    private var isLoading = false
    private var skip = 0
    private var loadMore = false

    private val notificationsData: MutableList<NotificationsData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        initAdapter()


        getUserNotifications()
    }

    private fun getUserNotifications() {
        apiCall = retrofitClient.getNotifications()
        viewModel.getNotifications(apiCall)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(NotificationsVM::class.java)
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
                        ApiTags.GET_NOTIFICATIONS -> {
                            val model = Gson().fromJson(it.data.toString(), NotificationsModel::class.java)
                            handleNotificationsResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun handleNotificationsResponse(data: List<NotificationsData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (!data.isNullOrEmpty()) {
            if (notificationsData.size > 0)
                notificationsData.removeAt(notificationsData.size - 1)

            if (data.size >= AppController.pageCount) {
                loadMore = true
                skip += AppController.pageCount
            } else {
                loadMore = false
            }

            notificationsData.addAll(data)

            if (notificationsData.isEmpty()) {
                binding.notificationRecycler.viewGone()
                binding.llNoData.viewVisible()
            } else {
                binding.llNoData.viewGone()
                binding.notificationRecycler.viewVisible()
            }

            notificationsAdapter.notifyDataSetChanged()
        } else {
            binding.notificationRecycler.viewGone()
            binding.llNoData.viewVisible()
        }

        isLoading = false

    }


    private fun initViews() {
        binding.topBar.tvTitle.text = "Notifications"


        binding.pullToRefresh.setOnRefreshListener {
            notificationsData.clear()
            skip = 0
            getUserNotifications()
        }

        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {
        notificationsAdapter = NotificationsAdapter(notificationsData)
        binding.notificationRecycler.adapter = notificationsAdapter
    }


}