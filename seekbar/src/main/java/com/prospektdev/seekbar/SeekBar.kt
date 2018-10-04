package com.prospektdev.seekbar

import android.content.Context
import android.support.annotation.ColorRes
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.util.SparseArrayCompat
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import android.widget.TextView


class SeekBar : ConstraintLayout {
    var eventListener: ((Int) -> Unit)? = null
    var seekElements: ArrayList<String>? = null
        set(value) {
            field = value
            redrawElements()
        }
    var progress: Int = 0
        set(value) {
            field = value
            seekBar?.progress = field
        }
    private var seekBar: AppCompatSeekBar? = null
    private var lastSelectedId: Int? = null
    private val idMap = SparseArrayCompat<Int>()

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        redrawElements()
    }

    fun getProgressString(): String = seekElements!![progress]

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekBar, 0, 0)
            val arrayElements = array.getTextArray(R.styleable.SeekBar_arrayOfItems)
            seekElements = arrayListOf()
            arrayElements.forEach {
                seekElements!!.add(it.toString())
            }
            progress = array.getInt(R.styleable.SeekBar_defaultIndexOfValue, 0)
            array.recycle()
        } else {
            progress = 0
        }
    }

    private fun redrawElements() {
        if (seekElements == null) {
            return
        }
        idMap.clear()
        removeAllViews()
        seekBar = AppCompatSeekBar(ContextThemeWrapper(context, R.style.MySeekBar), null, 0)
        seekBar!!.id = View.generateViewId()
        val seekBarParams = ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        seekBarParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        seekBarParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        seekBarParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        seekBar!!.max = seekElements!!.size - 1
        seekBar!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                seekBar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                drawElements()
                seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setTextViewStyle(0, R.color.primary_dark_material_light, R.font.montserratregular)
                        val id = idMap[progress]
                        lastSelectedId = id
                        setTextViewStyle(convertDpToPixel(8f, context), R.color.colorBlue, R.font.montserratextrabold)
                        eventListener?.invoke(progress)
                        this@SeekBar.progress = progress
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                    }
                })
                seekBar!!.progress = progress
            }
        })
        addView(seekBar, seekBarParams)
    }

    private fun setTextViewStyle(margin: Int, @ColorRes color: Int, style: Int) {
        if (lastSelectedId != null) {
            val textView = findViewById<TextView>(lastSelectedId!!)
            val textViewParams = textView.layoutParams as ConstraintLayout.LayoutParams
            textViewParams.bottomMargin = margin
            textView.setTextColor(ContextCompat.getColor(context, color))
            val typeface = ResourcesCompat.getFont(context, style)
            textView.typeface = typeface
            textView.layoutParams = textViewParams
        }
    }

    private fun drawElements() {
        seekElements!!.forEachIndexed { index, element ->
            val x = (seekBar!!.width - thumbWidth()) / (seekElements!!.size - 1)
            val textView = TextView(context)
            textView.text = element
            textView.id = View.generateViewId()
            idMap.put(index, textView.id)
            val textViewParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT)
            textViewParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            textViewParams.bottomToTop = seekBar!!.id
            textViewParams.startToStart = seekBar!!.id
            textView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    textViewParams.marginStart = (x * index) + (thumbWidth() / 2) - (textView.width / 2)
                    textView.layoutParams = textViewParams
                }
            })
            addView(textView, textViewParams)
        }
    }

    private fun thumbWidth() = seekBar!!.paddingStart + seekBar!!.paddingEnd

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}
