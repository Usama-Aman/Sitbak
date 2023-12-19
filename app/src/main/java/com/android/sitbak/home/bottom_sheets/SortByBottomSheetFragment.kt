package com.android.sitbak.home.bottom_sheets

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import com.android.sitbak.R
import com.android.sitbak.databinding.BottomSheetSortByBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.jetbrains.anko.textColor

class SortByBottomSheetFragment(private val sortByInterface: SortByInterface, _sortType: String) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetSortByBinding
    private lateinit var mContext: Context
    private var sortType = _sortType

    interface SortByInterface {
        fun onSortTypeClicked(type: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetSortByBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        when (sortType) {
            "delivery_time" -> binding.tvTimeOfDelivery.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            "delivery" -> binding.tvDeliveryFee.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            "ratings_avg_rating" -> binding.tvRatingSort.textColor = ContextCompat.getColor(mContext, R.color.app_color)
        }



        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.tvTimeOfDelivery.setOnClickListener {
            sortType = "delivery_time"
            binding.tvTimeOfDelivery.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            binding.tvDeliveryFee.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)
            binding.tvRatingSort.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)

            sortByInterface.onSortTypeClicked(sortType)
            dismiss()
        }

        binding.tvDeliveryFee.setOnClickListener {
            sortType = "delivery"
            binding.tvDeliveryFee.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            binding.tvTimeOfDelivery.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)
            binding.tvRatingSort.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)

            sortByInterface.onSortTypeClicked(sortType)
            dismiss()
        }

        binding.tvRatingSort.setOnClickListener {
            sortType = "ratings_avg_rating"
            binding.tvRatingSort.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            binding.tvTimeOfDelivery.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)
            binding.tvDeliveryFee.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)

            sortByInterface.onSortTypeClicked(sortType)
            dismiss()
        }

    }


}