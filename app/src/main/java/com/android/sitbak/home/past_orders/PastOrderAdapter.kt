package com.android.sitbak.home.past_orders

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.databinding.ItemPastOrdersBinding
import com.android.sitbak.utils.AppUtils
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide

class PastOrderAdapter(val pastOrderInterface: PastOrderInterface, val data: MutableList<PastOrdersData?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface PastOrderInterface {
        fun onStarClicked(position: Int)
        fun orderAgainClicked(position: Int)
        fun onOrderDetailClicked(position: Int)
        fun onLoadMoreClicked()
    }

    companion object {
        private const val ITEM_TYPE = 0
        private const val LOAD_MORE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        return if (viewType == ITEM_TYPE) {
            PastOrderItem(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_past_orders, parent, false
                )
            )
        } else {
            LoadMore(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_load_more, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PastOrderItem -> holder.bind(position)
            is LoadMore -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position] == null) LOAD_MORE else ITEM_TYPE
    }

    inner class PastOrderItem(_binding: ItemPastOrdersBinding) : RecyclerView.ViewHolder(_binding.root) {
        private val binding = _binding

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            Glide.with(mContext)
                .load(data[position]?.store?.photo_path)
                .placeholder(R.drawable.shop_image)
                .into(binding.ivShopImage)

            binding.tvShopName.text = data[position]?.store?.name
            val itemOrItems = if (data[position]?.items!! > 0) "items" else "item"
            binding.tvItemDetails.text =
                "${data[position]?.items.toString()} $itemOrItems, for $${data[position]?.grand_total}"

            binding.tvDateDetails.text = data[position]?.created_at
            binding.tvDeliveryDetails.text = data[position]?.delivery?.address

            if (data[position]?.is_favourite == 0)
                binding.ivStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_grey))
            else
                binding.ivStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_filled))


            binding.ivStar.setOnClickListener {
                AppUtils.preventDoubleClick(binding.ivStar)
                pastOrderInterface.onStarClicked(position)
            }

            Boom(binding.btnDetails)
            Boom(binding.btnOrderAgain)

            binding.btnDetails.setOnClickListener {
                AppUtils.preventDoubleClick(binding.btnDetails)
                pastOrderInterface.onOrderDetailClicked(adapterPosition)
            }

            binding.btnOrderAgain.setOnClickListener {
                AppUtils.preventDoubleClick(binding.btnDetails)
                pastOrderInterface.orderAgainClicked(adapterPosition)
            }
        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.loadMoreItem.setOnClickListener {
                pastOrderInterface.onLoadMoreClicked()
            }
        }

    }
}