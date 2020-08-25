package com.mikepenz.crossfader.app

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader.init
import com.squareup.picasso.Picasso

/**
 * Created by mikepenz on 27.03.15.
 */
@Suppress("unused")
class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //initialize and create the image loader logic
        init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Picasso.get().cancelRequest(imageView)
            }
        })
    }
}