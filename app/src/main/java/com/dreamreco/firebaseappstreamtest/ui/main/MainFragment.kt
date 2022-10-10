package com.dreamreco.firebaseappstreamtest.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.toDateInt
import com.google.firebase.analytics.FirebaseAnalytics
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel by viewModels<MainViewModel>()
    private val randomStringList = arrayListOf<String>("가", "나", "다", "라", "마", "바", "사", "아")
    private lateinit var mFirebaseAnalytics : FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
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
        // TODO : MyData MyDrink 와 같은 것들만 영향을 받는 것으로 보임
        // TODO : DB 관련 모듈을 조정해서 원복할 수 없을까??

        with(binding) {
            btnToList.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToListFragment())
            }
            btnToOnly.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToOnlyFragment())
            }
            btnToAddList.setOnClickListener {
                listAdd()
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
        Log.e("메인조각", "fireBase 로그 전송")
    }


}