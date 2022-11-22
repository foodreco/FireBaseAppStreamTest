package com.dreamreco.firebaseappstreamtest.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

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
        // nav 세팅 완료됨

//        1.똑같이 데이터 애드 까지만 만들고
//        2.정상작동 확인하고,
//        3.Firebase 넣어서 작동 확인 (app 상태 및 플레이스토어 상태)
//        양호시 : 주량일기 애널리틱스 설정 살펴보기(설정 초기화 등)
//        오류시 : Room 생성전에 Firebase 먼저 설정해야 하는 것??

//        TODO : 자 이제 fireBase 에서 본격 테스트 하면 됨
//        1. git commit 완료하기
//        2. build 해서 내부테스트
//        3. firebase 넣어서 내부테스트
//        재연성 나오는지 확인
        // TODO : 테스트 결과 동일 현상 확인됨!!
        // TODO : MyData MyDrink 와 같은 converter 가 적용된 것들만 영향을 받는 것으로 보임
        // TODO : 단, CalendarDay 는 converter 적용임에도 이상없음
        // TODO : 직접 커스터마이징 한 data class 만 이상발생함
        // TODO : DB 관련 모듈을 조정해서 원복할 수 없을까?? -> 알 수 없음

        // #1. converter return null 직접대입 versionCode4 -> 효과없음
        // #2. converter Gson 부분 변경 versionCode5 -> 효과없음
        // #3. Migration + SDK 동시 적용 양호한지 확인

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