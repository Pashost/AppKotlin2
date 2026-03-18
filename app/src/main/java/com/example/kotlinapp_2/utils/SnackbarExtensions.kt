package com.example.kotlinapp_2.utils

import android.view.View
import androidx.core.content.ContextCompat
import com.example.kotlinapp_2.R
import com.google.android.material.snackbar.Snackbar

fun View.showModernSnackbar(
    message: String,
    actionText: String? = null,
    onActionClicked: (() -> Unit)? = null,
) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

    snackbar.view.apply {
        background = ContextCompat.getDrawable(context, R.drawable.bg_modern_snackbar)
        elevation = 8f
        setPadding(24, 12, 24, 12)
    }

    snackbar.setTextColor(ContextCompat.getColor(context, R.color.md_theme_on_primary))
    snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.md_theme_accent))

    if (!actionText.isNullOrBlank() && onActionClicked != null) {
        snackbar.setAction(actionText) { onActionClicked.invoke() }
    }

    snackbar.show()
}
