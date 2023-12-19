package com.android.sitbak.home.bottom_sheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.sitbak.R
import com.android.sitbak.databinding.BottomSheetAddPaymentCardBinding
import com.android.sitbak.utils.DateConverter
import com.android.sitbak.utils.Loader
import com.android.sitbak.utils.maskeditor.MaskTextWatcher
import com.android.sitbak.utils.viewGone
import com.android.sitbak.utils.viewVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import com.stripe.android.view.CardMultilineWidget
import org.jetbrains.anko.windowManager


class AddNewCardBottomSheetFragment(val paymentCardInterface: PaymentCardInterface) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAddPaymentCardBinding
    private lateinit var mContext: Context

    private lateinit var stripe: Stripe

    interface PaymentCardInterface {
        fun addCard(token: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
        val layoutParams = bottomSheet.layoutParams

        val displayMetrics = DisplayMetrics()
        mContext.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        if (layoutParams != null) {
            layoutParams.height = height
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetAddPaymentCardBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        stripe = Stripe(mContext, mContext.resources.getString(R.string.stripe_test_key))

        val textWatcherNumber = MaskTextWatcher(binding.etCardNumber, "#### #### #### ####")
        val textWatcherDate = MaskTextWatcher(binding.etCardDate, "##/####")
        binding.etCardDate.addTextChangedListener(textWatcherDate)
        binding.etCardNumber.addTextChangedListener(textWatcherNumber)


        binding.llCancel.setOnClickListener {
            dismiss()
        }

        binding.llAddNewCard.setOnClickListener {

            if (binding.etCardNumber.text.toString().length < 16) {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_number_invalid)
                return@setOnClickListener
            } else
                binding.tvCardError.viewGone()
            if (binding.etCardDate.text.toString().length < 5) {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_expiry_invalid)
                return@setOnClickListener
            } else
                binding.tvCardError.viewGone()
            if (binding.etCardDate.text.toString().trim().isNotEmpty()) {
                if (!DateConverter.checkIfCardDateIsValid(binding.etCardDate.text.toString().trim())) {
                    binding.tvCardError.viewVisible()
                    binding.tvCardError.text = mContext.resources.getString(R.string.card_expiry_invalid)
                    return@setOnClickListener
                } else
                    binding.tvCardError.viewGone()
            } else
                binding.tvCardError.viewGone()

            if (binding.etCardCVV.text.toString().isBlank()) {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_cvv_required)
                return@setOnClickListener
            } else
                binding.tvCardError.viewGone()
            if (binding.etCardHolderName.text.toString().isBlank()) {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_name_required)
                return@setOnClickListener
            }
            if (binding.etCardHolderSurname.text.toString().isBlank()) {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_surname_required)
                return@setOnClickListener
            } else
                binding.tvCardError.viewGone()

            val cardNumber = binding.etCardNumber.text.toString().trim()
            val cardNo = cardNumber.replace(" ", "")
            val cardMonth = binding.etCardDate.text.toString().trim().split("/").first().toInt()
            val cardYear = binding.etCardDate.text.toString().trim().split("/").last().toInt()
            val cardCVC = binding.etCardCVV.text.toString().trim()
            val cardToSave: Card = Card.create(
                cardNumber,
                cardMonth,
                cardYear,
                binding.etCardCVV.text.toString().trim()
            )

            if (cardToSave.validateCard() && cardToSave.validateNumber() &&
                cardToSave.validateCVC()
            ) {
                binding.tvCardError.viewGone()
                Loader.showLoader(mContext)
                generateToken(cardToSave)
            } else {
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_invalid)
            }

        }
    }

    private fun generateToken(
        cardToSave: Card,
    ) {
        stripe.createToken(cardToSave, object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                Log.e("Stripe", result.toString())
                Loader.hideLoader()
                dismiss()
                paymentCardInterface.addCard(result.id)
            }

            override fun onError(e: Exception) {
                Log.e("Stripe", e.toString())
                Loader.hideLoader()
                binding.tvCardError.viewVisible()
                binding.tvCardError.text = mContext.resources.getString(R.string.card_invalid)
            }
        })
    }


}