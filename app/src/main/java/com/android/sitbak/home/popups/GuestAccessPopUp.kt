package com.android.sitbak.home.popups

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.sitbak.databinding.DialogGuestAccessBinding
import com.android.sitbak.home.home.HomeActivity
import com.astritveliu.boom.Boom


class GuestAccessPopUp(val guestAccessInterface: GuestAccessInterface) : DialogFragment() {

    interface GuestAccessInterface {
        fun guestClickedLogin()
    }

    private lateinit var binding: DialogGuestAccessBinding

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.getWindowManager().getDefaultDisplay()
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
        binding = DialogGuestAccessBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        listener()
    }

    private fun listener() {
        Boom(binding.btnCreateAccount)
        Boom(binding.btnLogin)
        Boom(binding.btnContinueAsAGuest)

        binding.btnContinueAsAGuest.setOnClickListener {
            dismiss()
        }
        binding.btnCreateAccount.setOnClickListener {
            dismiss()
            guestAccessInterface.guestClickedLogin()
        }
        binding.btnLogin.setOnClickListener {
            dismiss()
            guestAccessInterface.guestClickedLogin()
        }
    }


}