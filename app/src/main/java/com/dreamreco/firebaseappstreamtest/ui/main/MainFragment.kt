package com.dreamreco.firebaseappstreamtest.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }


    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel by viewModels<MainViewModel>()
    private val randomStringList = arrayListOf<String>("가", "나", "다", "라", "마", "바", "사", "아")
    private lateinit var mFirebaseAnalytics : FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        mFirebaseAnalytics = Firebase.analytics
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            btnToList.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "btnToList")
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "btnToList")
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "리스트로이동")
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToListFragment())
            }
            btnToOnly.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "btnToOnly")
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "btnToOnly")
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "온니로이동")
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToOnlyFragment())
            }
            btnToAddList.setOnClickListener {
                listAdd()
            }


            btnCustom1.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("이름", "버튼")
                bundle.putString("장소", "메인조각")
                bundle.putString("이벤트", "btnCustom1")
                mFirebaseAnalytics.logEvent("myCustomEvent", bundle)
            }
            btnCustom2.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("이름", "버튼")
                bundle.putString("장소", "메인조각")
                bundle.putString("이벤트", "btnCustom2")
                mFirebaseAnalytics.logEvent("myCustomEvent", bundle)
            }
            btnCustom3.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("이름", "버튼")
                bundle.putString("장소", "메인조각")
                bundle.putString("이벤트", "btnCustom3")
                mFirebaseAnalytics.logEvent("myCustomEvent", bundle)
            }

            // Firebase Message 토큰을 불러오는 코드
            btnGetToken.setOnClickListener {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    Log.d(TAG, "토큰값 : $token")
                    Toast.makeText(requireContext(), token , Toast.LENGTH_SHORT).show()
                    binding.textToken.setText(token)
                })
            }
        }

        return binding.root
    }

    // 리스트 신규 추가 코드
    private fun listAdd() {
        mainViewModel.insertDiaryBase()
        mainViewModel.insertOnlyBasic()
        sendFirebaseLog("MainFragment","listAdd","리스트추가")
    }

    /** FireBase 에 로그를 보내는 함수 (데이터 저장 또는 업데이트 시 발동) */
    private fun sendFirebaseLog(itemID: String, itemName: String, contentType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemID)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

        Log.e("메인조각", "fireBase 로그 전송")
    }

    override fun onResume() {
        super.onResume()
        val screenViewBundle = Bundle()
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "메인화면")
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainFragment")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenViewBundle)
    }
}