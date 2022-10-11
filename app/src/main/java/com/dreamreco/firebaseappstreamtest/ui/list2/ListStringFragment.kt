package com.dreamreco.firebaseappstreamtest.ui.list2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentListStringBinding
import com.dreamreco.firebaseappstreamtest.databinding.FragmentOnlyBinding
import com.dreamreco.firebaseappstreamtest.ui.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListStringFragment : Fragment() {

    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentListStringBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(listViewModel) {
            getMyKeywordData().observe(viewLifecycleOwner) { keywordRoomLive ->
                if (keywordRoomLive == null) {
                    binding.listStringText.text = "null 을 반환함"
                } else {
                    binding.listStringText.text = keywordRoomLive.keywords.toString()
                }
            }
        }

        return binding.root
    }

}