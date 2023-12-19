package com.android.sitbak.home.popups

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.FragmentRatingAlertPopupBinding
import com.astritveliu.boom.Boom
import me.zhanghai.android.materialratingbar.MaterialRatingBar


class RatingAlertPopup(private val ratingInterface: RatingInterface, private val orderId: Int?, private val storeName: String?) : DialogFragment() {

    private lateinit var binding: FragmentRatingAlertPopupBinding
    private lateinit var mContext: Context

    private var storeRating = 0f
    private var driverRating = 0f

    interface RatingInterface {
        fun postRating(storeRating: Float, driverRating: Float, orderId: Int?)
    }


    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRatingAlertPopupBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initVar()
        listener()
    }

    @SuppressLint("SetTextI18n")
    private fun initVar() {
        Boom(binding.btnSendAnEstimate)
        Boom(binding.btnCancel)

        binding.tvTitle.text = "You have just received your order from $storeName"

        binding.storeRatingBar.onRatingChangeListener = MaterialRatingBar.OnRatingChangeListener { _, rating ->
            storeRating = rating
            if (rating > 0.0 || driverRating > 0) {
                binding.btnSendAnEstimate.isEnabled = true
                binding.llSendAnEstimate.background = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_main)
                binding.btnSendAnEstimate.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            } else {
                binding.btnSendAnEstimate.isEnabled = false
                binding.llSendAnEstimate.background = ContextCompat.getDrawable(mContext, R.drawable.bg_tv_main_unselected)
                binding.btnSendAnEstimate.setTextColor(ContextCompat.getColor(mContext, R.color.green_100))
            }
        }

        binding.driverRatingBar.onRatingChangeListener = MaterialRatingBar.OnRatingChangeListener { _, rating ->
            driverRating = rating
            if (rating > 0.0 || storeRating > 0) {
                binding.btnSendAnEstimate.isEnabled = true
                binding.llSendAnEstimate.background = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_main)
                binding.btnSendAnEstimate.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            } else {
                binding.btnSendAnEstimate.isEnabled = false
                binding.llSendAnEstimate.background = ContextCompat.getDrawable(mContext, R.drawable.bg_tv_main_unselected)
                binding.btnSendAnEstimate.setTextColor(ContextCompat.getColor(mContext, R.color.green_100))
            }
        }

    }


    private fun listener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSendAnEstimate.setOnClickListener {
            ratingInterface.postRating(storeRating, driverRating, orderId)
            dismiss()
        }
    }


}