package com.android.sitbak.home.active_orders

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemActiveOrdersBinding
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.OrdersDeliveryStatus.BEING_PREPARED
import com.android.sitbak.utils.OrdersDeliveryStatus.CANCELLED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.CANCELLED_BY_SHOP_ADMIN
import com.android.sitbak.utils.OrdersDeliveryStatus.CANCELLED_BY_USER
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_STORE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_USER_PLACE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_ON_WAY
import com.android.sitbak.utils.OrdersDeliveryStatus.PENDING
import com.android.sitbak.utils.OrdersDeliveryStatus.PICKED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.READY_FOR_PICKUP
import com.astritveliu.boom.Boom


class ActiveOrdersAdapter(val activeOrderInterface: ActiveOrderInterface, private val activeOrderList: MutableList<ActiveOrderData?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context: Context

    interface ActiveOrderInterface {
        fun nextOrderClicked(position: Int)
        fun previousOrderClicked(position: Int)
        fun onOrderItemCliked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val binding: ItemActiveOrdersBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_active_orders, parent, false
            )

        return OrderItem(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = activeOrderList.size

    inner class OrderItem(val binding: ItemActiveOrdersBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            Boom(binding.root)
            binding.tvOrderTime.text =
                "${
                    activeOrderList[position]?.start_time.toString()/*.replace("AM", "").replace("PM", "")*/
                }- ${activeOrderList[position]?.end_time.toString()}"

            binding.tvSellerName.text = activeOrderList[position]?.store?.name.toString()
            binding.tvOrderDate.text = activeOrderList[position]?.created_at.toString()

            val itemOrItems = if (activeOrderList[position]?.items!! > 0) "items" else "item"
            binding.tvOrderItems.text =
                "${activeOrderList[position]?.items.toString()} $itemOrItems, for $${activeOrderList[position]?.grand_total}"

            binding.tvDeliveryAddress.text = activeOrderList[position]?.delivery?.address

            when (activeOrderList[position]?.status) {
                PENDING, BEING_PREPARED, READY_FOR_PICKUP -> {
                    val color = ContextCompat.getColor(context, R.color.delivery_status_pending)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_pending)
                }
                PICKED_BY_DRIVER -> {
                    val color = ContextCompat.getColor(context, R.color.delivery_status_accepted)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_accepted)
                }
                DRIVER_ON_WAY -> {
                    val color = ContextCompat.getColor(context, R.color.delivery_status_ongoing)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_ongoing)
                }
                DRIVER_AT_STORE -> {
                    val color = ContextCompat.getColor(context, R.color.delivery_status_arrived_store)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_arrived_store)
                }
                DRIVER_AT_USER_PLACE -> {
                    val color = ContextCompat.getColor(context, R.color.app_color)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_arrived)
                }
                CANCELLED_BY_DRIVER -> {
                    val color = ContextCompat.getColor(context, R.color.red)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_cancelled_by_driver)
                }
                CANCELLED_BY_USER -> {
                    val color = ContextCompat.getColor(context, R.color.red)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_cancelled)
                }
                CANCELLED_BY_SHOP_ADMIN -> {
                    val color = ContextCompat.getColor(context, R.color.red)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text =
                        "${activeOrderList[position]?.updated_time} - " +
                                context.resources.getString(R.string.delivery_status_cancelled_by_shop)
                }
                else -> {
                    val color = ContextCompat.getColor(context, R.color.red)
                    binding.sideViewActiveOrder.setBackgroundColor(color)
                    binding.tvOrderStatus.setTextColor(color)
                    binding.tvOrderStatus.text = activeOrderList[position]?.status
                }
            }

            binding.btnNextOrder.setOnClickListener {
                activeOrderInterface.nextOrderClicked(position + 1)
            }
            binding.btnPreviousOrder.setOnClickListener {
                activeOrderInterface.previousOrderClicked(position - 1)
            }

            binding.activeOrderItem.setOnClickListener {
                AppUtils.preventDoubleClick(binding.activeOrderItem)
                activeOrderInterface.onOrderItemCliked(adapterPosition)
            }
        }
    }

}