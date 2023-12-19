package com.android.sitbak.home.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemNotificationsBinding
import com.android.sitbak.utils.NotificationTypes


class NotificationsAdapter(private val notificationsData: MutableList<NotificationsData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val binding: ItemNotificationsBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_notifications, parent, false
            )

        return Item(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is Item) {
            holder.onBind(position)
        }

    }

    override fun getItemCount(): Int = notificationsData.size

    inner class Item(val binding: ItemNotificationsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvNotification.text = notificationsData[position].title
            binding.tvNotificationTime.text = notificationsData[position].time

            when (notificationsData[position].notification_type) {
                NotificationTypes.DriverAcceptDelivery -> {
                    binding.ivNotification.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_rider_picked))
                }
            }

        }

    }

}