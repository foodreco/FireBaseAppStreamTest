package com.dreamreco.firebaseappstreamtest.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentListBinding
import dagger.hilt.android.AndroidEntryPoint


//TODO : 긴 술 이름 창에 다 보일 수 있게 개선
@AndroidEntryPoint
abstract class MyCustomDialogFragment : DialogFragment() {

    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }

    open var typeface: Typeface? = null
    open var themeType: String? = null
    open var imageViewColor = R.color.black

    // Dialog 테마 설정
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 폰트 설정
        typeface = getFontType(requireContext())
        themeType = getThemeType()
        imageViewColor = when (getThemeType()) {
            THEME_2 -> R.color.white
            else -> R.color.black
        }

        // 배경 색 설정
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        when (themeType) {
//            THEME_2 -> dialog.window?.setBackgroundDrawableResource(R.color.theme2_primary_background_color)
//            else -> dialog.window?.setBackgroundDrawableResource(R.color.basic_primary_background_color)
//        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onResume() {
        val deviceWidth = getScreenWidth(requireContext())
        val deviceHeight = getScreenHeight(requireContext())
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (deviceWidth * 0.8).toInt()
        params?.height = (deviceHeight * 0.8).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        super.onResume()
    }
}