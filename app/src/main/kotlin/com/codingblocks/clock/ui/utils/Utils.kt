package com.codingblocks.clock.ui.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codingblocks.clock.R
import com.codingblocks.clock.core.decodeBase64ToBitmap

fun loadBase64WithGlide(context: Context, base64String: String, imageView: ImageView) {
    val bitmap = decodeBase64ToBitmap(base64String)
    if (bitmap != null) {
        Glide.with(context)
            .load(bitmap)
            .apply(RequestOptions().override(32, 32))
            .into(imageView)
    } else {
        Glide.with(context)
            .load(R.drawable.ic_placeholder)
            .into(imageView)
    }
}