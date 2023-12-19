package com.android.sitbak.home.location_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLocationListBinding
import com.astritveliu.boom.Boom

class LocationListAdapter(val locationListInterface: LocationListInterface, val locationListData: MutableList<LocationListData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface LocationListInterface {
        fun onLocationItemClicked(position: Int)
        fun onLocationItemDelete(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding: ItemLocationListBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_location_list, parent, false
            )

        return LocationItem(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LocationItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = locationListData.size
    inner class LocationItem(val binding: ItemLocationListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            Boom(binding.root)
            binding.tvDefaultLocation.text = locationListData[position].address

            binding.tvDefaultLocation.setOnClickListener {
                locationListInterface.onLocationItemClicked(position)
            }

            binding.ivCross.setOnClickListener {
                locationListInterface.onLocationItemDelete(position)
            }


        }

    }

}