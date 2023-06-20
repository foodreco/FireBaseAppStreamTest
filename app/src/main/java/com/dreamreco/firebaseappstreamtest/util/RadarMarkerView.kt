package com.dreamreco.firebaseappstreamtest.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.widget.TextView
import com.dreamreco.firebaseappstreamtest.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.DecimalFormat


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */

/** Radar 차트 터치 시  */
@SuppressLint("ViewConstructor")
class RadarMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    interface RadarMarkListener {
        fun onMarkTouch(value : Any)
    }

    private var radarMarkListener : RadarMarkListener? = null

    private val tvContent: TextView = findViewById<TextView>(R.id.tvContent)
    private val format = DecimalFormat("##0")

    init {
//        tvContent.typeface = Typeface.createFromAsset(context.assets, FONT_BASIC)
    }

    fun setRadarMarkListener(listener: RadarMarkListener) {
        this.radarMarkListener = listener
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    /** content 다시 그리는 코드 */
    override fun refreshContent(e: Entry, highlight: Highlight) {
        tvContent.text = String.format("%s %%", format.format(e.y.toDouble()))
        Log.e("RadarMarkerView", "e.data : ${e.data}\ne.x : ${e.x}")
        tvContent.setTextColor(Color.RED)
        tvContent.setTypeface(null, Typeface.BOLD_ITALIC)
        radarMarkListener?.onMarkTouch(e.data)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), (-height - 10).toFloat())
    }
}