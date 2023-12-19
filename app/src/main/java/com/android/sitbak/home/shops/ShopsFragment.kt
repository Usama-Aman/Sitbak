package com.android.sitbak.home.shops

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.activities.MapsActivity
import com.android.sitbak.activities.order_webview.OrderWebViewActivity
import com.android.sitbak.auth.login.DefaultLocation
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentShopsBinding
import com.android.sitbak.home.bottom_sheets.SortByBottomSheetFragment
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.location_bottom_sheet.LocationBottomSheet
import com.android.sitbak.home.notifications.NotificationActivity
import com.android.sitbak.home.popups.GuestAccessPopUp
import com.android.sitbak.home.shop_detail.ShopDetailsActivity
import com.android.sitbak.home.shop_search_result.ShopSearchResultActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ApiTags.GET_CART
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList


class ShopsFragment : Fragment(), ShopsAdapter.ShopItemInterface,
    ShopsProductAdapter.ShopProductInterface, LocationListener,
    GuestAccessPopUp.GuestAccessInterface, SortByBottomSheetFragment.SortByInterface, LocationBottomSheet.LocationBottomSheetInterface {

    private lateinit var viewModel: ShopFragmentVM
    private lateinit var mContext: Context
    private lateinit var productsAdapter: ShopsProductAdapter
    private lateinit var shopsAdapter: ShopsAdapter
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var userData: LoginModel
    private lateinit var locationManager: LocationManager
    private val selectedCategory: MutableLiveData<Int> = MutableLiveData()
    private val shopsList: MutableList<StoresData?> = ArrayList()
    private val categoriesList: MutableList<CategoriesData> = ArrayList()

    private lateinit var shopSearchInterface: ShopSearchInterface

    private var storeSearchLocation: DefaultLocation? = DefaultLocation(
        "", -1, -1, "0.0", "0.0"
    )

    private var currentLocationAddress = ""


    private var skip = 0
    private var sortType = ""
    private val cartListData: MutableList<CartData> = ArrayList()

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentShopsBinding
    }

    interface ShopSearchInterface {
        fun shopSearchResult(data: StoresModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShopsBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)


        initVM()
        initObservers()
        initViews()
        initProductsAdapter()
        initShopsAdapter()
        initListeners()

        checkLocationAndHitApi()

        binding.btnSortBy.isEnabled = false
    }

    private fun checkLocationAndHitApi() {
        if (!AppController.isGuest) {
            userData = AppUtils.getUserDetails(mContext)
            val dl = userData.data.default_location
            if (dl != null) {
                binding.tvCurrentLocation.text = dl.address
                storeSearchLocation = DefaultLocation(
                    dl.address,
                    dl.id,
                    dl.is_default,
                    dl.latitude,
                    dl.longitude,
                )
                getStoreCategories()
            } else {
                binding.llEnableLocation.viewVisible()
                binding.llEnableLocation.setOnClickListener {
                    getLocationAccess()
                }
            }
        } else if (storeSearchLocation?.address?.isNotBlank() == true) {
            binding.tvCurrentLocation.text = storeSearchLocation?.address
            getStoreCategories()
        } else {
            binding.llEnableLocation.viewVisible()
            binding.llEnableLocation.setOnClickListener {
                getLocationAccess()
            }
        }
    }

    private fun getStoreCategories() {
        apiCall = if (AppController.isGuest) retrofitClient.getCategoriesGuest() else retrofitClient.getCategoriesUser()
        viewModel.getCategories(apiCall)
    }

    private fun getStores(search: String, tag: String) {
        val apiCall = if (AppController.isGuest)
            retrofitClient.getStoresGuest(
                selectedCategory.value!!, search,
                storeSearchLocation?.latitude!!, storeSearchLocation?.longitude!!,
                sortType, skip
            )
        else
            retrofitClient.getStoresUser(
                selectedCategory.value!!, search,
                storeSearchLocation?.latitude!!, storeSearchLocation?.longitude!!,
                sortType, skip
            )
        viewModel.getStores(apiCall, tag)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ShopFragmentVM::class.java)
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
                        apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    when (it.tag) {
                        ApiTags.GET_CATEGORIES -> {
                            val model = Gson().fromJson(it.data.toString(), CategoriesModel::class.java)
                            handleCategoriesResponse(model.data)
                        }
                        ApiTags.GET_STORES -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), StoresModel::class.java)
                            handleStoreResponse(model.data)
                        }
                        ApiTags.GET_STORES_SEARCH -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), StoresModel::class.java)

                            if (::shopSearchInterface.isInitialized) {
                                shopSearchInterface.shopSearchResult(model)
                            }
                        }
                        GET_CART -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), CartModel::class.java)
                            cartListData.clear()
                            cartListData.addAll(model.data)

                            if (!cartListData.isNullOrEmpty()) {
                                val intent = Intent(mContext, OrderWebViewActivity::class.java)
                                val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
                                val url = "http://178.128.29.7/sitbak/web_views_checkout/${cartListData[0].store_id}/$accessToken"
                                intent.putExtra("url", url)
                                mContext.startActivity(intent)
                            } else
                                AppUtils.showToast(requireActivity(), "Your cart is empty.", false)
                        }

                    }
                }
            }
        })

        selectedCategory.observe(viewLifecycleOwner, {
            shopsList.clear()
            skip = 0
            getStores("", ApiTags.GET_STORES)
        })

    }

    private fun handleCategoriesResponse(data: java.util.ArrayList<CategoriesData>) {
        binding.llEnableLocation.viewGone()
        if (data.isNotEmpty()) {
            categoriesList.clear()

            categoriesList.add(CategoriesData("", "All", 0, "", "", 0))
            categoriesList.addAll(data)

            selectedCategory.postValue(categoriesList[0].id)
            categoriesList[0].isSelected = 1
            productsAdapter.notifyDataSetChanged()

            binding.llNoData.viewGone()
        } else {
            binding.llNoData.viewVisible()
        }
    }

    private fun handleStoreResponse(data: List<StoresData>) {
        binding.llEnableLocation.viewGone()
        binding.topBarLayout.ivSearch.viewVisible()
        binding.btnSortBy.viewVisible()
        binding.btnSortBy.isEnabled = true

        if (shopsList.isNotEmpty())
            shopsList.removeAt(shopsList.size - 1)

        shopsList.addAll(data)

        if (data.size == AppController.pageCount) {
            shopsList.add(null)
            skip += AppController.pageCount
        }

        if (shopsList.isEmpty()) {
            binding.shopRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.shopRecyclerView.viewVisible()
        }

        shopsAdapter.notifyDataSetChanged()

    }

    private fun initViews() {
        boomViews()
        binding.topBarLayout.ivBack.viewGone()
        binding.topBarLayout.tvTitle.text = mContext.resources.getString(R.string.shop_screen_title)

        if (!AppController.isGuest) {
            binding.topBarLayout.ivNotification.viewVisible()
            binding.topBarLayout.ivCart.viewVisible()
        }
    }

    private fun boomViews() {
        Boom(binding.topBarLayout.ivCart)
        Boom(binding.topBarLayout.ivSearch)
        Boom(binding.topBarLayout.ivNotification)
        Boom(binding.tvCurrentLocation)
        Boom(binding.btnSortBy)
    }


    private fun initListeners() {
        binding.pullToRefresh.setOnRefreshListener {
            binding.pullToRefresh.isRefreshing = false

            if (categoriesList.isEmpty())
                getStoreCategories()
            else {
                shopsList.clear()
                skip = 0
                getStores("", ApiTags.GET_STORES)
            }
        }
        binding.topBarLayout.ivCart.setOnClickListener {
            AppUtils.preventDoubleClick(binding.topBarLayout.ivCart)
            if (!AppController.isGuest)
                getUserCart()
        }

        binding.topBarLayout.ivNotification.setOnClickListener {
            AppUtils.preventDoubleClick(binding.topBarLayout.ivNotification)
            mContext.startActivity(Intent(mContext, NotificationActivity::class.java))
        }

        binding.topBarLayout.ivSearch.setOnClickListener {
//            val dialog = ShopSearchDialog(this)
//            dialog.show(activity?.supportFragmentManager!!, "ShopSearchDialog")
            gotoSearchResultActivity("")
        }

        binding.btnSortBy.setOnClickListener {
            val dialog = SortByBottomSheetFragment(this, sortType)
            dialog.show(activity?.supportFragmentManager!!, "SortByBottomSheet")
        }

        binding.tvCurrentLocation.setOnClickListener {
            val dialog = LocationBottomSheet(storeSearchLocation?.address!!, storeSearchLocation?.id!!, currentLocationAddress, this)
            dialog.show(activity?.supportFragmentManager!!, "LocationBottomSheet")
        }

    }

    private fun initProductsAdapter() {
        productsAdapter = ShopsProductAdapter(this, categoriesList)
        binding.shopProductsRecyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        binding.shopProductsRecyclerView.adapter = productsAdapter
        HorizontalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(binding.shopProductsRecyclerView))

    }

    private fun initShopsAdapter() {
        shopsAdapter = ShopsAdapter(this, shopsList)
        binding.shopRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.shopRecyclerView.adapter = shopsAdapter

        binding.shopRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.btnSortBy.visibility == View.VISIBLE) {
                    binding.btnSortBy.hide()
                } else if (dy < 0 && binding.btnSortBy.visibility != View.VISIBLE) {
                    binding.btnSortBy.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    if (binding.btnSortBy.isEnabled)
                        binding.btnSortBy.show()
            }
        })
    }

    fun gotoSearchResultActivity(searchWord: String) {
        val intent = Intent(mContext, ShopSearchResultActivity::class.java)
        intent.putExtra("searchWord", searchWord)
        intent.putExtra("categoryId", selectedCategory.value)
        intent.putExtra("user_location", storeSearchLocation)
        startActivity(intent)
    }

    override fun onShopItemClicked(position: Int) {
        val intent = Intent(mContext, ShopDetailsActivity::class.java)
        intent.putExtra("storeId", shopsList[position]?.id)
        intent.putExtra("storeName", shopsList[position]?.name)
        intent.putExtra("storeDeliveryTime", shopsList[position]?.delivery_time)
        intent.putExtra("storeDistance", shopsList[position]?.distance)
        intent.putExtra("storeDelivery", shopsList[position]?.delivery)
        intent.putExtra("storeRating", shopsList[position]?.rating)
        intent.putExtra("storeImage", shopsList[position]?.photo_path)
        mContext.startActivity(intent)
    }

    override fun onLoadMoreClicked() {
        getStores("", ApiTags.GET_STORES)
    }

    override fun onShopProductClicked(position: Int) {
        Loader.showLoader(mContext)
        categoriesList.forEach { it.isSelected = 0 }
        categoriesList[position].isSelected = 1
        selectedCategory.postValue(categoriesList[position].id)
        productsAdapter.notifyDataSetChanged()
    }

    private fun getLocationAccess() {
        Dexter.withContext(mContext)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    when {
                        report?.areAllPermissionsGranted() == true -> {

                            if (!isLocationEnabled(mContext)) {
                                binding.btnSortBy.viewGone()
                                binding.topBarLayout.ivSearch.viewGone()
                                enableLoc()
                            } else {
                                Loader.showLoader(mContext)
                                locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, this@ShopsFragment)
                                if (location != null)
                                    setLocationAddress(LatLng(location.latitude, location.longitude))

                            }

                        }
                        report?.isAnyPermissionPermanentlyDenied == true -> {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", activity?.packageName, null)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        else -> {
                            getLocationAccess()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
                    p1?.continuePermissionRequest()
                }
            })
            .check()
    }

    override fun onLocationChanged(location: Location) {
        locationManager.removeUpdates(this)
        setLocationAddress(LatLng(location.latitude, location.longitude))
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun setLocationAddress(latLng: LatLng) {
        activity?.runOnUiThread {
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(mContext, Locale.getDefault())
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                val address: String = addresses[0].getAddressLine(0)

                if (address.isNotBlank()) {
                    storeSearchLocation = DefaultLocation(
                        address, -1, 1, latLng.latitude.toString(), latLng.longitude.toString()
                    )
                    binding.tvCurrentLocation.text = address
                    currentLocationAddress = address
                    getStoreCategories()

                } else {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), "Please enable gps and try again.", false)
                }

            } catch (e: Exception) {
                Loader.hideLoader()
                AppUtils.showToast(requireActivity(), "Please check your internet connection and try again.", false)
                e.printStackTrace()
            }
        }
    }

    override fun guestClickedLogin() {
        (activity as HomeActivity).gotoBoarding()
    }

    fun searchResult(searchWord: String, shopSearchInterface: ShopSearchInterface) {
//        this.shopSearchInterface = shopSearchInterface
//        getStores(searchWord, ApiTags.GET_STORES_SEARCH)
    }

    override fun onSortTypeClicked(type: String) {
        Loader.showLoader(mContext)
        sortType = if (sortType == type) "" else type
        shopsList.clear()
        skip = 0
        getStores("", ApiTags.GET_STORES)
    }

    private fun enableLoc() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(30 * 1000)
            .setFastestInterval(5 * 1000)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val pendingResult = LocationServices
            .getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        pendingResult.addOnCompleteListener { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let {
                    if (it is ApiException && it.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().

                        startIntentSenderForResult(
                            it.status.resolution.intentSender,
                            Constants.LOCATION_REQUEST_CODE, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.LOCATION_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocationAccess()
                }
            }
            Constants.FROM_LOCATION_BOTTOM -> {
                if (resultCode == Activity.RESULT_OK)
                    if (data != null) {
                        if (data.hasExtra("address") && data.hasExtra("lat") && data.hasExtra("lng")) {
                            storeSearchLocation = DefaultLocation(
                                data.getStringExtra("address")!!, -1, -1,
                                data.getStringExtra("lat")!!,
                                data.getStringExtra("lng")!!
                            )
                            binding.tvCurrentLocation.text = data.getStringExtra("address")!!
                            getStoreCategories()
                        }

                    }
            }
        }
    }

    override fun onLocationItemSelected(address: String, id: Int, latitude: String, longitude: String) {
        storeSearchLocation = DefaultLocation(address, id, -1, latitude, longitude)
        binding.tvCurrentLocation.text = address
        getStoreCategories()
    }

    override fun useMyLocation() {
        getLocationAccess()
    }


    override fun addNewLocation() {
        val intent = Intent(mContext, MapsActivity::class.java)
        intent.putExtra("fromBottomLocation", true)
        intent.putExtra("savedAddress", "")
        startActivityForResult(intent, Constants.FROM_LOCATION_BOTTOM)
    }

    private fun getUserCart() {
        apiCall = retrofitClient.getUserCart()
        viewModel.getUserCart(apiCall)
    }


}