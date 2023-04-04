package com.dreamreco.firebaseappstreamtest.util

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class MyCustomFragment : Fragment() {

    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }

    open var typeface: Typeface? = null
    open var themeType: String? = null
    open var imageViewColor = R.color.black

    override fun onCreate(savedInstanceState: Bundle?) {
        // 폰트 설정
        typeface = getFontType(requireContext())
        themeType = getThemeType()
        imageViewColor = when (getThemeType()) {
            THEME_2 -> R.color.white
            else -> R.color.black
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}