package com.android.sitbak.home.shop_detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.databinding.ItemRecyclerHeaderBinding
import com.android.sitbak.databinding.ItemShopDetailBinding
import com.bumptech.glide.Glide

class ShopDetailAdapter(val shopDetailInterface: ShopDetailInterface, private val shopDetailList: MutableList<ShopDetailData?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val HEADER_TYPE = 0
        const val ITEM_TYPE = 1
        const val LOAD_MORE = 2
    }

    interface ShopDetailInterface {
        fun onItemClick(position: Int)
        fun onLoadMoreClikecd()
    }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return when (viewType) {
            HEADER_TYPE -> ShopDetailHeader(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_recycler_header, parent, false
                )
            )
            LOAD_MORE -> LoadMore(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_load_more, parent, false
                )
            )
            else -> ShopDetailItem(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_shop_detail, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShopDetailItem -> holder.bind(position)
            is ShopDetailHeader -> holder.bind(position)
            is LoadMore -> holder.bind()
        }

    }

    override fun getItemCount(): Int = shopDetailList.size

    override fun getItemViewType(position: Int): Int {
        return when {
            shopDetailList[position] == null -> LOAD_MORE
            shopDetailList[position]?.isHeader == true -> HEADER_TYPE
            else -> ITEM_TYPE
        }
    }

    inner class ShopDetailItem(val binding: ItemShopDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            Glide.with(mContext)
                .load(shopDetailList[position]?.image?.src)
                .placeholder(R.drawable.ic_shop_placeholder)
                .into(binding.ivProductImage)

            binding.tvProductName.text = shopDetailList[position]?.product_type
            binding.tvProductShortDescription.text = shopDetailList[position]?.title
            binding.tvProductTags.text = shopDetailList[position]?.tags

            val shortestPriceIndex =
                shopDetailList[position]?.variants?.minByOrNull { it.price }?.let { shopDetailList[position]?.variants?.indexOf(it) }

            binding.tvProductQuantity.text =
                shortestPriceIndex?.let { shopDetailList[position]?.variants?.get(it)?.weight.toString() } +
                        shortestPriceIndex?.let { shopDetailList[position]?.variants?.get(it)?.weight_unit.toString() }

            binding.tvProductPrice.text =
                "$${shortestPriceIndex?.let { shopDetailList[position]?.variants?.get(it)?.price }}"


            binding.detailItemConstraint.setOnClickListener {
                shopDetailInterface.onItemClick(position)
            }
        }

    }

    inner class ShopDetailHeader(val binding: ItemRecyclerHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.tvProductName.text = shopDetailList[position]?.title
        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                shopDetailInterface.onLoadMoreClikecd()
            }
        }
    }
}