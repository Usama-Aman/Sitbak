package com.android.sitbak.home.active_orders

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.activities.MapsActivity
import com.android.sitbak.activities.order_webview.OrderWebViewActivity
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentActiveOrderBinding
import com.android.sitbak.home.chat.ChatActivity
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.popups.RatingAlertPopup
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.OrdersDeliveryStatus.BEING_PREPARED
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_STORE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_USER_PLACE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_ON_WAY
import com.android.sitbak.utils.OrdersDeliveryStatus.FULFILLED
import com.android.sitbak.utils.OrdersDeliveryStatus.PENDING
import com.android.sitbak.utils.OrdersDeliveryStatus.PICKED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.READY_FOR_PICKUP
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.takusemba.multisnaprecyclerview.MultiSnapHelper
import com.takusemba.multisnaprecyclerview.SnapGravity
import kotlinx.android.synthetic.main.fragment_active_order.*
import kotlinx.android.synthetic.main.popup_start_shopping.view.*
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import okhttp3.ResponseBody
import retrofit2.Call


class ActiveOrderFragment : Fragment(), OnMapReadyCallback, LocationListener, ActiveOrdersAdapter.ActiveOrderInterface,
    RatingAlertPopup.RatingInterface {

    lateinit var binding: FragmentActiveOrderBinding
    lateinit var viewModel: ActiveOrderFragmentVM
    private lateinit var mContext: Context
    private lateinit var locationManager: LocationManager
    private lateinit var mMap: GoogleMap
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private val activeOrderList: MutableList<ActiveOrderData?> = ArrayList()
    private lateinit var activeOrdersAdapter: ActiveOrdersAdapter

    private lateinit var userData: LoginModel
    private var mapView: View? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var currentLocation: Location

    private var currentOrderPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActiveOrderBinding.inflate(inflater, container, false)
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
        initializeOrders()

        if (!AppController.isGuest)
            getActiveOrders()
    }

    private fun getActiveOrders() {
        apiCall = retrofitClient.getOrders("active", 0)
        viewModel.getOrders(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ActiveOrderFragmentVM::class.java)
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
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_ACTIVE_ORDERS -> {
                            val model = Gson().fromJson(it.data.toString(), ActiveOrderModel::class.java)
                            handleOrdersResponse(model.data)
                        }
                        ApiTags.POST_ORDER_RATING -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }
                    }
                }
            }
        })

        viewModel.getOrderStatusResponse().observe(viewLifecycleOwner, {

            try {
                val orderId = it.getInt("id")
                val userId = it.getInt("user_id")
                val status = it.getString("status")

                for (i in activeOrderList.indices)
                    if (activeOrderList[i]?.id == orderId) {

                        when (status) {
                            FULFILLED -> {
                                activeOrderList.removeAt(i)
                                activeOrdersAdapter.notifyDataSetChanged()
                                if (activeOrderList.isNotEmpty()) {
                                    binding.activeOrdersRecyclerView.smoothScrollToPosition(0)
                                    updateMapAndViews(0)
                                }

                                val dialog = RatingAlertPopup(this, activeOrderList[i]?.id, activeOrderList[i]?.store?.name)
                                dialog.show(activity?.supportFragmentManager!!, "AgeAlertPopup")

                            }
                            DRIVER_AT_STORE -> {
                                getActiveOrders()
                            }
                            else -> {
                                activeOrderList[i]?.status = status
                                activeOrdersAdapter.notifyDataSetChanged()
                                if (currentOrderPosition == i)
                                    updateMapAndViews(i)
                            }
                        }

                        break
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        })

        viewModel.getDriverLocationResponse().observe(viewLifecycleOwner, {


//            val driverId = it.getInt("id")
//            val userId = it.getInt("user_id")
            try {
                val orderId = it.getInt("order_id")
                val latitude = it.getDouble("latitude")
                val longitude = it.getDouble("longitude")

                for (i in activeOrderList.indices)
                    if (activeOrderList[i]?.id == orderId) {
                        activeOrderList[i]?.delivery?.latitude = latitude.toString()
                        activeOrderList[i]?.delivery?.longitude = longitude.toString()
                        activeOrdersAdapter.notifyItemChanged(i)

                        if (currentOrderPosition == i)
                            updateMapAndViews(i)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })


    }


    private fun initViews() {
        if (!AppController.isGuest)
            userData = AppUtils.getUserDetails(mContext)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun initListeners() {
        binding.constraintStartShopping.btnStartShopping.setOnClickListener {
            (activity as HomeActivity).gotoShopping()
        }

        binding.constraintIndicateLocation.tvIndicateLocation.setOnClickListener {
            startActivityForResult(Intent(mContext, MapsActivity::class.java), 1200)
        }
    }

    private fun initializeOrders() {
        activeOrdersAdapter = ActiveOrdersAdapter(this, activeOrderList)
        binding.activeOrdersRecyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        binding.activeOrdersRecyclerView.adapter = activeOrdersAdapter

        val multiSnapHelper = MultiSnapHelper(SnapGravity.START, 1, 50f)
        multiSnapHelper.attachToRecyclerView(binding.activeOrdersRecyclerView)
        HorizontalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(binding.activeOrdersRecyclerView))


        binding.activeOrdersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                println("Scroll state")

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val position = lm.findFirstVisibleItemPosition()
                    println("$position")

                    currentOrderPosition = position
                    updateMapAndViews(position)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                println("Scrolled")

            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapView = mapFragment.view

        mMap.setOnMarkerClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 15f))
            true
        }
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
                                enableLoc()
                            } else {
                                Loader.showLoader(mContext)
                                locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, this@ActiveOrderFragment)
                                changeLocationButtonPosition()
                                mMap.isMyLocationEnabled = true
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


    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun changeLocationButtonPosition() {
        if (mapView != null) {
            val locationButton = (mapView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
            val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            rlp.setMargins(0, 0, 30, 30)
        }
    }

    override fun onLocationChanged(location: Location) {
        Loader.hideLoader()
        locationManager.removeUpdates(this)
        currentLocation = location
        addMarkerAndZoom(LatLng(location.latitude, location.longitude))
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        AlertDialog.Builder(mContext)
            .setMessage(R.string.gps_network_not_enabled)
            .setPositiveButton(
                R.string.open_location_settings
            ) { _, _ ->
                mContext.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.Cancel) { _, _ ->
                (activity as HomeActivity).onBackPressed()
            }
            .show()
    }

    private fun addMarkerAndZoom(latLng: LatLng, driverImage: String? = null) {
        mMap.clear()
        try {
            activity?.runOnUiThread {
                val marker: MarkerOptions = if (driverImage == null) {
                    MarkerOptions()
                        .position(LatLng(latLng.latitude, latLng.longitude))
                        .icon(AppUtils.bitmapDescriptorFromVector(mContext, R.drawable.ic_marker_pin))

                } else {
                    MarkerOptions()
                        .position(LatLng(latLng.latitude, latLng.longitude))
                        .icon(AppUtils.createDrawableFromView(mContext, driverImage))
                }
                mMap.addMarker(marker)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun nextOrderClicked(position: Int) {
        if (position < activeOrderList.size)
            binding.activeOrdersRecyclerView.smoothScrollToPosition(position)
    }

    override fun previousOrderClicked(position: Int) {
        if (position >= 0)
            binding.activeOrdersRecyclerView.smoothScrollToPosition(position)
    }

    override fun onOrderItemCliked(position: Int) {
        val intent = Intent(mContext, OrderWebViewActivity::class.java)
        val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
        val url = "http://178.128.29.7/sitbak/web_views_order_details/${activeOrderList[position]?.id}/$accessToken"
        intent.putExtra("url", url)
        mContext.startActivity(intent)
    }

    private fun handleOrdersResponse(data: ArrayList<ActiveOrderData>) {

        if (data.isNotEmpty()) {
            activeOrderList.clear()
//            activeOrderList.addAll(data.filterNot { it.delivery_status == DELIVERED })
            activeOrderList.addAll(data)

            if (activeOrderList.isNotEmpty()) {
                updateMapAndViews(0)
                binding.activeOrdersRecyclerView.viewVisible()
                binding.constraintStartShopping.viewGone()
                activeOrdersAdapter.notifyDataSetChanged()
            } else {
                binding.activeOrdersRecyclerView.viewGone()
                binding.constraintStartShopping.viewVisible()
                binding.constraintStartShopping.tvIndicateLocation.text = userData.data.default_location?.address
            }
        } else {

            if (::currentLocation.isInitialized)
                addMarkerAndZoom(LatLng(currentLocation.latitude, currentLocation.longitude))
            else
                getLocationAccess()

            binding.activeOrdersRecyclerView.viewGone()
            binding.constraintStartShopping.viewVisible()
            binding.constraintStartShopping.tvIndicateLocation.text = userData.data.default_location?.address
        }
    }

    private fun updateMapAndViews(position: Int) {

        val drawable = when (activeOrderList[position]?.status) {
            PENDING, BEING_PREPARED, READY_FOR_PICKUP -> R.drawable.ic_order_tooltip_light_green
            DRIVER_AT_STORE -> R.drawable.ic_order_tooltip_blue
            PICKED_BY_DRIVER -> R.drawable.ic_order_tooltip_orange
            DRIVER_ON_WAY -> R.drawable.ic_order_tooltip_purple
            DRIVER_AT_USER_PLACE -> R.drawable.ic_order_tooltip_green
            else -> R.drawable.ic_order_tooltip_red
        }
        (activity as HomeActivity).showOrderTooltip(activeOrderList[position]?.updated_time!!, drawable)


        when (activeOrderList[position]?.status) {
            PENDING, BEING_PREPARED, READY_FOR_PICKUP -> {
                binding.riderCallConstraint.viewGone()
                if (::currentLocation.isInitialized)
                    addMarkerAndZoom(LatLng(currentLocation.latitude, currentLocation.longitude))
                else {
                    addMarkerAndZoom(
                        LatLng(
                            activeOrderList[position]?.delivery?.latitude?.toDouble()!!,
                            activeOrderList[position]?.delivery?.longitude?.toDouble()!!
                        )
                    )
                }
            }
            PICKED_BY_DRIVER, DRIVER_ON_WAY, DRIVER_AT_STORE, DRIVER_AT_USER_PLACE -> {
                binding.riderCallConstraint.viewVisible()
                val delivery = activeOrderList[position]?.delivery

                if (delivery != null) {
                    addMarkerAndZoom(
                        LatLng(delivery.latitude.toDouble(), delivery.longitude.toDouble()),
                        activeOrderList[position]?.delivery?.driver?.photo_path
                    )

                    val driver: ActiveOrdersDriver? = delivery.driver

                    if (driver != null) {
                        if (driver.photo_path != null)
                            Glide.with(mContext)
                                .load(driver.photo_path)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .into(binding.ivRiderImage)

                        binding.tvRiderName.text = driver.name

                        binding.ivCallToRider.setOnClickListener {
                            val intent = Intent(ACTION_DIAL)
                            intent.data = Uri.parse("tel:${driver.phone_number}")
                            startActivity(intent)
                        }

                        binding.ivMsgToRider.setOnClickListener {
                            val intent = Intent(mContext, ChatActivity::class.java)
                            intent.putExtra("orderId", activeOrderList[position]?.id)
                            intent.putExtra("senderName", driver.name)
                            intent.putExtra("senderPhoto", driver.photo_path)
                            mContext.startActivity(intent)
                        }
                    }
                }
            }
            else -> {
                binding.riderCallConstraint.viewGone()
                if (::currentLocation.isInitialized)
                    addMarkerAndZoom(LatLng(currentLocation.latitude, currentLocation.longitude))
                else
                    mMap.clear()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        AppController.goToTabFromWebView = 0
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
                            2000, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2000 -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocationAccess()
                }
            }
        }
    }

    override fun postRating(storeRating: Float, driverRating: Float, orderId: Int?) {
        apiCall = retrofitClient.postOrderRating(storeRating.toDouble(), driverRating.toDouble(), orderId!!)
        viewModel.postOrderRating(apiCall)
    }

}
