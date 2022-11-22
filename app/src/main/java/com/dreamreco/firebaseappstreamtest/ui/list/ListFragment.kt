package com.dreamreco.firebaseappstreamtest.ui.list

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.firebaseappstreamtest.*
import com.dreamreco.firebaseappstreamtest.databinding.FragmentListBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ListFragment : Fragment(){

    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private var typeface: Typeface? = null
    lateinit var mAdapter: ListFragmentAdapter

    private lateinit var mFirebaseAnalytics : FirebaseAnalytics


    // 정렬 관련 변수
    private val sortNumber = MutableLiveData(SORT_NORMAL) // 기본 세팅

    // 검색 관련 코드
    private val adapterListClearCode = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 폰트 설정 및 적용 코드
        typeface = getFontType(requireContext())
        typeface?.let { setRecyclerView(it) }
        typeface?.let { setGlobalFont(binding.root, it) }

        mFirebaseAnalytics = Firebase.analytics
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(listViewModel) {

//             정렬 설정에 따라 가져오는 데이터
            sortNumber.observe(viewLifecycleOwner) { sortType ->
                when (sortType) {
                    SORT_NORMAL -> {
                        getAllDataDESC().observe(viewLifecycleOwner) {
                            if (sortNumber.value == SORT_NORMAL) {
                                makeList(it)
                                binding.sortTextView.text =
                                    getString(R.string.list_menu_sort_recent)
                            }
                        }
                    }
                    SORT_IMPORTANCE -> {
                        getDiaryDataImportant().observe(viewLifecycleOwner) {
                            if (sortNumber.value == SORT_IMPORTANCE) {
                                makeList(it)
                                binding.sortTextView.text =
                                    getString(R.string.list_menu_sort_important)
                            }
                        }
                    }
                }
            }

            listFragmentDiaryData.observe(viewLifecycleOwner) {
                mAdapter.submitList(it)
            }
        }

        // 1. 툴바 관련 코드
        with(binding.listFragmentToolbar) {

//            툴바 메뉴 터치가 왜 안되는 거지????
//            ???????????????????????????????????????????????????
//            직접 호출로 일단 진행

            with(binding.toolbarSearch) {
                setOnQueryTextListener(searchViewTextListener)
            }


            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.sort_by_recent -> {
                        sortNumber.postValue(SORT_NORMAL)
                        true
                    }
                    R.id.sort_by_importance -> {
                        sortNumber.postValue(SORT_IMPORTANCE)
                        true
                    }
                    else -> false
                }
            }
        }
        return binding.root
    }

    private var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != "") {
                    searchDatabase(newText)
                } else {
                    sortNumber.postValue(SORT_NORMAL)
                }
                return true
            }
        }

    private fun setRecyclerView(typeface: Typeface) {
        mAdapter = ListFragmentAdapter(requireContext(), typeface, getThemeType())
        with(binding) {
            with(listFragmentRecyclerView) {
                adapter = mAdapter
                setItemViewCacheSize(13)
            }

            // recyclerView 갱신 시, 깜빡임 방지
            val animator = listFragmentRecyclerView.itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
        }
    }

    private fun searchDatabase(query: String) {
        adapterListClearCode.value = query
        adapterListClearCode.observe(viewLifecycleOwner) { searchText ->
            listViewModel.getAllDiaryBase().observe(viewLifecycleOwner) { totalList ->
                listViewModel.filtering(searchText, totalList)
                binding.sortTextView.text = getString(R.string.list_menu_search)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val searchText = binding.toolbarSearch.query.toString()
        if (searchText != "") {
            searchDatabase(searchText)
        }

        val screenViewBundle = Bundle()
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "리스트화면")
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ListFragment")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenViewBundle)
    }

}