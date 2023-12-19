package com.android.sitbak.home.bottom_sheets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemStoreCategoryBinding
import com.android.sitbak.home.shop_detail.StraingTypeData
import org.jetbrains.anko.textColor

class StraingTypesAdapter(
    val straingTypeBottomSheetFragment: StraingTypeBottomSheetFragment,
    val shopDetailStraingTypes: MutableList<StraingTypeData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val binding: ItemStoreCategoryBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_store_category, parent, false
            )

        return StoreCategoryItem(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StoreCategoryItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = shopDetailStraingTypes.size

    inner class StoreCategoryItem(val binding: ItemStoreCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            if (position == straingTypeBottomSheetFragment.selectedStraingPosition)
                binding.tvStoreCategory.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            else
                binding.tvStoreCategory.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)

            binding.tvStoreCategory.text = shopDetailStraingTypes[position].type_title

            binding.tvStoreCategory.setOnClickListener {
                straingTypeBottomSheetFragment.straingTypeSelected(position)
            }
        }

    }

}