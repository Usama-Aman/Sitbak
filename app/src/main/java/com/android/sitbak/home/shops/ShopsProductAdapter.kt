package com.android.sitbak.home.shops

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemShopsProductsBinding
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide

class ShopsProductAdapter(val shopProductInterface: ShopProductInterface, val categoriesList: MutableList<CategoriesData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ShopProductInterface {
        fun onShopProductClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val binding: ItemShopsProductsBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_shops_products, parent, false
            )

        return ShopProductItem(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShopProductItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = categoriesList.size

    inner class ShopProductItem(val binding: ItemShopsProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Boom(binding.root)

            Glide.with(mContext)
                .load(categoriesList[position].image_path)
                .placeholder(R.drawable.ic_shop_placeholder)
                .into(binding.ivProductImage)

            binding.tvProductName.text = categoriesList[position].category_title

            if (categoriesList[position].isSelected == 0)
                binding.tvProductName.setTextColor(ContextCompat.getColor(mContext, R.color.text_light_black))
            else
                binding.tvProductName.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))


            binding.itemProductCategory.setOnClickListener {
                shopProductInterface.onShopProductClicked(position)
            }
        }

    }
}