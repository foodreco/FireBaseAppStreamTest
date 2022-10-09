package com.dreamreco.firebaseappstreamtest.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import com.google.firebase.analytics.FirebaseAnalytics

class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var mFirebaseAnalytics : FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.btnEvent1.setOnClickListener {
            activateFirebase()
        }

        // nav 세팅 완료됨
        return binding.root
    }

    private fun activateFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "테스트아이디")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "보내는이름")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "이건타입")

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        Toast.makeText(requireContext(), "이벤트 전송함", Toast.LENGTH_SHORT).show()
    }
}