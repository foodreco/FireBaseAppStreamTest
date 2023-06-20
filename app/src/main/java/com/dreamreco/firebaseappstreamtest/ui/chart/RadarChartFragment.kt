package com.dreamreco.firebaseappstreamtest.ui.chart

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.RadarChartBinding
import com.dreamreco.firebaseappstreamtest.util.RadarMarkerView
import com.dreamreco.firebaseappstreamtest.util.getFontType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import dagger.hilt.android.AndroidEntryPoint

import java.util.ArrayList

@AndroidEntryPoint
class RadarChartFragment : Fragment() {

    companion object {
        const val TAG = "RadarChartActivity"
    }

    private lateinit var chart: RadarChart
    private val binding by lazy { RadarChartBinding.inflate(layoutInflater) }
    private lateinit var tfLight : Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tfLight = getFontType(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        chart = binding.chart1.apply {
            //        chart.setBackgroundColor(Color.rgb(60, 65, 82))
            setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.basic_primary_background_color))

            description.isEnabled = false

            webLineWidth = 2f // 세로 라인 굵기
            webColor = Color.BLACK
            webLineWidthInner = 1f  // 가로 라인 굵기
            webColorInner = Color.BLACK
            webAlpha = 100
            isRotationEnabled = false // 회전 여부
        }


        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = RadarMarkerView(requireContext(), R.layout.radar_markerview)
        mv.setRadarMarkListener(object : RadarMarkerView.RadarMarkListener {
            override fun onMarkTouch(value: Any) {
                Log.e(TAG,"$TAG 수신 : $value")
            }
        })
        mv.chartView = chart // For bounds control
        chart.marker = mv // Set the marker to the chart

        setData()

        chart.animateXY(1400, 1400, Easing.EaseInOutQuad)

        val xAxis: XAxis = chart.xAxis
        xAxis.typeface = tfLight
        xAxis.textSize = 9f
        xAxis.yOffset = 0f
        xAxis.xOffset = 0f
        xAxis.valueFormatter = object : ValueFormatter() {
            private val mActivities = arrayOf("주량", "알콜", "기록수", "주종수","제품 수","빈도")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return mActivities[value.toInt() % mActivities.size]
            }


        }

        xAxis.textColor = Color.BLACK

        val yAxis: YAxis = chart.yAxis
        yAxis.typeface = tfLight
        yAxis.labelCount = 1 // 가로 줄 갯수
        yAxis.textSize = 12f // 글자 크기
        yAxis.axisMinimum = 0f // y 축 최소값
        yAxis.axisMaximum = 100f // y 축 최대값
        yAxis.setDrawLabels(false) // y 축 값 넣기

        val l: Legend = chart.legend.apply { isEnabled = false }

//        l.isEnabled = false
//        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//        l.orientation = Legend.LegendOrientation.HORIZONTAL
//        l.setDrawInside(false)
//        l.typeface = tfLight
//        l.xEntrySpace = 7f
//        l.yEntrySpace = 5f
//        l.textColor = Color.WHITE


        /** 회전 코드 */

        with(binding) {
            textViewAlcohol.setOnClickListener {
            }
            textViewRecord.setOnClickListener {
            }
            textViewTypes.setOnClickListener {
            }
            textViewVolume.setOnClickListener {
            }
        }

        return binding.root
    }

    private fun setData() {
        val mul = 90f
        val min = 20f
        val cnt = 6

        val entries1 = ArrayList<RadarEntry>()
        val entries2 = ArrayList<RadarEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until cnt) {
            val val1 = (Math.random() * mul).toFloat() + min
            entries1.add(RadarEntry(val1,i))
        }

        val set1 = RadarDataSet(entries1, "Last Week")
        set1.color = ContextCompat.getColor(requireContext(),R.color.joo_dark)
        set1.fillColor = ContextCompat.getColor(requireContext(),R.color.joo_main_color)
        set1.setDrawFilled(true) // 데이터 셋 영역 채우기
        set1.fillAlpha = 180 // 영역 채우기 알파값
        set1.lineWidth = 2f // 데이터 셋 선 두께
        set1.isDrawHighlightCircleEnabled = true // 데이터 셋 터치 표시 여부
        set1.highlightCircleStrokeColor = Color.RED // 데이터 셋 터치 시 동그라미 색상
        set1.setDrawHighlightIndicators(false) // 데이터 셋 터치 시 십자 강조 여부

//        val set2 = RadarDataSet(entries2, "This Week")
//        set2.color = Color.rgb(121, 162, 175)
//        set2.fillColor = Color.rgb(121, 162, 175)
//        set2.setDrawFilled(true)
//        set2.fillAlpha = 180
//        set2.lineWidth = 2f
//        set2.isDrawHighlightCircleEnabled = true
//        set2.setDrawHighlightIndicators(false)

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set1)
//        sets.add(set2)

        val data = RadarData(sets)
        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setDrawValues(false) // 데이터 값 텍스트
        data.setValueTextColor(Color.RED) // 데이터 값 색상

        chart.data = data
        chart.invalidate()
    }

}
