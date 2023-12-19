package com.android.sitbak.home.shop_search_result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.auth.login.DefaultLocation
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityShopSearchResultBinding
import com.android.sitbak.home.shop_detail.ShopDetailsActivity
import com.android.sitbak.home.shops.StoresData
import com.android.sitbak.home.shops.StoresModel
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class ShopSearchResultActivity : BaseActivity(), ShopSearchAdapter.ShopSearchResult {

    private lateinit var binding: ActivityShopSearchResultBinding
    private lateinit var shopSearchAdapter: ShopSearchAdapter
    private val searchData: MutableList<StoresData> = ArrayList()
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var viewModel: ShopSearchResultVM
    private lateinit var mContext: Context

    private var isLoading = false
    private var skip = 0
    private var loadMore = false

    private var searchWord = ""
    private var categoryId = -1
    private lateinit var location: DefaultLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_search_result)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)


        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()
        onSetupViewGroup(binding.constraintShopSearchResult)

        initVM()
        initObservers()
        initViews()
        initListeners()
        initAdapter()

        getStores()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ShopSearchResultVM::class.java)
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
                        apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    when (it.tag) {
                        ApiTags.GET_STORES_SEARCH -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), StoresModel::class.java)
                            handleStoreResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun handleStoreResponse(data: List<StoresData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (!data.isNullOrEmpty()) {
            if (searchData.size > 0)
                searchData.removeAt(searchData.size - 1)

            if (data.size == AppController.pageCount) {
                loadMore = true
                skip += AppController.pageCount
            } else {
                loadMore = false
            }

            searchData.addAll(data)

            if (searchData.isEmpty()) {
                binding.searchResultRecyclerView.viewGone()
                binding.llNoData.viewVisible()
            } else {
                binding.llNoData.viewGone()
                binding.searchResultRecyclerView.viewVisible()
            }

            shopSearchAdapter.notifyDataSetChanged()
        } else {
            binding.searchResultRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        }

        isLoading = false
    }


    private fun initViews() {
        searchWord = intent?.getStringExtra("searchWord")!!
        categoryId = intent?.getIntExtra("categoryId", 0)!!
        location = intent?.getParcelableExtra("user_location")!!

        binding.topBar.tvSubTitle.text = searchWord

        binding.topBar.ivSearch.viewVisible()
        binding.topBar.tvTitle.viewVisible()
        binding.topBar.tvTitle.text = resources.getString(R.string.search_results_stores)
//        binding.topBar.tvSubTitle.text = resources.getString(R.string.search_result_subtitle, searchWord)
    }

    private fun getStores() {
        searchData.clear()
        val apiCall = if (AppController.isGuest)
            retrofitClient.getStoresGuest(categoryId, searchWord, location.latitude, location.longitude, "", skip)
        else
            retrofitClient.getStoresUser(categoryId, searchWord, location.latitude, location.longitude, "", skip)
        viewModel.getStores(apiCall, ApiTags.GET_STORES_SEARCH)
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.topBar.ivSearch.setOnClickListener {
            binding.topBar.tvTitle.viewGone()
            binding.topBar.tvSubTitle.viewGone()
            binding.topBar.ivSearch.viewGone()
            binding.topBar.etSearch.viewVisible()
            binding.topBar.ivCross.viewVisible()
            binding.topBar.etSearch.animate().translationX(0f)
            binding.topBar.etSearch.requestFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        binding.topBar.ivCross.setOnClickListener {
            searchWord = ""
            getStores()

            binding.topBar.tvTitle.viewVisible()
            binding.topBar.tvSubTitle.viewVisible()
            binding.topBar.ivSearch.viewVisible()
            binding.topBar.ivCross.viewGone()
            binding.topBar.etSearch.viewGone()
            binding.topBar.tvSubTitle.viewGone()
            AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)
        }

        binding.topBar.etSearch.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.action == KeyEvent.ACTION_DOWN
                || event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                searchWord = binding.topBar.etSearch.text.toString()

                if (searchWord.isNotBlank()) {
                    binding.topBar.tvSubTitle.viewVisible()
                    binding.topBar.tvSubTitle.text = resources.getString(R.string.search_result_subtitle, searchWord)
                } else
                    binding.topBar.tvSubTitle.viewGone()

                getStores()
                binding.topBar.tvTitle.viewVisible()
                binding.topBar.tvSubTitle.viewVisible()
                binding.topBar.ivSearch.viewVisible()
                binding.topBar.ivCross.viewGone()
                binding.topBar.etSearch.viewGone()
                AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)

            }
            false
        }

        binding.pullToRefresh.setOnRefreshListener {
            searchData.clear()
            skip = 0
            getStores()
        }

    }

    private fun initAdapter() {
        shopSearchAdapter = ShopSearchAdapter(searchData, this)
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultRecyclerView.adapter = shopSearchAdapter
    }

    override fun shopSearchItemClicked(position: Int) {
        val intent = Intent(this, ShopDetailsActivity::class.java)
        intent.putExtra("storeId", searchData[position].id)
        intent.putExtra("storeName", searchData[position].name)
        intent.putExtra("storeDeliveryTime", searchData[position].delivery_time)
        intent.putExtra("storeDistance", searchData[position].distance)
        intent.putExtra("storeDelivery", searchData[position].delivery)
        intent.putExtra("storeRating", searchData[position].rating)
        intent.putExtra("storeImage", searchData[position].photo_path)
        startActivity(intent)
        finish()
    }


}