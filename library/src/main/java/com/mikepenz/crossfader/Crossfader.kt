package com.mikepenz.crossfader

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.slidingpanelayout.widget.SlidingPaneLayout.PanelSlideListener
import com.mikepenz.crossfader.view.ICrossFadeSlidingPaneLayout

/**
 * Created by mikepenz on 15.07.15.
 */
@Suppress("unused")
class Crossfader<T> where T : SlidingPaneLayout, T : ICrossFadeSlidingPaneLayout {
    lateinit var crossFadeSlidingPaneLayout: T
        private set
    private var mBaseLayout = R.layout.crossfader_base

    /**
     * defines the base layout to be used for this crossfader
     * look at the sample definition of the crossfader_base
     *
     * @param baseLayout
     * @return
     */
    fun withBaseLayout(@LayoutRes baseLayout: Int): Crossfader<*> {
        mBaseLayout = baseLayout
        return this
    }

    /**
     * Gmail android app (as of version autumn 2015) has custom swiping:
     * when drawer is closed, only left pane is swipeable (content swiping is blocked)
     * when drawer is opened, all swiping gestures goes to crossfader
     *
     * @return
     */
    fun withGmailStyleSwiping(): Crossfader<*> {
        mBaseLayout = R.layout.crossfader_gmail_style
        return this
    }

    /**
     * @return the content view
     */
    lateinit var content: View
        private set

    /**
     * define the content which is shown on the right of the crossfader
     *
     * @param content
     * @return
     */
    fun withContent(content: View): Crossfader<*> {
        this.content = content
        return this
    }

    /**
     * @return the first (default) view
     */
    private var first: View? = null

    /**
     * @return the width of the first (default) view
     */
    var firstWidth = -1
        private set

    /**
     * define the default (first) view of the crossfader
     *
     * @param first
     * @param width
     * @return
     */
    fun withFirst(first: View?, width: Int): Crossfader<*> {
        this.first = first
        firstWidth = width
        return this
    }

    /**
     * @return the second (slided) view
     */
    private var second: View? = null

    /**
     * @return the width of the second (slided) view
     */
    var secondWidth = -1
        private set

    /**
     * define the slided (second) view of the crossfader
     *
     * @param first
     * @param width
     * @return
     */
    fun withSecond(first: View?, width: Int): Crossfader<*> {
        second = first
        secondWidth = width
        return this
    }

    /**
     * define the default view and the slided view of the crossfader
     *
     * @param first
     * @param firstWidth
     * @param second
     * @param secondWidth
     * @return
     */
    fun withStructure(first: View?, firstWidth: Int, second: View?, secondWidth: Int): Crossfader<*> {
        withFirst(first, firstWidth)
        withSecond(second, secondWidth)
        return this
    }

    // savedInstance to restore state
    private var mSavedInstance: Bundle? = null

    /**
     * Set the Bundle (savedInstance) which is passed by the activity.
     * No need to null-check everything is handled automatically
     *
     * @param savedInstance
     * @return
     */
    fun withSavedInstance(savedInstance: Bundle?): Crossfader<*> {
        mSavedInstance = savedInstance
        return this
    }

    // enable the panelSlide
    private var mCanSlide = true

    /**
     * Allow the panel to slide
     *
     * @param canSlide
     * @return
     */
    fun withCanSlide(canSlide: Boolean): Crossfader<*> {
        mCanSlide = canSlide
        crossFadeSlidingPaneLayout.setCanSlide(mCanSlide)
        return this
    }

    //a panelSlideListener
    private var mPanelSlideListener: PanelSlideListener? = null

    /**
     * set a PanelSlideListener used with the CrossFadeSlidingPaneLayout
     *
     * @param panelSlideListener
     * @return
     */
    fun withPanelSlideListener(panelSlideListener: PanelSlideListener?): Crossfader<*> {
        mPanelSlideListener = panelSlideListener
        crossFadeSlidingPaneLayout.setPanelSlideListener(mPanelSlideListener)
        return this
    }

    // if enabled we use a PanelSlideListener to resize the content panel instead of moving it out
    private var mResizeContentPanel = false

    /**
     * if enabled we use a PanelSlideListener to resize the content panel instead of moving it out
     *
     * @param resizeContentPanel
     * @return
     */
    fun withResizeContentPanel(resizeContentPanel: Boolean): Crossfader<*> {
        mResizeContentPanel = resizeContentPanel
        enableResizeContentPanel(mResizeContentPanel)
        return this
    }

