package com.android.sitbak.home.shops

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.databinding.ItemShopsBinding
import com.bumptech.glide.Glide

class ShopsAdapter(val shopItemInterface: ShopItemInterface, val data: MutableList<StoresData?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val ITEM_TYPE = 0
        const val LOAD_MORE = 1
    }

    interface ShopItemInterface {
        fun onShopItemClicked(position: Int)
        fun onLoadMoreClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM_TYPE)
            ShopItem(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_shops, parent, false
                )
            )
        else
            LoadMore(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_load_more, parent, false
                )
            )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShopItem -> holder.bind(position)
            is LoadMore -> holder.bind()
        }

    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position] == null)
            LOAD_MORE
        else
            ITEM_TYPE
    }

    inner class ShopItem(val binding: ItemShopsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Glide.with(mContext)
                .load(data[position]?.photo_path)
                .placeholder(R.drawable.ic_shop_placeholder)
                .into(binding.ivShopImage)


            binding.tvShopName.text = data[position]?.name
            binding.tvTime.text = "${data[position]?.delivery_time.toString()} min"
            binding.tvDistance.text = "${data[position]?.distance.toString()} km"
            binding.tvDeliveryAmount.text = data[position]?.delivery
            binding.tvRating.text = data[position]?.rating

            binding.itemShop.setOnClickListener {
                shopItemInterface.onShopItemClicked(position)
            }
        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                shopItemInterface.onLoadMoreClicked()
            }
        }
    }

    fun updateList(list: MutableList<StoresData?>) {
        data.clear()
        data.addAll(list)
    }

}