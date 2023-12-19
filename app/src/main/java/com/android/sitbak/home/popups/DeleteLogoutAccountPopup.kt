package com.android.sitbak.home.popups

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.DialogDeleteLogoutAccountBinding
import com.android.sitbak.home.profile.LogoutDeleteInterface
import com.android.sitbak.utils.viewGone
import com.android.sitbak.utils.viewInVisible
import com.android.sitbak.utils.viewVisible
import com.astritveliu.boom.Boom

class DeleteLogoutAccountPopup(val logoutDeleteInterface: LogoutDeleteInterface, val dialogCheck: Int) : DialogFragment() {

    private lateinit var binding: DialogDeleteLogoutAccountBinding
    private lateinit var mContext: Context

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DialogDeleteLogoutAccountBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = true

        initViews()
        listener()
    }

    private fun initViews() {
        checkForDialogType()

        Boom(binding.llDelete)
        Boom(binding.llLogOut)
        Boom(binding.llCancel)

    }

    private fun checkForDialogType() {
        /* Check for dialog to reuse class view
        * 0 - Delete account
        * 1 - Logout account
        * 2 - Delete/logout account
        * 3 - Delete Payment Card
        * */
        when (dialogCheck) {
            0 -> {
                binding.tvTitle.text = mContext.resources.getString(R.string.are_you_sure_you_want_to_delete_account)
                binding.tvDescription.text = mContext.resources.getString(R.string.delete_logout_account_description)
                binding.deleteCardConstraint.viewGone()
                binding.tvDescription.viewVisible()
                binding.llCancel.viewVisible()
                binding.llLogOut.viewGone()
                binding.llDelete.viewVisible()
            }
            1 -> {
                binding.tvTitle.text = mContext.resources.getString(R.string.are_you_sure_you_want_to_logout)
                binding.llLogOut.background = ContextCompat.getDrawable(mContext, R.drawable.bg_tv_main)
                binding.btnLogOut.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))
                binding.deleteCardConstraint.viewGone()
                binding.tvDescription.viewInVisible()
                binding.llCancel.viewVisible()
                binding.llLogOut.viewVisible()
                binding.llDelete.viewGone()
            }
            2 -> {
                binding.tvTitle.text = mContext.resources.getString(R.string.delete_logout)
                binding.deleteCardConstraint.viewGone()
                binding.tvDescription.viewInVisible()
                binding.llCancel.viewVisible()
                binding.llLogOut.viewVisible()
                binding.llDelete.viewVisible()
            }
            3 -> {
                binding.tvTitle.text = mContext.resources.getString(R.string.are_you_sure_you_want_to_delete_card)
                binding.tvDescription.viewGone()
                binding.deleteCardConstraint.viewVisible()
                binding.llCancel.viewVisible()
                binding.llLogOut.viewGone()
                binding.llDelete.viewVisible()
            }
        }
    }

    private fun listener() {
        binding.llCancel.setOnClickListener {
            dismiss()
        }

        binding.llLogOut.setOnClickListener {
            dismiss()
            logoutDeleteInterface.logoutAccount()
        }

        binding.llDelete.setOnClickListener {
            dismiss()
            when (dialogCheck) {
                0 -> {
                    /* Check for dialog to reuse class view
                        * 0 - Delete account
                        * 1 - Logout account
                        * 2 - Delete/logout account
                        * 3 - Delete Payment Card
                        * */
                    val dialog = DeleteLogoutAccountPopup(logoutDeleteInterface, 2)
                    dialog.show(activity?.supportFragmentManager!!, "DeleteLogoutAccountPopup")
                }
                2 -> {
                    logoutDeleteInterface.deleteAccount()
                }
                3 -> {
                    logoutDeleteInterface.deleteCard()
                }
            }
        }

    }


}