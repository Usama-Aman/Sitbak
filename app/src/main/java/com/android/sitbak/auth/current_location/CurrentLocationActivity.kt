package com.android.sitbak.auth.current_location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.activities.MapsActivity
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.age_verification.AgeVerificationActivity
import com.android.sitbak.auth.login.DefaultLocation
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.auth.register.EmailConfirmationActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityCurrentLocationBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.Constants.LOCATION_PERMISSION_REQUEST
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
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*


class CurrentLocationActivity : BaseActivity(), LocationListener {

    private lateinit var binding: ActivityCurrentLocationBinding
    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: CurrentLocationVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var mContext: Context

    private var userAddress = ""
    private var userLat = ""
    private var userLng = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        initListeners()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(CurrentLocationVM::class.java)
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
                        ApiTags.ADD_USER_LOCATION -> {
                            AppUtils.showToast(this, it.data?.message!!, true)

                            val data = AppUtils.getUserDetails(mContext)

                            val defaultLocation = DefaultLocation(userAddress, -1, 1, userLat, userLng)
                            data.data.default_location = defaultLocation
                            AppUtils.saveUserModel(mContext, data)

                            val intent: Intent
                            when {
                                data.data.email_verified_at == null -> {
                                    intent = Intent(mContext, EmailConfirmationActivity::class.java)
                                    intent.putExtra("email", data.data.email)
                                }
                                data.data.phone_verified_at == null -> {
                                    intent = Intent(mContext, MobileNumberActivity::class.java)
                                    intent.putExtra("type", "phone")
                                }
                                data.data.photo_path == null -> {
                                    intent = Intent(mContext, AddYourPhotoActivity::class.java)
                                }
                                data.data.is_age_verified == 0 -> {
                                    intent = Intent(mContext, AgeVerificationActivity::class.java)
                                }
                                data.data.default_location == null -> {
                                    intent = Intent(mContext, CurrentLocationActivity::class.java)
                                }
                                else -> {
                                    intent = Intent(mContext, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                                }
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(intent)
                                finish()
                            }, 1000)

                        }
                    }
                }
            }
        })
    }


    private fun initViews() {
        Boom(binding.btnCurrentLocation)
        Boom(binding.btnStartShopping)
        binding.topBarLayout.tvTitle.text = mContext.resources.getString(R.string.current_location_title)
    }

    private fun initListeners() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.txtIndicateLocations.setOnClickListener {
            val intent = Intent(mContext, MapsActivity::class.java)
            intent.putExtra("fromLocationList", false)
            intent.putExtra("savedAddress", "")
            startActivityForResult(intent, Constants.LOCATION_REQUEST_CODE)
        }

        binding.btnCurrentLocation.setOnClickListener {
            getLocationAccess()
        }

        binding.btnStartShopping.setOnClickListener {
            apiCall = retrofitClient.addUserLocation(userAddress, userLat, userLng)
            viewModel.addUserLocation(apiCall)
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
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, this@CurrentLocationActivity)
                            }
                        }
                        report?.isAnyPermissionPermanentlyDenied == true -> {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
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
        Loader.hideLoader()
        setLocationAddress(LatLng(location.latitude, location.longitude))
    }

    private fun setLocationAddress(latLng: LatLng) {
        runOnUiThread {
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(this, Locale.getDefault())
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                val address: String = addresses[0].getAddressLine(0)

                if (address.isNotBlank()) {
                    binding.txtIndicateLocations.text = address
                    binding.btnStartShopping.isEnabled = true
                    binding.btnStartShopping.background = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_main)
                    binding.btnStartShopping.setTextColor(ContextCompat.getColor(mContext, R.color.white))

                    userLat = latLng.latitude.toString()
                    userLng = latLng.longitude.toString()
                    userAddress = address

                } else
                    Toast.makeText(mContext, "Please enable gps and try again.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            .getSettingsClient(this)
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
            Constants.LOCATION_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    userAddress = data?.getStringExtra("address")!!
                    userLat = data.getStringExtra("lat")!!
                    userLng = data.getStringExtra("lng")!!

                    binding.txtIndicateLocations.text = userAddress
                    binding.btnStartShopping.isEnabled = true
                    binding.btnStartShopping.background = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_main)
                    binding.btnStartShopping.setTextColor(ContextCompat.getColor(mContext, R.color.white))
                }
            }
            2000 -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocationAccess()
                }
            }
        }
    }

}