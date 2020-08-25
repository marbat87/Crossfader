package com.mikepenz.crossfader.app

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.mikepenz.crossfader.view.ICrossFadeSlidingPaneLayout

/**
 * SlidingPaneLayout that is partially visible, with cross fade.
 * https://github.com/chiuki/sliding-pane-layout
 */
@Suppress("unused")
class CustomCrossFadeSlidingPaneLayout : SlidingPaneLayout, ICrossFadeSlidingPaneLayout {
    private var partialView: View? = null
    private var fullView: View? = null

    // helper flag pre honeycomb used in visibility and click response handling
    // helps avoid unnecessary layouts
//    private var wasOpened = false
    private var mCanSlide = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount < 1) {
            return
        }
        val panel: View = getChildAt(0) as? ViewGroup ?: return
        val viewGroup = panel as ViewGroup
        if (viewGroup.childCount != 2) {
            return
        }
        fullView = viewGroup.getChildAt(0)
        partialView = viewGroup.getChildAt(1)
        super.setPanelSlideListener(crossFadeListener)
    }

    override fun setPanelSlideListener(listener: PanelSlideListener?) {
        if (listener == null) {
            super.setPanelSlideListener(crossFadeListener)
            return
        }

        super.setPanelSlideListener(object : PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                crossFadeListener.onPanelSlide(panel, slideOffset)
                listener.onPanelSlide(panel, slideOffset)
            }

            override fun onPanelOpened(panel: View) {
                listener.onPanelOpened(panel)
            }

            override fun onPanelClosed(panel: View) {
                listener.onPanelClosed(panel)
            }
        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        partialView?.visibility = if (isOpen) View.GONE else VISIBLE
    }

    private val crossFadeListener: SimplePanelSlideListener = object : SimplePanelSlideListener() {
        override fun onPanelSlide(panel: View, slideOffset: Float) {
            super.onPanelSlide(panel, slideOffset)
            if (partialView == null || fullView == null) {
                return
            }
            setOffset(slideOffset)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mCanSlide && super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return mCanSlide && super.onTouchEvent(ev)
    }

    override fun setCanSlide(canSlide: Boolean) {
        mCanSlide = canSlide
    }

    override fun setOffset(slideOffset: Float) {
        partialView?.alpha = 1 - slideOffset
        fullView?.alpha = slideOffset
        partialView?.visibility = if (isOpen) View.GONE else VISIBLE
    }

    private fun updateAlphaApi10(v: View?, value: Float) {
        val alpha = AlphaAnimation(value, value)
        alpha.duration = 0 // Make animation instant
        alpha.fillAfter = true // Tell it to persist after the animation ends
        v!!.startAnimation(alpha)
    }

    private fun updatePartialViewVisibilityPreHoneycomb(slidingPaneOpened: Boolean) {
        // below API 11 the top view must be moved so it does not consume clicks intended for the bottom view
        // this applies curiously even when setting its visibility to GONE
        // this might be due to platform limitations or it may have been introduced by NineOldAndroids library
        if (slidingPaneOpened) {
            partialView?.layout(-partialView!!.width, 0, 0, partialView!!.height)
        } else {
            partialView?.layout(0, 0, partialView!!.width, partialView!!.height)
        }
    }
}