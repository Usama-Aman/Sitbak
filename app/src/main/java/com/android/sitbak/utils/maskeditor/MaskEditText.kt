package com.android.sitbak.utils.maskeditor

import android.content.Context
import android.util.AttributeSet
import com.android.sitbak.R
import com.android.sitbak.utils.maskeditor.MaskTextWatcher
import com.google.android.material.textfield.TextInputEditText


class MaskEditText : TextInputEditText {

    private var maskTextWatcher: MaskTextWatcher? = null

    private var mask: String? = null
        set(value) {
            field = value
            if (value.isNullOrEmpty()) {
                removeTextChangedListener(maskTextWatcher)
            }
            else {
                maskTextWatcher = MaskTextWatcher(this, mask!!)
                addTextChangedListener(maskTextWatcher)
            }
        }

    val rawText: String?
        get() {
            val formatted = text
            return maskTextWatcher?.unFormat(formatted) ?: formatted.toString()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.MaskEditText)
            with(a) {
                mask = getString(R.styleable.MaskEditText_met_mask)
                recycle()
            }
        }
    }
}