package com.android.sitbak.home.payment_method

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.DialogDeleteLogoutAccountBinding
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide

class DeleteCardDialog(val deleteCard: DeleteCard, val deleteCardPosition: Int, private val paymentCardData: PaymentCardData?) : DialogFragment() {

    private lateinit var binding: DialogDeleteLogoutAccountBinding
    private lateinit var mContext: Context

    interface DeleteCard {
        fun deleteCardDialog(position: Int)
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
    ): View? {
        // Inflate the layout for this fragment
        binding = DialogDeleteLogoutAccountBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = true

        initViews()
        listener()
    }

    private fun initViews() {

        binding.tvCardNo.text = mContext.resources.getString(
            R.string.payment_card_number,
            paymentCardData?.last4
        )

        when (paymentCardData?.card_type) {
            "Visa" -> {
                showImage(R.drawable.ic_visa_card, binding.ivCardImage)
            }
            "MasterCard" -> {
                showImage(R.drawable.ic_master_card, binding.ivCardImage)
            }
            "AmericanExpress" -> {
                showImage(R.drawable.ic_american_express, binding.ivCardImage)
            }
            "DiscoverDiners" -> {
                showImage(R.drawable.ic_discover_card, binding.ivCardImage)
            }
            "UnionPay" -> {
                showImage(R.drawable.ic_union_pay, binding.ivCardImage)
            }
            "JapanCreditBureau" -> {
                showImage(R.drawable.ic_jcb_card, binding.ivCardImage)
            }
        }
    }

    private fun showImage(drawable: Int, ivCardImage: ImageView) {
        Glide.with(mContext)
            .load(drawable)
            .placeholder(R.drawable.shop_image)
            .into(ivCardImage)
    }


    private fun listener() {
        Boom(binding.llDelete)
        Boom(binding.llLogOut)
        Boom(binding.llCancel)

        binding.llCancel.setOnClickListener {
            dismiss()
        }

        binding.llDelete.setOnClickListener {
            dismiss()
            deleteCard.deleteCardDialog(deleteCardPosition)
        }

    }

}