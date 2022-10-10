package com.dreamreco.firebaseappstreamtest.ui.popup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentPopUpBinding

class PopUpFragment2 : Fragment() {

    private val binding by lazy { FragmentPopUpBinding.inflate(layoutInflater) }
    private val args by navArgs<PopUpFragment2Args>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        with(binding) {
            title.text = args.onlyBasic.title
            content.text = args.onlyBasic.content
            myDrink.text = "여기엔 없음"
            mydate.text = "여기엔 없음"
            calendarDay.text = "여기엔 없음"
            importance.text = args.onlyBasic.importance.toString()
            diaryBaseId.text = args.onlyBasic.id.toString()
            keywords.text = args.onlyBasic.keywords.toString()
            dateForSort.text = args.onlyBasic.dateForSort.toString()
        }

        return binding.root
    }

}