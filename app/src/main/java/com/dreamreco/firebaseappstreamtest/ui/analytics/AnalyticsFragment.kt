package com.dreamreco.firebaseappstreamtest.ui.analytics

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentAnalyticsBinding
import com.dreamreco.firebaseappstreamtest.util.actionEventLogToFireBase
import com.dreamreco.firebaseappstreamtest.util.actionEventLogToFireBaseTest
import com.dreamreco.firebaseappstreamtest.util.screenLogToFireBase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private val binding by lazy{FragmentAnalyticsBinding.inflate(layoutInflater)}
    private val analyticsViewModel by viewModels<AnalyticsViewModel>()

    companion object {
        const val TAG = "AnalyticsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            btnFirst.setOnClickListener {
                val text = "하나가"
                screenLogToFireBase(text,it.id.toString())
                actionEventLogToFireBase(text)
                actionEventLogToFireBaseTest(text)
            }

            btnSecond.setOnClickListener {
                val text = "둘이되고"
                screenLogToFireBase(text,it.id.toString())
                actionEventLogToFireBase(text)
                actionEventLogToFireBaseTest(text)
            }

            btnThird.setOnClickListener {
                val text = "둘이"
                screenLogToFireBase(text,it.id.toString())
                actionEventLogToFireBase(text)
                actionEventLogToFireBaseTest(text)
            }

            btnFourth.setOnClickListener {
                val text = "셋이된다."
                screenLogToFireBase(text,it.id.toString())
                actionEventLogToFireBase(text)
                actionEventLogToFireBaseTest(text)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        screenLogToFireBase("분석조각",TAG)
    }
}