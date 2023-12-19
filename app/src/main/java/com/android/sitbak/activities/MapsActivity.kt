package com.android.sitbak.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.sitbak.R
import com.android.sitbak.auth.login.DefaultLocation
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityMapsBinding
import com.android.sitbak.home.location_list.LocationListActivity
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var mMap: GoogleMap
    private lateinit var mContext: Context

    private var fromLocationList = false
    private var fromBottomLocation = false
    private lateinit var savedLatLng: LatLng
    private lateinit var savedAddress: String

    private var userAddress = ""
    private var userLat = "0.0"
    private var userLng = "0.0"
    private var isDefault = false

    private var isMarkerDraggable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        mContext = this

        Places.initialize(mContext, mContext.resources.getString(R.string.google_places_key))

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initViews()
        initListeners()
        boomViews()
    }

    private fun boomViews() {
        Boom(binding.llAddLocation)
        Boom(binding.llSetAsDefault)
        Boom(binding.llDeleteLocation)
        Boom(binding.llSaveLocation)
        Boom(binding.llUseNewLocation)
    }

    private fun initViews() {
        fromLocationList = intent?.getBooleanExtra("fromLocationList", false)!!
        fromBottomLocation = intent?.getBooleanExtra("fromBottomLocation", false)!!
        isDefault = intent?.getBooleanExtra("isDefault", false)!!
        savedAddress = intent?.getStringExtra("savedAddress")!!

        if (savedAddress.isNotBlank()) {
            savedLatLng = LatLng(
                intent?.getDoubleExtra("lat", 0.0)!!,
                intent?.getDoubleExtra("lng", 0.0)!!
            )
        }

        binding.topBarLayout.tvTitle.text = resources.getString(R.string.maps_screen_title)

    }

    private fun initListeners() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }

        binding.llSaveLocation.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("address", userAddress)
            returnIntent.putExtra("lat", userLat)
            returnIntent.putExtra("lng", userLng)
            returnIntent.putExtra("isDefault", isDefault)
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        binding.llAddLocation.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("address", userAddress)
            returnIntent.putExtra("lat", userLat)
            returnIntent.putExtra("lng", userLng)
            returnIntent.putExtra("isDefault", isDefault)
            setResult(LocationListActivity.ADD_LOCATION_RESULT_CODE, returnIntent)
            finish()
        }

        binding.llDeleteLocation.setOnClickListener {
            setResult(LocationListActivity.DELETE_LOCATION_RESULT_CODE)
            finish()
        }
        binding.llSetAsDefault.setOnClickListener {
            setResult(LocationListActivity.SET_AS_DEFAULT_RESULT_CODE)
            finish()
        }

        binding.tvMapSearch.setOnClickListener {
            if (isMarkerDraggable) {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this)
                startActivityForResult(intent, 3000)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationAccess()

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker) {
            }

            override fun onMarkerDrag(p0: Marker) {
            }

            override fun onMarkerDragEnd(p0: Marker) {
                setLocationAddress(p0.position)
            }

        })
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
                                mMap.isMyLocationEnabled = true
                                locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, this@MapsActivity)
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
        Loader.hideLoader()
        hideShowButtons()
        if (savedAddress.isBlank()) {
            locationManager.removeUpdates(this)
            addMarkerAndZoom(LatLng(location.latitude, location.longitude))
            setLocationAddress(LatLng(location.latitude, location.longitude))
        }
    }

    private fun hideShowButtons() {
        if (fromLocationList) {
            if (savedAddress.isNotBlank()) {
                isMarkerDraggable = false
                if (!isDefault) {
                    binding.llDeleteLocation.viewVisible()
                    binding.llSetAsDefault.viewVisible()
                    binding.llSaveLocation.viewGone()
                    binding.llAddLocation.viewGone()
                } else {
                    binding.llSaveLocation.viewGone()
                    binding.llAddLocation.viewGone()
                    binding.llDeleteLocation.viewGone()
                    binding.llSetAsDefault.viewGone()
                }

                binding.tvMapSearch.text = savedAddress

                userLat = savedLatLng.latitude.toString()
                userLng = savedLatLng.longitude.toString()
                userAddress = savedAddress

                addMarkerAndZoom(savedLatLng)
            } else {
                isMarkerDraggable = true
                binding.llAddLocation.viewVisible()
                binding.llSaveLocation.viewGone()
                binding.llDeleteLocation.viewGone()
                binding.llSetAsDefault.viewGone()
            }
        } else if (fromBottomLocation) {
            isMarkerDraggable = true
            binding.llSaveLocation.viewGone()
            binding.llDeleteLocation.viewGone()
            binding.llSetAsDefault.viewGone()
            binding.llAddLocation.viewGone()
            binding.llUseNewLocation.viewVisible()
            binding.llUseNewLocation.setOnClickListener {
                val returnIntent = Intent()
                returnIntent.putExtra("address", userAddress)
                returnIntent.putExtra("lat", userLat)
                returnIntent.putExtra("lng", userLng)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        } else {
            isMarkerDraggable = true
            binding.llSaveLocation.viewVisible()
            binding.llDeleteLocation.viewGone()
            binding.llSetAsDefault.viewGone()
            binding.llAddLocation.viewGone()
        }
    }

    private fun addMarkerAndZoom(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .draggable(isMarkerDraggable)
                .position(latLng)
                .icon(AppUtils.bitmapDescriptorFromVector(mContext, R.drawable.ic_marker_pin))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun setLocationAddress(latLng: LatLng) {
        runOnUiThread {
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(this, Locale.getDefault())
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                val address: String = addresses[0].getAddressLine(0)

                if (address.isNotBlank()) {

                    userLat = latLng.latitude.toString()
                    userLng = latLng.longitude.toString()
                    userAddress = address

                    binding.tvMapSearch.text = userAddress
                } else
                    Toast.makeText(mContext, "Please enable gps and try again.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            3000 -> if (resultCode == Activity.RESULT_OK)
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)

                    val address = place.address
                    if (address != null) {
                        if (address.isNotBlank()) {
                            userLat = place.latLng?.latitude.toString()
                            userLng = place.latLng?.longitude.toString()
                            userAddress = address
                            binding.tvMapSearch.text = userAddress
                            addMarkerAndZoom(place.latLng!!)
                        }
                    }
                }
            Constants.LOCATION_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocationAccess()
                }
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
                            Constants.LOCATION_REQUEST_CODE, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

}
