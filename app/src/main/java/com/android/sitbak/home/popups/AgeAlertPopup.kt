package com.android.sitbak.home.popups

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.DialogAgeAlertBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.utils.ConvertDateFormat
import com.astritveliu.boom.Boom
import java.util.*

@Suppress("DEPRECATION")
class AgeAlertPopup(val homeActivity: HomeActivity) : DialogFragment() {

    private lateinit var binding: DialogAgeAlertBinding
    private lateinit var mContext: Context

    private var myCalendar = Calendar.getInstance()

    private var dateOfBirth = ""
    private var isNineteenPlus = false

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
    ): View {
        // Inflate the layout for this fragment
        binding = DialogAgeAlertBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false

        listener()


        Boom(binding.btnCancel)
        Boom(binding.btnConfirm)
    }

    private fun listener() {

        binding.tvDOB.setOnClickListener {
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->


                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val dob = "$dayOfMonth-${month + 1}-$year"
                    binding.tvDOB.text = ConvertDateFormat(dob)
                    binding.tvDOB.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.lunar_green
                        )
                    )

                    val today = Calendar.getInstance()
                    var age: Int = today.get(Calendar.YEAR) - cal[Calendar.YEAR]

                    if (today.get(Calendar.DAY_OF_YEAR) < cal[Calendar.DAY_OF_YEAR]) {
                        age--
                    }

                    if (age > 19) {
                        binding.llConfirm.setBackgroundResource(R.drawable.bg_btn_main)
                        binding.btnConfirm.setTextColor(ContextCompat.getColor(mContext, R.color.white))
                        binding.btnConfirm.isEnabled = true

                        dateOfBirth = "$year-${month + 1}-$dayOfMonth"
                        isNineteenPlus = true
                    } else {
                        binding.llConfirm.setBackgroundResource(R.drawable.bg_tv_main_unselected)
                        binding.btnConfirm.setTextColor(ContextCompat.getColor(mContext, R.color.green_100))
                        binding.btnConfirm.isEnabled = false

                        isNineteenPlus = false
                    }

                }, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            dpd.datePicker.maxDate = myCalendar.timeInMillis
            dpd.show()
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            homeActivity.setNineteenPlus(dateOfBirth)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            homeActivity.closeApp()
        }
    }

}