package com.mikepenz.crossfader.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.mikepenz.crossfader.R
import com.mikepenz.crossfader.util.UIUtils

/**
 * Created on 05.11.15
 *
 * @author github @androideveloper (Roland Yeghiazaryan)
 * @author github @suren1525 (Suren Khachatryan)
 */
class GmailStyleCrossFadeSlidingPaneLayout : CrossFadeSlidingPaneLayout {
    private var isEventHandled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isOutOfSecond(ev)) {
            false
        } else super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (isOutOfSecond(ev)) {
            false
        } else super.onTouchEvent(ev)
    }

    private fun isOutOfSecond(ev: MotionEvent): Boolean {
        if (MotionEvent.ACTION_UP == ev.action || MotionEvent.ACTION_CANCEL == ev.action) {
            isEventHandled = false
        }
        val mCrossFadeSecond = findViewById<View>(R.id.second) as LinearLayout
        if (!isOpen && ev.action == MotionEvent.ACTION_DOWN && !UIUtils.isPointInsideView(ev.rawX, ev.rawY, mCrossFadeSecond) || isEventHandled) {
            isEventHandled = true
            return true
        }
        return false
    }
}