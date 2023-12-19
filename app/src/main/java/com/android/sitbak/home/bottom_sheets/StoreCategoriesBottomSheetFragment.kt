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
import com.android.sitbak.home.shop_detail.ShopDetailCategoryData
import com.android.sitbak.home.shop_detail.ShopDetailsActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class StoreCategoriesBottomSheetFragment(
    private val shopDetailsActivity: ShopDetailsActivity,
    val shopDetailCategoriesList: MutableList<ShopDetailCategoryData>,
    _selectedCategoryPosition: Int
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetStoreCategoriesBinding
    private lateinit var mContext: Context
    private lateinit var storeCategoriesAdapter: StoreCategoriesAdapter

    var selectedCategoryPosition = _selectedCategoryPosition

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
        binding.tvStoreCategories.text = mContext.resources.getString(R.string.storeCategories)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        storeCategoriesAdapter = StoreCategoriesAdapter(this, shopDetailCategoriesList)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.categoryRecyclerView.adapter = storeCategoriesAdapter
    }

    fun categorySelected(position: Int) {
        selectedCategoryPosition = position
        storeCategoriesAdapter.notifyDataSetChanged()

        shopDetailsActivity.onCategorySelected(position)
        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, 100)
    }


}