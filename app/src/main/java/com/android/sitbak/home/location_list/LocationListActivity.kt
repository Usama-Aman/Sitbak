package com.android.sitbak.home.location_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.activities.MapsActivity
import com.android.sitbak.auth.login.DefaultLocation
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityLocationListBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ApiTags.ADD_USER_LOCATION
import com.android.sitbak.network.ApiTags.DELETE_USER_LOCATION
import com.android.sitbak.network.ApiTags.SET_AS_DEFAULT_LOCATION
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.Constants.LOCATION_REQUEST_CODE
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class LocationListActivity : BaseActivity(), LocationListAdapter.LocationListInterface {

    private lateinit var binding: ActivityLocationListBinding
    private lateinit var locationListAdapter: LocationListAdapter
    private lateinit var viewModel: LocationListVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private val locationListData: MutableList<LocationListData> = ArrayList()

    private lateinit var userData: LoginModel
    private var selectedPosition = -1

    companion object {
        const val SET_AS_DEFAULT_RESULT_CODE = 3000
        const val ADD_LOCATION_RESULT_CODE = 3001
        const val DELETE_LOCATION_RESULT_CODE = 3002
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_list)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        initListeners()
        initAdapter()

        getUserLocations()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LocationListVM::class.java)
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
                        ApiTags.GET_USER_LOCATIONS -> {
                            val model = Gson().fromJson(it.data.toString(), LocationListModel::class.java)
                            handleListLocationResponse(model.data)
                        }
                        DELETE_USER_LOCATION -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            locationListData.removeAt(selectedPosition)
                            locationListAdapter.notifyDataSetChanged()
                            selectedPosition = -1

                            if (locationListData.isEmpty()) {
                                binding.llNoData.viewVisible()
                                binding.locationsRecyclerView.viewGone()
                            }
                        }
                        ADD_USER_LOCATION -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getUserLocations()
                            }, 1000)
                        }
                        SET_AS_DEFAULT_LOCATION -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getUserLocations()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun handleListLocationResponse(data: java.util.ArrayList<LocationListData>) {
        if (data.isNotEmpty()) {
            locationListData.clear()
            locationListData.addAll(data.filter { it.is_default == 0 })
            locationListAdapter.notifyDataSetChanged()

            val defaultLocation = data.singleOrNull { it.is_default == 1 }

            if (defaultLocation != null) {
                binding.tvDefaultLocation.text = defaultLocation.address
                setUpUserDefaultLocation(defaultLocation)
            }

            if (locationListData.isNotEmpty()) {
                binding.llNoData.viewGone()
                binding.locationsRecyclerView.viewVisible()
            } else {
                binding.llNoData.viewVisible()
                binding.locationsRecyclerView.viewGone()
            }

        } else {
            binding.llNoData.viewVisible()
            binding.locationsRecyclerView.viewGone()
        }
    }

    private fun initViews() {
        Boom(binding.llAddNewLocation)
        Boom(binding.tvDefaultLocation)
        binding.topBar.tvTitle.text = mContext.resources.getString(R.string.location_list_screen_title)
    }

    private fun setUpUserDefaultLocation(defaultLocation: LocationListData) {
        userData = AppUtils.getUserDetails(mContext)
        userData.data.default_location = DefaultLocation(
            defaultLocation.address!!,
            defaultLocation.id!!,
            1,
            defaultLocation.latitude!!,
            defaultLocation.longitude!!,
        )
        AppUtils.saveUserModel(mContext, userData)
    }

    private fun initListeners() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.tvDefaultLocation.setOnClickListener {
            selectedPosition = -1
            openMap(
                userData.data.default_location?.address,
                userData.data.default_location?.latitude,
                userData.data.default_location?.longitude,
                true
            )
        }

        binding.llAddNewLocation.setOnClickListener {
            selectedPosition = -1
            openMap("", "0.0", "0.0", false)
        }
    }

    private fun initAdapter() {
        locationListAdapter = LocationListAdapter(this, locationListData)
        binding.locationsRecyclerView.adapter = locationListAdapter
    }

    private fun getUserLocations() {
        apiCall = retrofitClient.getUserLocations()
        viewModel.getUserLocations(apiCall)
    }

    override fun onLocationItemClicked(position: Int) {
        selectedPosition = position
        val data = locationListData[position]
        openMap(data.address, data.latitude, data.longitude, false)
    }

    override fun onLocationItemDelete(position: Int) {
        selectedPosition = position
        deleteLocationItem(position)
    }

    private fun openMap(address: String?, latitude: String?, longitude: String?, isDefault: Boolean) {
        val intent = Intent(mContext, MapsActivity::class.java)
        intent.putExtra("fromLocationList", true)
        intent.putExtra("savedAddress", address)
        intent.putExtra("lat", latitude?.toDouble())
        intent.putExtra("lng", longitude?.toDouble())
        intent.putExtra("isDefault", isDefault)
        startActivityForResult(intent, LOCATION_REQUEST_CODE)
    }

    private fun deleteLocationItem(position: Int) {
        apiCall = retrofitClient.deleteUserLocation(locationListData[position].id!!)
        viewModel.deleteUserLocation(apiCall)
    }

    private fun setAsDefaultLocation(position: Int) {
        apiCall = retrofitClient.setDefaultUserLocation(locationListData[position].id!!)
        viewModel.setAsDefault(apiCall)
    }

    private fun addLocation(address: String, lat: String, lng: String) {
        apiCall = retrofitClient.addUserLocation(address, lat, lng)
        viewModel.addUserLocation(apiCall)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST_CODE)
            when (resultCode) {
                DELETE_LOCATION_RESULT_CODE -> {
                    deleteLocationItem(selectedPosition)
                }
                ADD_LOCATION_RESULT_CODE -> {
                    if (data?.hasExtra("address") == true && data.hasExtra("lat") && data.hasExtra("lng"))
                        addLocation(
                            data.getStringExtra("address")!!,
                            data.getStringExtra("lat")!!,
                            data.getStringExtra("lng")!!,
                        )
                }
                SET_AS_DEFAULT_RESULT_CODE -> {
                    setAsDefaultLocation(selectedPosition)
                }
            }
    }

}