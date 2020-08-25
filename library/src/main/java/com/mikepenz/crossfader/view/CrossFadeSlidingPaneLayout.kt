package com.mikepenz.crossfader.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.slidingpanelayout.widget.SlidingPaneLayout

/**
 * SlidingPaneLayout that is partially visible, with cross fade.
 * https://github.com/chiuki/sliding-pane-layout
 */
open class CrossFadeSlidingPaneLayout : SlidingPaneLayout, ICrossFadeSlidingPaneLayout {
    private var partialView: View? = null
    private var fullView: View? = null

    // helper flag pre honeycomb used in visibility and click response handling
    // helps avoid unnecessary layouts
//    private val wasOpened = false
    private var mCanSlide = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount < 1) {
            return
        }
        val panel = getChildAt(0) as? ViewGroup ?: return
        if (panel.childCount != 2) {
            return
        }
        fullView = panel.getChildAt(0)
        partialView = panel.getChildAt(1)
        super.setPanelSlideListener(crossFadeListener)

        //make sure we prevent click on the fullView when we create the crossfader
        //just do this if we are not opened
        if (!isOpen) {
            enableDisableView(fullView, false)
        }
        fullView?.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (!isOpen) {
                enableDisableView(v, false)
            }
        }
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
        if (partialView != null) {
            partialView!!.visibility = if (isOpen) GONE else VISIBLE
        }
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

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mCanSlide && super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mCanSlide && super.onTouchEvent(ev)
    }

    override fun setCanSlide(canSlide: Boolean) {
        this.mCanSlide = canSlide
    }

    override fun setOffset(slideOffset: Float) {
        partialView!!.alpha = 1 - slideOffset
        fullView!!.alpha = slideOffset
        partialView!!.visibility = if (isOpen) GONE else VISIBLE

        //if the fullView is hidden we prevent the click on all its views and subviews
        //otherwhise enable it again
        if (slideOffset == 0f && fullView!!.isEnabled || slideOffset != 0f && !fullView!!.isEnabled) {
            enableDisableView(fullView, slideOffset != 0f)
        }
    }

    /**
     * helper method to disable a view and all its subviews
     *
     * @param view
     * @param enabled
     */
    private fun enableDisableView(view: View?, enabled: Boolean) {
        view!!.isEnabled = enabled
        view.isFocusable = enabled
        if (view is ViewGroup) {
            for (idx in 0 until view.childCount) {
                enableDisableView(view.getChildAt(idx), enabled)
            }
        }
    }
}