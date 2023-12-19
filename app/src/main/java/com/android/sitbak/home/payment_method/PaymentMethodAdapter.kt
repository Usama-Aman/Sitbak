package com.android.sitbak.home.payment_method

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemAddNewCardBinding
import com.android.sitbak.databinding.ItemPaymentMethodBinding
import com.android.sitbak.utils.viewGone
import com.android.sitbak.utils.viewVisible
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide

class PaymentMethodAdapter(val addCardInterface: AddCardInterface, val paymentCardList: MutableList<PaymentCardData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context
    private var selectedPosition = -1

    interface AddCardInterface {
        fun onAddCardClicked()
        fun deleteCard(position: Int)
        fun selectCard(position: Int)
    }

    companion object {
        const val ITEM_CARD = 0
        const val ITEM_ADD_CARD = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM_CARD) {
            PaymentMethodItem(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_payment_method, parent, false
                )
            )
        } else {
            AddPaymentMethodItems(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_add_new_card, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PaymentMethodItem)
            holder.bind(position)
        else if (holder is AddPaymentMethodItems)
            holder.bind(position)
    }

    override fun getItemCount(): Int = paymentCardList.size

    override fun getItemViewType(position: Int): Int {
        return if (paymentCardList[position].isAddCard) ITEM_ADD_CARD else ITEM_CARD
    }

    inner class PaymentMethodItem(_binding: ItemPaymentMethodBinding) : RecyclerView.ViewHolder(_binding.root) {

        private val binding = _binding

        fun bind(position: Int) {

            Boom(binding.root)

            binding.tvCardNo.text = mContext.resources.getString(
                R.string.payment_card_number,
                paymentCardList[position]?.last4
            )

            when (paymentCardList[position].card_type) {
                "Visa" -> {
                    showImage(R.drawable.ic_visa_card, binding.ivCardImage)
                }
                "MasterCard" -> {
                    showImage(R.drawable.ic_master_card, binding.ivCardImage)
                }
                "AmericanExpress" -> {
                    showImage(R.drawable.ic_american_express, binding.ivCardImage)
                }
                "DiscoverDiners" -> {
                    showImage(R.drawable.ic_discover_card, binding.ivCardImage)
                }
                "UnionPay" -> {
                    showImage(R.drawable.ic_union_pay, binding.ivCardImage)
                }
                "JapanCreditBureau" -> {
                    showImage(R.drawable.ic_jcb_card, binding.ivCardImage)
                }
            }

            if (paymentCardList[position].is_default == 0) {
                binding.ivCross.viewVisible()
                binding.ivTick.viewGone()
                binding.constraintCard.background = ContextCompat.getDrawable(mContext, R.drawable.drawable_payment_method_shadow)
            } else {
                binding.ivCross.viewGone()
                binding.ivTick.viewVisible()
                binding.constraintCard.background = ContextCompat.getDrawable(mContext, R.drawable.drawable_payment_selected_borders)
                selectedPosition = position
            }

            binding.constraintCard.setOnClickListener {
                if (selectedPosition != -1)
                    paymentCardList[selectedPosition].is_default = 0
                paymentCardList[position].is_default = 1

                addCardInterface.selectCard(position)
            }

            binding.ivCross.setOnClickListener {
                addCardInterface.deleteCard(position)
            }
        }

        private fun showImage(drawable: Int, ivCardImage: ImageView) {
            Glide.with(mContext)
                .load(drawable)
                .placeholder(R.drawable.shop_image)
                .into(ivCardImage)
        }

    }

    inner class AddPaymentMethodItems(val binding: ItemAddNewCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Glide.with(mContext)
                .load(R.drawable.ic_add_payment_card)
                .placeholder(R.drawable.shop_image)
                .into(binding.ivCardImage)

            binding.constraintCard.setOnClickListener {
                addCardInterface.onAddCardClicked()
            }
        }

    }
}