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
            title.text = args.diaryBase.title
            content.text = args.diaryBase.content
            myDrink.text = args.diaryBase.myDrink.toString()
            mydate.text = args.diaryBase.date.toString()
            calendarDay.text = args.diaryBase.calendarDay.toString()
            importance.text = args.diaryBase.importance.toString()
            diaryBaseId.text = args.diaryBase.id.toString()
            keywords.text = args.diaryBase.keywords.toString()
            dateForSort.text = args.diaryBase.dateForSort.toString()
        }

        return binding.root
    }

}