    /**
     * a small helper class to enable resizing of the content panel / or keep the default behavior
     */
    private fun enableResizeContentPanel(enable: Boolean) {
        if (enable) {
            //activate the resizeFunction
            val displaymetrics = content.context.resources.displayMetrics
            val screenWidth = displaymetrics.widthPixels
            val lp = content.layoutParams
            lp.width = screenWidth - secondWidth
            content.layoutParams = lp
            crossFadeSlidingPaneLayout.setPanelSlideListener(object : PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) {
                    val lp1 = content.layoutParams
                    lp1?.width = (screenWidth - secondWidth - (firstWidth - secondWidth) * slideOffset).toInt()
                    content.layoutParams = lp1
                    if (mPanelSlideListener != null) {
                        mPanelSlideListener!!.onPanelSlide(panel, slideOffset)
                    }
                }

                override fun onPanelOpened(panel: View) {
                    if (mPanelSlideListener != null) {
                        mPanelSlideListener!!.onPanelOpened(panel)
                    }
                }

                override fun onPanelClosed(panel: View) {
                    if (mPanelSlideListener != null) {
                        mPanelSlideListener!!.onPanelClosed(panel)
                    }
                }
            })
        } else {
            //reset the resizeFunction
            val lp = content.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            content.layoutParams = lp
            crossFadeSlidingPaneLayout.setPanelSlideListener(mPanelSlideListener)
        }
    }

    /**
     * builds the crossfader and it's content views
     * will define all properties and define and add the layouts
     *
     * @return
     */
    fun build(): Crossfader<*> {
        if (firstWidth < secondWidth) {
            throw RuntimeException("the first layout has to be the layout with the greater width")
        }

        //get the layout which should be replaced by the CrossFadeSlidingPaneLayout
        val container = content.parent as ViewGroup

        //remove the content from it's parent
        container.removeView(content)

        //create the cross fader container
        @Suppress("UNCHECKED_CAST")
        crossFadeSlidingPaneLayout = LayoutInflater.from(content.context).inflate(mBaseLayout, container, false) as T
        container.addView(crossFadeSlidingPaneLayout)

        //find the container layouts
        val mCrossFadePanel = crossFadeSlidingPaneLayout.findViewById<FrameLayout>(R.id.panel)
        val mCrossFadeFirst = crossFadeSlidingPaneLayout.findViewById<LinearLayout>(R.id.first)
        val mCrossFadeSecond = crossFadeSlidingPaneLayout.findViewById<LinearLayout>(R.id.second)
        val mCrossFadeContainer = crossFadeSlidingPaneLayout.findViewById<LinearLayout>(R.id.content)

        //define the widths
        setWidth(mCrossFadePanel, firstWidth)
        setWidth(mCrossFadeFirst, firstWidth)
        setWidth(mCrossFadeSecond, secondWidth)
        setLeftMargin(mCrossFadeContainer, secondWidth)

        //add content to the panel
        mCrossFadeFirst.addView(first, firstWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        mCrossFadeSecond.addView(second, secondWidth, ViewGroup.LayoutParams.MATCH_PARENT)

        //add back main content
        mCrossFadeContainer.addView(content, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // try to restore all saved values again
        var crossFaded = false
        if (mSavedInstance != null) {
            crossFaded = mSavedInstance!!.getBoolean(BUNDLE_CROSS_FADED, false)
        }
        if (crossFaded) {
            crossFadeSlidingPaneLayout.setOffset(1f)
        } else {
            crossFadeSlidingPaneLayout.setOffset(0f)
        }

        //set the PanelSlideListener for the CrossFadeSlidingPaneLayout
        crossFadeSlidingPaneLayout.setPanelSlideListener(mPanelSlideListener)

        //set the ability to slide
        crossFadeSlidingPaneLayout.setCanSlide(mCanSlide)

        //define that we don't want a slider color
        crossFadeSlidingPaneLayout.sliderFadeColor = Color.TRANSPARENT

        //enable / disable the resize functionality
        enableResizeContentPanel(mResizeContentPanel)
        return this
    }

    /**
     * returns if the crossfader is currently opened (the second view is shown)
     *
     * @return
     */
    val isCrossFaded: Boolean
        get() = crossFadeSlidingPaneLayout.isOpen

    /**
     * crossfade the current crossfader (toggle between first and second view)
     */
    fun crossFade() {
        if (crossFadeSlidingPaneLayout.isOpen) {
            crossFadeSlidingPaneLayout.closePane()
        } else {
            crossFadeSlidingPaneLayout.openPane()
        }
    }

    /**
     * add the values to the bundle for saveInstanceState
     *
     * @param savedInstanceState
     * @return
     */
    fun saveInstanceState(savedInstanceState: Bundle?): Bundle? {
        savedInstanceState?.putBoolean(BUNDLE_CROSS_FADED, crossFadeSlidingPaneLayout.isOpen)
        return savedInstanceState
    }

    /**
     * define the width of the given view
     *
     * @param view
     * @param width
     */
    private fun setWidth(view: View, width: Int) {
        val lp = view.layoutParams
        lp.width = width
        view.layoutParams = lp
    }

    /**
     * define the left margin of the given view
     *
     * @param view
     * @param leftMargin
     */
    private fun setLeftMargin(view: View, leftMargin: Int) {
        val lp = view.layoutParams as SlidingPaneLayout.LayoutParams
        lp.leftMargin = leftMargin
        lp.rightMargin = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.marginStart = leftMargin
            lp.marginEnd = 0
        }
        view.layoutParams = lp
    }

    companion object {
        /**
         * BUNDLE param to store the selection
         */
        private const val BUNDLE_CROSS_FADED = "bundle_cross_faded"
    }
}