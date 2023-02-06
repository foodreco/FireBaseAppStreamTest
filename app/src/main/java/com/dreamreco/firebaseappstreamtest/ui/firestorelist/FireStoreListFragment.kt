package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.util.WineUtil
import com.dreamreco.firebaseappstreamtest.databinding.FragmentFireStoreListBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FireStoreListFragment : Fragment(), WineAdapter.OnRestaurantSelectedListener, FilterDialogFragment.FilterListener {

    //TODO : 리스너를 이용한 DialogFragment 필터 만들어보기
    //TODO : 검색으로 관련 데이터 도출해보기
    //TODO : FireStore ↔ Storage 연계하기 : 특정 FireStore 관련 이미지 Storage 에서 불러오기

    private val binding by lazy { FragmentFireStoreListBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<FireStoreListViewModel>()
    private var mAdapter: WineAdapter? = null

    // FireStore 관련
    private lateinit var firestore: FirebaseFirestore
    private var query: Query? = null

    // 필터링 관련
    private lateinit var filterDialog: FilterDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 50개 highest rated 와인 목록 얻기
        // 기본 정렬 avgRating 순으로
        query = firestore.collection("wine")
            .orderBy("name",Query.Direction.ASCENDING)
            .limit(LIMIT.toLong())

        query?.let {
            Log.e(TAG,"쿼리 불러옴 : ${it}")
            mAdapter = object : WineAdapter(it, this@FireStoreListFragment) {
                override fun onError(e: FirebaseFirestoreException) {
                    Snackbar.make(
                        binding.root,
                        "에러발생 : $e.", Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun onDataChanged() {
                    if (itemCount == 0) {
                        Toast.makeText(requireContext(),"리스트가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.fireStoreRecyclerView.adapter = mAdapter

        binding.btnToAddFireStoreList.setOnClickListener {
            onAddItemsClicked()
        }

        filterDialog = FilterDialogFragment()

        binding.btnToFilter.setOnClickListener { onFilterClicked() }
        binding.btnToFilterCancel.setOnClickListener { onClearFilterClicked() }


        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Start listening for Firestore updates
        mAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()

        mAdapter?.stopListening()
    }

    companion object {
        private const val TAG = "FireStoreListFragment"
        private const val LIMIT = 50
    }

    private fun onFilterClicked() {
        // Show the dialog containing filter options
        filterDialog.show(childFragmentManager, FilterDialogFragment.TAG)
    }

    private fun onClearFilterClicked() {
        filterDialog.resetFilters()

        onFilter(Filters.default)
    }

    override fun onRestaurantSelected(wine: CustomDrink) {
        Toast.makeText(requireContext(),"선택됨:${wine}",Toast.LENGTH_SHORT).show()
    }

    private fun onAddItemsClicked() {
        val wineRef = firestore.collection("wine")
        for (i in 0..3) {
            // Create random restaurant / ratings
            val randomWine = WineUtil.getRandom(requireContext())

            Log.e(TAG,"추가와인 : $randomWine")

            // Add restaurant
            wineRef.add(randomWine)
        }
        Toast.makeText(requireContext(),"DB 리스트 추가됨",Toast.LENGTH_SHORT).show()
    }

    override fun onFilter(filters: Filters) {
        var query : Query = firestore.collection("wine")

        // 카테고리
        if (filters.hasCategory()) {
            query = query.whereEqualTo(Wine.FIELD_CATEGORY, filters.category)
        }

        // 도시
        if (filters.hasCity()) {
            query = query.whereEqualTo(Wine.FIELD_COUNTRY, filters.country)
        }

        // 가격
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Wine.FIELD_PRICE, filters.price)
        }

        // 방향??
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.sortBy.toString(), filters.sortDirection)
        }

        // Limit items (50개 까지 나타냄)
        query = query.limit(LIMIT.toLong())

        // 쿼리 반영하기
        mAdapter?.setQuery(query)

        // 헤더 설정
        binding.textCurrentSearch.text = HtmlCompat.fromHtml(
            filters.getSearchDescription(requireContext()),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.textCurrentSortBy.text = filters.getOrderDescription(requireContext())

        // 필터 저장
        viewModel.filters = filters
    }
}