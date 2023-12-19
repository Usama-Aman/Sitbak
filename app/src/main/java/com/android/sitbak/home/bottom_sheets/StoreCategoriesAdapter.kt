package com.android.sitbak.home.bottom_sheets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemStoreCategoryBinding
import com.android.sitbak.home.shop_detail.ShopDetailCategoryData
import org.jetbrains.anko.textColor

class StoreCategoriesAdapter(
    val storeCategoriesBottomSheetFragment: StoreCategoriesBottomSheetFragment,
    val shopDetailCategoriesList: MutableList<ShopDetailCategoryData>,
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

    override fun getItemCount(): Int = shopDetailCategoriesList.size

    inner class StoreCategoryItem(val binding: ItemStoreCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            if (position == storeCategoriesBottomSheetFragment.selectedCategoryPosition)
                binding.tvStoreCategory.textColor = ContextCompat.getColor(mContext, R.color.app_color)
            else
                binding.tvStoreCategory.textColor = ContextCompat.getColor(mContext, R.color.text_light_black)

            binding.tvStoreCategory.text = shopDetailCategoriesList[position].category_title

            binding.tvStoreCategory.setOnClickListener {
                storeCategoriesBottomSheetFragment.categorySelected(position)
            }
        }

    }

}