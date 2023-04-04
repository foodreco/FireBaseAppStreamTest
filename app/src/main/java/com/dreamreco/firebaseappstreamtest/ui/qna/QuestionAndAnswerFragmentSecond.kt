package com.dreamreco.firebaseappstreamtest.ui.qna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentQuestionAndAnswerBinding
import com.dreamreco.firebaseappstreamtest.repository.FireBaseRepository
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Product
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.Operation
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.ProductListLiveData
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ArrayList


@AndroidEntryPoint
class QuestionAndAnswerFragmentSecond : Fragment(), com.google.firebase.firestore.EventListener<QuerySnapshot> {

    companion object {
        private const val TAG = "QuestionAndAnswerFragmentSecond"
    }

    private val binding by lazy { FragmentQuestionAndAnswerBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<QuestionAndAnswerViewModel>()

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    /** E + ListAdapter 관련 */
    private var boardSecondAdapter: BoardSecondAdapter? = null
    private val snapshots = ArrayList<QuestionAndAnswerClass>()
    private var registration: ListenerRegistration? = null
    private var isScrolling = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        viewModel.totalCountForBoard()

        Log.e(TAG,"깔끔한 onCreate 작동")
        initBoardSecondAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e(TAG,"onCreateView 작동")

        setBoardSecondAdapterList()

        with(binding) {
            btnWriteQuestion.setOnClickListener {
                if (user != null) {
                    Log.e(TAG, "질문 작성하기")
                    it.findNavController().navigate(
                        QuestionAndAnswerFragmentDirections.actionQuestionAndAnswerFragmentToQuestionAndAnswerWriteFragment()
                    )
                } else {
                    loginSuggest()
                }
            }

            btnSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Log.e(TAG, "내 질문만 보기")
                } else {
                    Log.e(TAG, "전체 질문 보기")
                }
            }
        }
        return binding.root
    }

    /** EventListener + ListAdapter 방식 */
    private fun initBoardSecondAdapter() {
        boardSecondAdapter = BoardSecondAdapter()
        boardSecondAdapter!!.setProductsAdapterListener(object :
            BoardSecondAdapter.OnBoardSelectedListener {
            override fun onBoardSelected(content: QuestionAndAnswerClass) {
                with(content) {
                    Log.e(
                        TAG,
                        "선택된 item\n제목 : $title / 내용 : $content / timeStamp : ${content.timestamp} / uid : $uid"
                    )}
            }
        })
        binding.recyclerViewForBoard.adapter = boardSecondAdapter
        val animator = binding.recyclerViewForBoard.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        val onScrollListener: RecyclerView.OnScrollListener =
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (layoutManager != null) {
                        val firstVisibleProductPosition =
                            layoutManager.findFirstVisibleItemPosition()
                        val visibleProductCount = layoutManager.childCount
                        val totalProductCount = layoutManager.itemCount

                        /** 스크롤이 밑에까지 완료되면, getProductsWithRealtime() 다시 작동 */
                        if (isScrolling && firstVisibleProductPosition + visibleProductCount == totalProductCount) {
                            isScrolling = false
                            getNewQuery()
                        }
                    }
                }
            }

        binding.recyclerViewForBoard.addOnScrollListener(onScrollListener)


        startListening()
    }

    private fun getNewQuery() {
        TODO("Not yet implemented")
    }

    private fun setBoardSecondAdapterList() {
        viewModel.listForBoardSecondAdapter.observe(viewLifecycleOwner) {
            Log.e(TAG, "listForBoardSecondAdapter 옵저빙 : $it")
            boardSecondAdapter?.submitList(it)
            boardSecondAdapter?.notifyDataSetChanged() // TODO : DiffUtil 동작하지 않으므로, 일반 RecyclerView 로 원복할 것.
        }
    }

    private fun startListening() {
        if (registration == null) {
            registration = viewModel.queryForBoard.addSnapshotListener(this)
        }
    }

    private fun stopListening() {
        registration?.remove()
        registration = null
        snapshots.clear()
//        viewModel.setListForBoardSecond(emptyList())
    }


    /** paging realtime 방식 */
//    private fun initProductsAdapter() {
//        productsAdapter = ProductsAdapterSecond(productList)
//        binding.recyclerViewForBoard.adapter = productsAdapter
//
//        val animator = binding.recyclerViewForBoard.itemAnimator
//        if (animator is SimpleItemAnimator) {
//            animator.supportsChangeAnimations = false
//        }
//
//        val onScrollListener: RecyclerView.OnScrollListener =
//            object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(
//                    recyclerView: RecyclerView,
//                    newState: Int
//                ) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                        isScrolling = true
//                    }
//                }
//
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
//                    if (layoutManager != null) {
//                        val firstVisibleProductPosition =
//                            layoutManager.findFirstVisibleItemPosition()
//                        val visibleProductCount = layoutManager.childCount
//                        val totalProductCount = layoutManager.itemCount
//
//                        /** 스크롤이 밑에까지 완료되면, getProductsWithRealtime() 다시 작동 */
//                        if (isScrolling && firstVisibleProductPosition + visibleProductCount == totalProductCount) {
//                            isScrolling = false
//                            getProductsWithRealtime()
//                        }
//                    }
//                }
//            }
//
//        binding.recyclerViewForBoard.addOnScrollListener(onScrollListener)
//    }




    /** 로그인 권유 코드 */
    private fun loginSuggest() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 로그인 Fragment 로 이동
//            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToLoginFragment())
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ -> }
        builder.setTitle(getString(R.string.need_login))
        builder.setMessage(getString(R.string.login_suggest))
        builder.create().show()
    }

    private fun activateProgressBar(boolean: Boolean) {
        binding.progressBar.visibility = if (boolean) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        stopListening()
    }

    override fun onResume() {
        super.onResume()
    }
    /** 쿼리 RealTime EventListener */
    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        // 에러 발생 시,
        if (e != null) {
            Log.e(TAG, "onEvent:error", e)
            return
        }

        // Dispatch the event
        if (documentSnapshots != null) {
            for (change in documentSnapshots.documentChanges) {

                Log.e(TAG, "onEvent 호출 ${change.document.toObject<Product>().title} : ${change.type}")

                // snapshot of the changed document
                when (change.type) {
                    DocumentChange.Type.ADDED -> {
                        onDocumentAdded(change)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        onDocumentModified(change)
                    }
                    DocumentChange.Type.REMOVED -> {
                        onDocumentRemoved(change)
                    }
                }
            }
        }
    }

    private fun onDocumentAdded(change: DocumentChange) {
        // 특정 위치에, 특정 요소를 넣는다.
        snapshots.add(change.newIndex, change.document.toObject())
        viewModel.setListForBoardSecond(snapshots)
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document.toObject()
            viewModel.setListForBoardSecond(snapshots)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document.toObject())
            viewModel.setListForBoardSecond(snapshots)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
    }

}