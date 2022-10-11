package com.dreamreco.firebaseappstreamtest.ui.popup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentPopUpBinding

class PopUpFragment : Fragment() {

    private val binding by lazy { FragmentPopUpBinding.inflate(layoutInflater) }
    private val args by navArgs<PopUpFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        with(binding) {
            title.text = args.diaryBaseAlpha.title
            content.text = args.diaryBaseAlpha.content
            myDrink.text = "${args.diaryBaseAlpha.drinkType}·${args.diaryBaseAlpha.drinkName}·${args.diaryBaseAlpha.POA}mL·${args.diaryBaseAlpha.VOD}%"
            mydate.text = "${args.diaryBaseAlpha.year}년${args.diaryBaseAlpha.month}월${args.diaryBaseAlpha.day}일"
            calendarDay.text = args.diaryBaseAlpha.calendarDay.toString()
            importance.text = args.diaryBaseAlpha.importance.toString()
            diaryBaseId.text = args.diaryBaseAlpha.id.toString()
            keywords.text = args.diaryBaseAlpha.keywords.toString()
            dateForSort.text = args.diaryBaseAlpha.dateForSort.toString()
        }

        return binding.root
    }

}