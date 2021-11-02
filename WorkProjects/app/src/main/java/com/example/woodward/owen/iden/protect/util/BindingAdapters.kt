package com.example.woodward.owen.iden.protect.util

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("text")
fun setStringInTv(tv: TextView, strVal: String?) {
    try {
        if (strVal == null && (tv.text.toString() == "" || tv.text.toString() == ".")) return

        if (tv.text.toString() == strVal) return else tv.text = strVal
    } catch (nfe: java.lang.NumberFormatException) {
        tv.text = strVal ?: ""
    }
}
