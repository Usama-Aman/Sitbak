package com.android.sitbak.home.shop_detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.R
import com.android.sitbak.activities.order_webview.OrderWebViewActivity
import com.android.sitbak.auth.BoardingActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityShopDetailsBinding
import com.android.sitbak.home.bottom_sheets.StoreCategoriesBottomSheetFragment
import com.android.sitbak.home.bottom_sheets.StraingTypeBottomSheetFragment
import com.android.sitbak.home.popups.GuestAccessPopUp
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call


@Suppress("DEPRECATION")
class ShopDetailsActivity : BaseActivity(), ShopDetailAdapter.ShopDetailInterface, GuestAccessPopUp.GuestAccessInterface {

    private lateinit var binding: ActivityShopDetailsBinding
    private lateinit var shopDetailAdapter: ShopDetailAdapter
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private lateinit var mContext: Context
    private lateinit var viewModel: ShopDetailVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var shopDetailProductsList: MutableList<ShopDetailData?> = ArrayList()
    private var shopDetailCategoriesList: MutableList<ShopDetailCategoryData> = ArrayList()
    private var shopDetailStraingTypes: MutableList<StraingTypeData> = ArrayList()

    private var storeId = 0
    private var storeName = "Stores"
    private var storeDeliveryTime = 0
    private var storeDistance = ""
    private var storeDelivery = ""
    private var storeRating = ""
    private var storeImage = ""

    private var selectedCategoryPosition = 0
    private var selectedStraingTypePosition = -1
    private var selectedProductType = ""
    private var searchWord = ""

