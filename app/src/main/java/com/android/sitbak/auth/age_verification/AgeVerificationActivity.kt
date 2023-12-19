package com.android.sitbak.auth.age_verification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.MediaIntentCallBack
import com.android.sitbak.R
import com.android.sitbak.auth.add_photo.AddPhotoVM
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.current_location.CurrentLocationActivity
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.auth.mobile_number.MobileNumberActivity
import com.android.sitbak.auth.register.EmailConfirmationActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityAgeVerificationBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

class AgeVerificationActivity : BaseActivity(), MediaIntentCallBack {

    private lateinit var binding: ActivityAgeVerificationBinding
    private lateinit var photoUtil: PhotoUtil
    private lateinit var viewModel: AddPhotoVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var filePath = ""
    private var isCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgeVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.gray_nurse)
        blackStatusBarIcons()

        initVM()
        initObservers()
        listener()
        initVar()
        boomViews()
    }

    private fun boomViews() {
        Boom(binding.btnTakePicture)
        Boom(binding.btnAddFromGallery)
        Boom(binding.btnContinue)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(AddPhotoVM::class.java)
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
                        ApiTags.ADD_UPDATE_PHOTO -> {
                            AppUtils.showToast(this, it.data?.message!!, true)

                            var authProgress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)
                            authProgress += 20
                            SharedPreference.saveInteger(mContext, Constants.authProgress, authProgress)

                            val data = AppUtils.getUserDetails(mContext)
                            data.data.is_age_verified = 1
                            data.data.id_photo_path = filePath
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


    private fun initVar() {
        photoUtil = PhotoUtil(this, this)
        binding.topBarLayout.tvTitle.text = "Age verification"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            binding.authProgressBar.setProgress(SharedPreference.getInteger(mContext, Constants.authProgress, 0), true)
        else
            binding.authProgressBar.progress = SharedPreference.getInteger(mContext, Constants.authProgress, 0)

    }

    private fun listener() {
        binding.topBarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.btnAddFromGallery.setOnClickListener {
            isCamera = false
            if (checkPermissions()) {
                photoUtil.selectImageFromGallery()
            } else {
                requestPermission()
            }
        }
        binding.btnTakePicture.setOnClickListener {
            isCamera = true
            if (checkPermissions()) {
                startCamera()
            } else {
                requestPermission()
            }
        }
        binding.ivCross.setOnClickListener {
            filePath = ""
            Glide.with(this).load(R.drawable.ic_license).into(binding.ivImage)
            binding.ivCross.viewGone()
            binding.llContinue.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnContinue.setTextColor(
                ContextCompat.getColor(
                    this@AgeVerificationActivity,
                    R.color.green_100
                )
            )
            binding.btnContinue.isEnabled = false
        }

        binding.btnContinue.setOnClickListener {
            if (filePath.isNotBlank()) {

                val file = File(filePath)
                val requestBody: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("photo", file.name, requestBody)

                val type: RequestBody = "id_photo".toRequestBody("text/plain".toMediaTypeOrNull())

                apiCall = retrofitClient.addUpdateUserPhotos(type, multipartBody)
                viewModel.addUpdatePhoto(apiCall)
            }
        }

    }

    private fun startCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .maxResultSize(620, 620)
            .compress(1024)   //User can only capture image using Camera
            .start()
    }


    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        }

        return true
    }


    private fun requestPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    when {
                        report?.areAllPermissionsGranted() == true -> {
                            if (isCamera)
                                startCamera()
                            else
                                photoUtil.selectImageFromGallery()
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
                            requestPermission()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }


    override fun onPhotoIntentSuccess(imageUri: String) {
        Glide.with(this).load(imageUri).into(binding.ivImage)
    }

    override fun onMediaIntentSuccess(Uri: String, type: String) {
        Glide.with(this).load(Uri).into(binding.ivImage)
    }

    override fun onMultipleImagesSuccess(imagesList: ArrayList<String>) {
        Glide.with(this).load(imagesList[0]).into(binding.ivImage)
        filePath = imagesList[0]

        if (imagesList.isNullOrEmpty()) {
            binding.btnContinue.isEnabled = false
            binding.llContinue.setBackgroundResource(R.drawable.bg_tv_main_unselected)
            binding.btnContinue.setTextColor(
                ContextCompat.getColor(
                    this@AgeVerificationActivity,
                    R.color.green_100
                )
            )
            binding.ivCross.viewGone()


        } else if (imagesList.isNotEmpty()) {
            binding.btnContinue.isEnabled = true
            binding.llContinue.setBackgroundResource(R.drawable.bg_btn_main)
            binding.btnContinue.setTextColor(
                ContextCompat.getColor(
                    this@AgeVerificationActivity,
                    R.color.white
                )
            )
            binding.ivCross.viewVisible()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, currentUser: Intent?) {
        super.onActivityResult(requestCode, resultCode, currentUser)
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoUtil.ACTIVITY_START_GALLERY) {
            photoUtil.handleMultpleImagesGalleryIntent(this, currentUser!!)
        } else if (resultCode == Activity.RESULT_OK && requestCode == PhotoUtil.ACTIVITY_START_CAMERA_APP) {
            photoUtil.handleCameraIntent(this)
        } else if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = currentUser?.data
            Glide.with(this).load(fileUri).centerCrop().into(binding.ivImage)


            filePath = fileUri!!.path.toString()
            if (filePath.isNullOrEmpty()) {
                binding.btnContinue.isEnabled = false
                binding.llContinue.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                binding.btnContinue.setTextColor(
                    ContextCompat.getColor(
                        this@AgeVerificationActivity,
                        R.color.green_100
                    )
                )
                binding.ivCross.viewGone()


            } else if (filePath.isNotEmpty()) {
                binding.btnContinue.isEnabled = true
                binding.llContinue.setBackgroundResource(R.drawable.bg_btn_main)
                binding.btnContinue.setTextColor(
                    ContextCompat.getColor(
                        this@AgeVerificationActivity,
                        R.color.white
                    )
                )
                binding.ivCross.viewVisible()
            }

            //You can get File object from intent
//            val file: File = ImagePicker.getFile(currentUser)!!
//
//            //You can also get File Path from intent
//            val filePath: String = ImagePicker.getFilePath(currentUser)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(currentUser), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

}