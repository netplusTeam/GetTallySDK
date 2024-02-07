package com.netplus.qrengine.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class TallyCustomProgressDialog(context: Context) : Dialog(context) {

    //private val loadingTextView: TextView
    private val updateTextView: TextView

    init {
        // Set the dialog style
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)
        setCancelable(false) // To prevent users from dismissing dialog by clicking outside

        // Create a LinearLayout to hold the ProgressBar, TextViews
        val linearLayout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER

        // Create the ProgressBar and add it to the LinearLayout
        val progressBar = ProgressBar(context)
        linearLayout.addView(progressBar)

        // Create the TextView for loading text
        /*loadingTextView = TextView(context)
        loadingTextView.text = loadingText
        linearLayout.addView(loadingTextView)*/

        // Create the TextView for dynamic updates
        updateTextView = TextView(context)
        updateTextView.textSize = 20f
        linearLayout.addView(updateTextView)

        // Add the LinearLayout to the dialog
        setContentView(linearLayout)
    }

    fun setUpdateText(updateText: String) {
        updateTextView.text = updateText
    }
}