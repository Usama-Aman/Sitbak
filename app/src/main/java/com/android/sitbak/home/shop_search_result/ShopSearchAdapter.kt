package com.android.sitbak.home.shop_search_result

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemShopSearchBinding
import com.android.sitbak.home.shops.StoresData
import com.bumptech.glide.Glide

class ShopSearchAdapter(val searchData: MutableList<StoresData>, val shopSearchResult: ShopSearchResult) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ShopSearchResult {
        fun shopSearchItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val binding: ItemShopSearchBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_shop_search, parent, false
            )

        return ShopSearchItem(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShopSearchItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = searchData.size

    inner class ShopSearchItem(val binding: ItemShopSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Glide.with(mContext)
                .load(searchData[position].photo_path)
                .placeholder(R.drawable.shop_image)
                .into(binding.ivShop)
            binding.tvShopName.text = searchData[position].name

            binding.itemSearchResult.setOnClickListener {
                shopSearchResult.shopSearchItemClicked(position)
            }
        }

    }

}