    private var skip = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT

        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_details)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)


        initVM()
        initObservers()
        initViews()
        initAdapter()
        initListeners()

        getProductCategories()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ShopDetailVM::class.java)
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
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_PRODUCT_CATEGORIES -> {
                            val model = Gson().fromJson(it.data.toString(), ShopDetailCategoryModel::class.java)
                            handleStoreCategoryResponse(model)
                        }
                        ApiTags.GET_STORE_PRODUCTS -> {
                            try {
                                val model = Gson().fromJson(it.data.toString(), ShopDetailModel::class.java)
                                handleStoreResponse(model)
                            } catch (e: Exception) {
                                binding.llNoData.viewVisible()
                                binding.shopDetailRecyclerView.viewGone()
                                e.printStackTrace()
                            }
                        }
                        ApiTags.GET_STRAING_TYPES -> {
                            val model = Gson().fromJson(it.data.toString(), StraingTypeModel::class.java)
                            if (!model.data.isNullOrEmpty()) {
                                shopDetailStraingTypes.add(
                                    StraingTypeData(-1, "", "", "", "All")
                                )
                                shopDetailStraingTypes.addAll(model.data)
                                showStraingTypes()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun handleStoreCategoryResponse(model: ShopDetailCategoryModel?) {
        shopDetailCategoriesList.clear()
        shopDetailCategoriesList.add(ShopDetailCategoryData("", "All", 0, "", ""))
        shopDetailCategoriesList.addAll(model?.data!!)

        if (shopDetailCategoriesList.isNotEmpty()) {
            binding.ivCategories.isClickable = true
            initTabs()
            binding.tabConstraint.viewVisible()
            binding.shopDetailRecyclerView.viewVisible()
        } else {
            binding.tabConstraint.viewGone()
            binding.shopDetailRecyclerView.viewGone()
            binding.ivCategories.isClickable = false

        }
    }

    private fun handleStoreResponse(model: ShopDetailModel) {

        if (model.data.isNullOrEmpty()) {
            binding.shopDetailRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.shopDetailRecyclerView.viewVisible()

            if (shopDetailProductsList.isEmpty()) {
                shopDetailProductsList.add(
                    ShopDetailData(
                        "0".toBigInteger(), shopDetailCategoriesList[selectedCategoryPosition].category_title, "", "",
                        "", "", "", "", "", "", "", null, null, true
                    )
                )
            }
//            else {
//                shopDetailProductsList.removeAt(shopDetailProductsList.size - 1)
//            }

            shopDetailProductsList.addAll(model.data)

//            if (model.data.size == AppController.pageCount) {
//                shopDetailProductsList.add(null)
//                skip += AppController.pageCount
//            }

            shopDetailAdapter.notifyDataSetChanged()
            binding.llNoData.viewGone()
        }

    }


    @SuppressLint("SetTextI18n")
    private fun initViews() {
        Boom(binding.btnStraingType)

        storeId = intent?.getIntExtra("storeId", 0)!!
        storeName = intent?.getStringExtra("storeName")!!
        storeDeliveryTime = intent?.getIntExtra("storeDeliveryTime", 0)!!
        storeDistance = intent?.getStringExtra("storeDistance")!!
        storeDelivery = intent?.getStringExtra("storeDelivery")!!
        storeRating = intent?.getStringExtra("storeRating")!!
        storeImage = intent?.getStringExtra("storeImage")!!


        binding.topBar.tvTitle.text = storeName
        binding.topBar.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.topBar.etSearch.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.topBar.ivSearch.setImageResource(R.drawable.ic_search_shop_white)
        binding.topBar.ivBack.setImageResource(R.drawable.ic_back_white)
        binding.topBar.ivCross.setImageResource(R.drawable.ic_cross_white)

        binding.topBar.ivSearch.viewVisible()


        Glide.with(mContext)
            .load(storeImage)
            .placeholder(R.drawable.shop_image)
            .into(binding.ivStoreImage)

        binding.tvTime.text = "$storeDeliveryTime min"
        binding.tvDistance.text = "$storeDistance km"
        binding.tvDeliveryAmount.text = storeDelivery
        binding.tvRating.text = storeRating
        binding.tvRatingTitle.text = mContext.resources.getString(R.string.store_rating_by_users, storeRating)


    }

    private fun initTabs() {
        for (i in shopDetailCategoriesList.indices) {
            if (i == 0) {
                val tabTextView = LayoutInflater.from(this).inflate(R.layout.item_custom_tab_layout, null) as TextView
                tabTextView.text = shopDetailCategoriesList[i].category_title
                tabTextView.setTextColor(ContextCompat.getColor(this, R.color.app_color))
                val tab = binding.shopCategoriesTab.newTab()
                tab.customView = tabTextView
                binding.shopCategoriesTab.addTab(tab)

                selectedCategoryPosition = i
                shopDetailProductsList.clear()
                skip = 0
                getProducts()


            } else {
                val tabTextView = LayoutInflater.from(this).inflate(R.layout.item_custom_tab_layout, null) as TextView
                tabTextView.text = shopDetailCategoriesList[i].category_title
                val tab = binding.shopCategoriesTab.newTab()
                tab.customView = tabTextView
                binding.shopCategoriesTab.addTab(tab)
            }
        }


        binding.shopCategoriesTab.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tabTextView = tab?.customView
                    tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                        ?.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))

                    selectedCategoryPosition = tab?.position!!
                    shopDetailProductsList.clear()
                    skip = 0
                    getProducts()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val tabTextView = tab?.customView
                    tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                        ?.setTextColor(ContextCompat.getColor(mContext, R.color.text_light_black))
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })

    }

    private fun initAdapter() {
        shopDetailAdapter = ShopDetailAdapter(this, shopDetailProductsList)

        binding.shopDetailRecyclerView.adapter = shopDetailAdapter
        binding.shopDetailRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.shopDetailRecyclerView.setHasFixedSize(false)
        binding.shopDetailRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.btnStraingType.visibility == View.VISIBLE) {
                    binding.btnStraingType.hide()
                } else if (dy < 0 && binding.btnStraingType.visibility != View.VISIBLE) {
                    binding.btnStraingType.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.btnStraingType.show()
                }
            }
        })
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)
            finish()
        }

        binding.topBar.ivSearch.setOnClickListener {
            binding.topBar.tvTitle.viewGone()
            binding.topBar.ivSearch.viewGone()
            binding.topBar.etSearch.viewVisible()
            binding.topBar.ivCross.viewVisible()
            binding.topBar.etSearch.animate().translationX(0f)
            binding.topBar.etSearch.requestFocus()
            binding.topBar.etSearch.setHintTextColor(ContextCompat.getColor(this, R.color.white))

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        binding.topBar.ivCross.setOnClickListener {
            binding.topBar.tvTitle.viewVisible()
            binding.topBar.ivSearch.viewVisible()
            binding.topBar.ivCross.viewGone()
            binding.topBar.etSearch.viewGone()
            searchWord = ""
            binding.topBar.etSearch.setText("")
            AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)

            shopDetailProductsList.clear()
            skip = 0
            getProducts()
        }

        binding.topBar.etSearch.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.action == KeyEvent.ACTION_DOWN
                || event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                searchWord = binding.topBar.etSearch.text.toString()
                shopDetailProductsList.clear()
                skip = 0
                getProducts()
                AppUtils.hideKeyBoardFromEdittext(binding.topBar.etSearch, this)

                true
            }
            false
        }

        binding.btnStraingType.setOnClickListener {
            if (shopDetailStraingTypes.isNullOrEmpty()) {
                apiCall = if (AppController.isGuest) retrofitClient.getStraingTypesGuest()
                else retrofitClient.getStraingTypesUser()
                viewModel.getStraingTypes(apiCall)
            } else
                showStraingTypes()
        }

        binding.ivCategories.setOnClickListener {
            val dialog = StoreCategoriesBottomSheetFragment(this, shopDetailCategoriesList, selectedCategoryPosition)
            dialog.show(supportFragmentManager, "StoreCategoriesFragment")
        }

        binding.llRating.setOnClickListener {
            binding.llShopStats.viewGone()
            binding.llRating.viewGone()
            binding.llRatingDescription.viewVisible()
        }

        binding.ivRatingCross.setOnClickListener {
            binding.llShopStats.viewVisible()
            binding.llRating.viewVisible()
            binding.llRatingDescription.viewGone()
        }

    }

    override fun onItemClick(position: Int) {
        if (AppController.isGuest) {
            showGuestAccess()
        } else {
            val intent = Intent(mContext, OrderWebViewActivity::class.java)
            val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
            val url = "http://178.128.29.7/sitbak/web_views_product/$storeId/${shopDetailProductsList[position]?.id.toString()}/$accessToken"
            intent.putExtra("url", url)
            mContext.startActivity(intent)
        }
    }

    override fun onLoadMoreClikecd() {
        getProducts()
    }

    private fun getProductCategories() {
        apiCall = if (AppController.isGuest) retrofitClient.getProductCategoriesGuest(storeId)
        else retrofitClient.getProductCategoriesUser(storeId)
        viewModel.getProductCategories(apiCall)
    }

    private fun getProducts() {
        apiCall = if (AppController.isGuest)
            retrofitClient.getStoreProductsGuest(
                storeId,
                if (shopDetailCategoriesList[selectedCategoryPosition].category_title == "All") "" else shopDetailCategoriesList[selectedCategoryPosition].category_title,
                selectedProductType, searchWord, skip
            )
        else
            retrofitClient.getStoreProductsUser(
                storeId,
                if (shopDetailCategoriesList[selectedCategoryPosition].category_title == "All") "" else shopDetailCategoriesList[selectedCategoryPosition].category_title,
                selectedProductType, searchWord, skip
            )
        viewModel.getStoreProducts(apiCall)
    }

    private fun showGuestAccess() {
        val dialog = GuestAccessPopUp(this)
        dialog.show(supportFragmentManager, "GuestAccessPopUp")
    }

    private fun showStraingTypes() {
        val dialog = StraingTypeBottomSheetFragment(this, shopDetailStraingTypes, selectedStraingTypePosition)
        dialog.show(supportFragmentManager, "StraingTypeFragment")
    }

    override fun guestClickedLogin() {
        val intent = Intent(mContext, BoardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    fun onCategorySelected(position: Int) {
        selectedCategoryPosition = position
        binding.shopCategoriesTab.selectTab(binding.shopCategoriesTab.getTabAt(position))
    }

    fun onStraingTypeSelected(position: Int) {
        when {
            selectedStraingTypePosition == position -> {
                selectedStraingTypePosition = -1
                selectedProductType = ""
            }
            position == 0 -> {
                selectedStraingTypePosition = 0
                selectedProductType = ""
            }
            else -> {
                selectedStraingTypePosition = position
                selectedProductType = shopDetailStraingTypes[position].type_title
            }
        }
        shopDetailProductsList.clear()
        skip = 0
        getProducts()
    }

}
