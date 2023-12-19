package com.android.sitbak.home.location_bottom_sheet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.BottomSheetLocationsBinding
import com.android.sitbak.home.location_list.LocationListActivity
import com.android.sitbak.home.location_list.LocationListVM
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class LocationBottomSheet(
    val selectedLocationAddress: String,
    private val selectedLocationId: Int = -1,
    val currentLocationAddress: String,
    val locationBottomSheetInterface: LocationBottomSheetInterface
) :
    BottomSheetDialogFragment() {


    interface LocationBottomSheetInterface {
        fun onLocationItemSelected(address: String, id: Int, latitude: String, longitude: String)
        fun useMyLocation()
        fun addNewLocation()
    }

    private lateinit var binding: BottomSheetLocationsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: LocationListVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private val locationListData: MutableList<LocationBottomSheetData> = ArrayList()
    private lateinit var locationBottomAdapter: LocationBottomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetLocationsBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LocationListVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
//                    Loader.showLoader(mContext)
//                    Loader.progressKHUD?.setCancellable {
//                        if (this::apiCall.isInitialized)
//                            apiCall.cancel()
//                    }
                }
                ApiStatus.ERROR -> {
//                    Loader.hideLoader()
//                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_USER_LOCATIONS -> {
                            val model = Gson().fromJson(it.data.toString(), LocationBottomSheetModel::class.java)
                            handleListLocationResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun handleListLocationResponse(data: ArrayList<LocationBottomSheetData>) {
        if (data.isNotEmpty()) {
            locationListData.clear()
            locationListData.addAll(data)

            if (locationListData.isNotEmpty()) {
                binding.locationsRecyclerView.viewVisible()
            } else {
                binding.locationsRecyclerView.viewGone()
            }
            initAdapter()
        } else {
            binding.locationsRecyclerView.viewGone()
        }
    }

    private fun initViews() {

        if (AppController.isGuest) {
            binding.locationsRecyclerView.viewGone()
        } else {
            getUserLocations()
        }

        if (selectedLocationAddress.isNotBlank() && selectedLocationId == -1) {

            if (selectedLocationAddress == currentLocationAddress) {
                binding.tvUseMyCurrentLocation.text = selectedLocationAddress
                binding.tvSelectedLocation.viewGone()
            } else {
                binding.tvSelectedLocation.viewVisible()
                binding.tvSelectedLocation.text = selectedLocationAddress
            }
        } else {
            binding.tvSelectedLocation.viewGone()
        }

        binding.tvSelectedLocation.setOnClickListener {
            dismiss()
        }
    }

    private fun initListeners() {
        binding.tvUseMyCurrentLocation.setOnClickListener {
            dismiss()
            locationBottomSheetInterface.useMyLocation()
        }

        binding.btnAddNewLocation.setOnClickListener {
            dismiss()
            if (!AppController.isGuest) {
                startActivity(Intent(context, LocationListActivity::class.java))
            } else {
                locationBottomSheetInterface.addNewLocation()
            }
        }
    }

    private fun getUserLocations() {
        apiCall = retrofitClient.getUserLocations()
        viewModel.getUserLocations(apiCall)
    }


    private fun initAdapter() {
        locationBottomAdapter = LocationBottomAdapter(
            locationListData, selectedLocationId, object : LocationBottomAdapter.SelectLocation {
                override fun onLocationSelect(position: Int) {
                    dismiss()
                    locationBottomSheetInterface.onLocationItemSelected(
                        locationListData[position].address!!,
                        locationListData[position].id!!,
                        locationListData[position].latitude!!,
                        locationListData[position].longitude!!,
                    )
                }
            })
        binding.locationsRecyclerView.adapter = locationBottomAdapter
    }

}