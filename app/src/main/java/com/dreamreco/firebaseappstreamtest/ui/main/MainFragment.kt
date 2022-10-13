package com.dreamreco.firebaseappstreamtest.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel by viewModels<MainViewModel>()
    private val randomStringList = arrayListOf<String>("가", "나", "다", "라", "마", "바", "사", "아")

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

//        fireBase 에서 본격 테스트 -> 실패
//        1. git commit 완료하기
//        2. build 해서 내부테스트
//        3. firebase 넣어서 내부테스트
//        재연성 나오는지 확인


        //TODO : 아래 작업 진행
//        1. 룸 migration 테스트 -> 다 기초로...
//        1)통째로 entity 추가해서 이전작업
//        2)업데이트하면서 기존 것 삭제 -> 이전본 main 화
//        3)SDK 추가
//
//        2. List<String> 은 이상없는지 column 추가해서 확인
//        3. migration 후 -> SDK 적용 이상없는지 확인




        with(binding) {
            btnToList.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToListFragment())
            }
            btnToOnly.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToOnlyFragment())
            }
            btnToListString.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToListStringFragment())
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
        mainViewModel.makeKeywordList()
    }


}