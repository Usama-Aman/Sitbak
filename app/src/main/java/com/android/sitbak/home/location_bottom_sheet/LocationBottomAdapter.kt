package com.android.sitbak.home.location_bottom_sheet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLocationBottomSheetBinding


class LocationBottomAdapter(
    val locationListData: MutableList<LocationBottomSheetData>, val selectedLocationId: Int,
    val selectLocation: SelectLocation
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface SelectLocation {
        fun onLocationSelect(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val binding: ItemLocationBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_location_bottom_sheet, parent, false
            )

        return Item(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is Item) {
            holder.onBind(position)
        }

    }

    override fun getItemCount(): Int = locationListData.size

    inner class Item(val binding: ItemLocationBottomSheetBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            if (selectedLocationId == locationListData[position].id)
                binding.tvLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.drawable_selected_location_bottom_sheet, 0, 0, 0)
            else
                binding.tvLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.drawable_unselected_location_bottom_sheet, 0, 0, 0)
            binding.tvLocation.text = locationListData[position].address

            binding.tvLocation.setOnClickListener {
                selectLocation.onLocationSelect(position)
            }
        }

    }

}
