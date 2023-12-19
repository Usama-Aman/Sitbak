package com.android.sitbak.home.bottom_sheets

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.databinding.BottomSheetStoreCategoriesBinding
import com.android.sitbak.home.shop_detail.ShopDetailsActivity
import com.android.sitbak.home.shop_detail.StraingTypeData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StraingTypeBottomSheetFragment(
    private val shopDetailsActivity: ShopDetailsActivity,
    private val shopDetailStraingTypes: MutableList<StraingTypeData>,
    _selectedStraingPosition: Int
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetStoreCategoriesBinding
    private lateinit var mContext: Context
    private lateinit var straingTypesAdapter: StraingTypesAdapter
    var selectedStraingPosition = _selectedStraingPosition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetStoreCategoriesBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initAdapter()
    }

    private fun initViews() {
        binding.tvStoreCategories.text = mContext.resources.getString(R.string.straingType)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        straingTypesAdapter = StraingTypesAdapter(this, shopDetailStraingTypes)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.categoryRecyclerView.adapter = straingTypesAdapter
    }

    fun straingTypeSelected(position: Int) {
        selectedStraingPosition = position
        straingTypesAdapter.notifyDataSetChanged()
        shopDetailsActivity.onStraingTypeSelected(position)

        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, 100)
    }


}