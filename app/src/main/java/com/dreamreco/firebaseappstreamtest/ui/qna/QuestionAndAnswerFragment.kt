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
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentQuestionAndAnswerBinding
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Product
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.Operation
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ArrayList


@AndroidEntryPoint
class QuestionAndAnswerFragment : Fragment(), BoardAdapter.OnBoardSelectedListener,
    com.google.firebase.firestore.EventListener<QuerySnapshot>,
    QuestionAndAnswerWriteFragment.WriteActionCompletedListener {

    companion object {
        private const val TAG = "QuestionAndAnswerFragment"
    }

    private val binding by lazy { FragmentQuestionAndAnswerBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<QuestionAndAnswerViewModel>()

    private var mAdapter: BoardAdapter? = null

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    private val pageNumber = MutableLiveData<Int>()
    private var pageNumberCount = 0

    /** paging + 수동 update : ★★★ */
    private val newAdapter = ProductsAdapter()

    /** paging realtime 관련 */
    private var isScrolling = false
    private val productList = mutableListOf<QuestionAndAnswerEditClass>()
    private var productsAdapter: ProductsAdapterSecond? = null

    /** E + ListAdapter 관련 */
    private var boardSecondAdapter: BoardSecondAdapter? = null
    private val snapshots = ArrayList<QuestionAndAnswerClass>()
    private var registration: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        viewModel.totalCountForBoard()

        pageNumberCount = 1
        pageNumber.value = pageNumberCount

        // query + recyclerView 방식
//        setQueryBasic()

        Log.e(TAG, "onCreate 작동")


        // paging 방식
        setProductsAdapter()
        getProducts()
        setProgressBarAccordingToLoadState()


        //paging + realtime 방식 ★★★
//        initProductsAdapter()
//        getProductsWithRealtime()


//        viewModel.queryForTest
//        initBoardSecondAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e(TAG, "onCreateView 작동")


//        setBoardList()

//        setBoardSecondAdapterList()

        with(binding) {
            btnWriteQuestion.setOnClickListener {
                if (user != null) {
                    Log.e(TAG, "질문 작성하기")

                    val fragment = QuestionAndAnswerWriteFragment()
                    fragment.show(childFragmentManager,"QuestionAndAnswerWriteFragment")

//                    it.findNavController().navigate(
//                        QuestionAndAnswerFragmentDirections.actionQuestionAndAnswerFragmentToQuestionAndAnswerWriteFragment()
//                    )
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


    /** 4. EventListener + ListAdapter 방식 */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initBoardSecondAdapter() {
        boardSecondAdapter = BoardSecondAdapter()
        boardSecondAdapter!!.setProductsAdapterListener(object :
            BoardSecondAdapter.OnBoardSelectedListener {
            override fun onBoardSelected(content: QuestionAndAnswerClass) {
                with(content) {
                    Log.e(
                        TAG,
                        "선택된 item\n제목 : $title / 내용 : $content / timeStamp : ${content.timestamp} / uid : $uid"
                    )
                }
            }
        })
        binding.recyclerViewForBoard.adapter = boardSecondAdapter
        val animator = binding.recyclerViewForBoard.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        startListening()
    }

    private fun setBoardSecondAdapterList() {
        viewModel.listForBoardSecondAdapter.observe(viewLifecycleOwner) {
            Log.e(TAG, "listForBoardSecondAdapter 옵저빙 : $it")
            boardSecondAdapter?.submitList(it)
            boardSecondAdapter?.notifyDataSetChanged() // TODO : DiffUtil 동작하지 않으므로, 일반 RecyclerView 로 원복할 것.
        }
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

                Log.e(
                    TAG,
                    "onEvent 호출 ${change.document.toObject<Product>().title} : ${change.type}"
                )

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
//        boardSecondAdapter!!.notifyItemInserted(change.newIndex)
        viewModel.setListForBoardSecond(snapshots)
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document.toObject()
//            notifyItemChanged(change.oldIndex)
            viewModel.setListForBoardSecond(snapshots)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document.toObject())
//            notifyItemMoved(change.oldIndex, change.newIndex)
            viewModel.setListForBoardSecond(snapshots)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
//        notifyItemRemoved(change.oldIndex)
    }

    fun startListening() {
        if (registration == null) {
            registration = viewModel.queryForBoard.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        registration?.remove()
        registration = null
        snapshots.clear()
        viewModel.setListForBoardSecond(emptyList())
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    /** 3. paging realtime 방식 ★ (제자리에서 수신 가능) */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initProductsAdapter() {

        productsAdapter = ProductsAdapterSecond(productList)

        productsAdapter!!.setProductsAdapterListener(object :
            ProductsAdapterSecond.OnBoardSelectedListener {
            override fun onBoardSelected(content: QuestionAndAnswerEditClass) {
                with(content) {
                    Log.e(
                        TAG,
                        "선택된 item\n제목 : $title / 내용 : $content / timeStamp : ${content.timestamp} / uid : $uid"
                    )
                    val argument = QuestionAndAnswerEditClass(
                        documentId,
                        boardWriter,
                        content.content,
                        title,
                        uid,
                        replyCount,
                        timestamp
                    )
                    findNavController().navigate(
                        QuestionAndAnswerFragmentDirections.actionQuestionAndAnswerFragmentToQuestionAndAnswerEditFragment(
                            argument
                        )
                    )
                }
            }
        })

        binding.recyclerViewForBoard.adapter = productsAdapter

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
                            activateProgressBar(true)
                            isScrolling = false
                            getProductsWithRealtime()
                        }
                    }
                }
            }
        binding.recyclerViewForBoard.addOnScrollListener(onScrollListener)
    }


    // 스크롤 완료 시, 다시호출
    private fun getProductsWithRealtime() {
        Log.e(TAG, "getProductsWithRealtime 작동")
        val request = viewModel.getProductListLiveData()

        if (request != null) {
            request.observe(this) { operation: Operation? ->
                if (operation == null) {
                    activateProgressBar(false)
                    return@observe
                }
                Log.e(TAG, "fragment 변화 : ${operation.product.title} / 수신타입 : ${operation.type}")
                when (operation.type) {
                    1 -> {
                        val addedProduct = operation.product
                        addProduct(addedProduct)
                    }
                    2 -> {
                        val modifiedProduct = operation.product
                        modifyProduct(modifiedProduct)
                    }
                    3 -> {
                        val removedProduct = operation.product
                        removeProduct(removedProduct)
                    }
                }

                productsAdapter?.notifyDataSetChanged()
                activateProgressBar(false)
            }
        } else {
            // query 맨 마지막까지 가서, null 을 반환할때
            activateProgressBar(false)
        }
    }

    private fun addProduct(addedProduct: QuestionAndAnswerEditClass) {
        Log.e(TAG, "addedProduct : ${addedProduct.title}")
        Log.e(TAG, "addProduct 전 productList : $productList")
//        productList.add(addedProduct)

        var finalPosition = -1

        for (i in 0 until productList.size) {
            val currentProduct: QuestionAndAnswerEditClass = productList[i]
            if (addedProduct.timestamp!! > currentProduct.timestamp) {
                finalPosition = i
            }
        }

        if (finalPosition != -1) {
            productList.add(finalPosition, addedProduct)
        } else {
            productList.add(addedProduct)
        }

        Log.e(TAG, "addProduct 후 productList : $productList")
    }

    private fun modifyProduct(modifiedProduct: QuestionAndAnswerEditClass) {
        try {
            Log.e(TAG, "modifiedProduct : ${modifiedProduct.title}")
            Log.e(TAG, "modifyProduct 전 productList : $productList")
            for (i in 0 until productList.size) {
                val currentProduct: QuestionAndAnswerEditClass = productList[i]
                if (currentProduct.documentId == modifiedProduct.documentId) {
                    productList.remove(currentProduct)
                    productList.add(i, modifiedProduct)
                }
            }
            Log.e(TAG, "modifyProduct 후 productList : $productList")
        } catch (e: Exception) {
            Log.e(TAG, "modifiedProduct 에러발생", e)
        }
    }

    // TODO : 삭제 시, index out of bound 에러 발생함 -> ANR
    private fun removeProduct(removedProduct: QuestionAndAnswerEditClass) {
        try {
            Log.e(TAG, "삭제대상 : $removedProduct")
            Log.e(TAG, "removeProduct 전 productList : $productList")

            Log.e(TAG, "★  productList.size : ${productList.size}")
            val removeTarget = mutableListOf<QuestionAndAnswerEditClass>()
            for (i in 0 until productList.size) {
                val currentProduct: QuestionAndAnswerEditClass = productList[i]
                Log.e(TAG, "★  productList[i] : ${productList[i]}")
                if (currentProduct.documentId == removedProduct.documentId) {
                    removeTarget.add(currentProduct)
//                    productList.remove(currentProduct)
                }
            }

            if (removeTarget != emptyList<QuestionAndAnswerEditClass>()) {
                for (removeElement in removeTarget) {
                    productList.remove(removeElement)
                }
            }

            Log.e(TAG, "removeProduct 후 productList : $productList")
        } catch (e: Exception) {
            Log.e(TAG, "removeProduct 에러발생", e)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /** 2. paging RecyclerView 방식 ★★★ 수동 갱신 가능 onStart */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setProductsAdapter() {
        newAdapter.setProductsAdapterListener(object : ProductsAdapter.OnBoardSelectedListener {
            override fun onBoardSelected(
                content: QuestionAndAnswerEditClass
            ) {
                findNavController().navigate(
                    QuestionAndAnswerFragmentDirections.actionQuestionAndAnswerFragmentToQuestionAndAnswerEditFragment(content)
                )

            }
        })
        binding.recyclerViewForBoard.adapter = newAdapter

//        newAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    private fun getProducts() {
        Log.e(TAG, "getProducts() 작동")

        lifecycleScope.launch {
            viewModel.getFlow().collectLatest {
                Log.e(TAG, "viewModel.flow.collectLatest : $it")
                newAdapter.submitData(it)
            }
        }

//        viewModel.getQueryLive().observe(viewLifecycleOwner) {
//            Log.e(TAG, "getQueryLive : $it")
//            newAdapter.submitData(lifecycle, it)
//        }
    }

    private fun setProgressBarAccordingToLoadState() {
        lifecycleScope.launch {
            newAdapter.loadStateFlow.collectLatest {
                binding.progressBar.isVisible = it.refresh is LoadState.Loading
                binding.progressBar.isVisible = it.append is LoadState.Loading
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /** 1. 직접 Pagination 형태 */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** 리스트 유지를 위해 observer 형태로 관리하는 코드*/
    private fun setBoardList() {
        with(viewModel) {
            pageNumber.observe(viewLifecycleOwner) { page ->
                when (page) {
                    1 -> {
                        setQueryForLive(queryForBoard)
                    }
                    else -> {
                    }
                }
            }

            queryForLive.observe(viewLifecycleOwner) { query ->
                query.let {
                    mAdapter?.setQuery(it)
                    setQueryListener(it)
                }
            }
        }
    }

    /** query 를 반영하는 recyclerView 초기화 */
    private fun setQueryBasic() {
        mAdapter = object : BoardAdapter(null, this@QuestionAndAnswerFragment) {
            override fun onError(e: FirebaseFirestoreException) {
                Snackbar.make(
                    binding.root,
                    "에러발생 : $e.", Snackbar.LENGTH_LONG
                ).show()
                Log.e(TAG, "에러발생", e)
            }

            override fun onDataChanged() {
                if (itemCount == 0) {
                    Toast.makeText(requireContext(), "리스트가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                activateProgressBar(false)
            }
        }

        with(binding) {
            recyclerViewForBoard.adapter = mAdapter

            val animator = recyclerViewForBoard.itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
        }
    }

    /** paginating 리스너 코드 */
    private fun setQueryListener(query: Query) {
        query.get().addOnSuccessListener { documentSnapshots ->
            var lastVisible: DocumentSnapshot? = null
            var firstVisible: DocumentSnapshot? = null

            val queryDocumentSize = documentSnapshots.size()

            try {
                val motherTest = documentSnapshots.documents
                for (test in motherTest) {
                    val print = test.toObject<QuestionAndAnswerClass>()?.title
                    Log.e(TAG, "$print")
                }
            } catch (e: Exception) {
                Log.e(TAG, "for 에러 발생", e)
            }

            if ((queryDocumentSize < FireStoreConst.LIMIT) && (pageNumberCount == 1)) {
                // 첫번째 페이지면서, total size 가 limit 보다 작을 때
                if (queryDocumentSize == 0) {
                    // 게시판이 비었을 때,
                    pageButtonActivationChild(
                        nextButton = false,
                        previousButton = false
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")
                } else {
                    pageButtonActivationChild(
                        nextButton = true,
                        previousButton = false,
                    )
                    lastVisible = documentSnapshots.documents[queryDocumentSize - 1]
                    Log.e(
                        TAG,
                        "btnNextPage 기준 : ${lastVisible.toObject<QuestionAndAnswerClass>()?.title}"
                    )
                    pageButtonActivationChild2(
                        binding.btnNextPage,
                        viewModel.queryForBoardAfter(lastVisible)
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")

                }
            } else if ((queryDocumentSize < FireStoreConst.LIMIT) && (pageNumberCount != 1)) {
                // 마지막 페이지일 때,
                if (queryDocumentSize == 0) {
                    pageButtonActivationChild(
                        nextButton = false,
                        previousButton = true,
                    )
                    pageButtonActivationChild2(
                        binding.btnPreviousPage,
                        viewModel.queryForBoardEndAt()
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")

                } else {
                    pageButtonActivationChild(
                        nextButton = false,
                        previousButton = true,
                    )
                    firstVisible = documentSnapshots.documents.first()
                    Log.e(
                        TAG,
                        "btnPreviousPage 기준 : ${firstVisible.toObject<QuestionAndAnswerClass>()?.title}"
                    )
                    pageButtonActivationChild2(
                        binding.btnPreviousPage,
                        viewModel.queryForBoardBefore(firstVisible)
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")

                }
            } else if (queryDocumentSize.toLong() == FireStoreConst.LIMIT) {
                if (pageNumberCount == 1) {
                    // 첫번째 꽉 찬 페이지 일 때,
                    pageButtonActivationChild(
                        nextButton = true,
                        previousButton = false,
                    )
                    lastVisible = documentSnapshots.documents[queryDocumentSize - 1]
                    Log.e(
                        TAG,
                        "btnNextPage 기준 : ${lastVisible.toObject<QuestionAndAnswerClass>()?.title}"
                    )
                    pageButtonActivationChild2(
                        binding.btnNextPage,
                        viewModel.queryForBoardAfter(lastVisible)
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")

                } else {
                    // 중간 또는 마지막 꽉 찬 페이지 일 때,
                    pageButtonActivationChild(
                        nextButton = true,
                        previousButton = true,
                    )
                    lastVisible = documentSnapshots.documents[queryDocumentSize - 1]
                    Log.e(
                        TAG,
                        "btnNextPage 기준 : ${lastVisible.toObject<QuestionAndAnswerClass>()?.title}"
                    )
                    pageButtonActivationChild2(
                        binding.btnNextPage,
                        viewModel.queryForBoardAfter(lastVisible)
                    )

                    firstVisible = documentSnapshots.documents.first()
                    Log.e(
                        TAG,
                        "btnPreviousPage 기준 : ${firstVisible.toObject<QuestionAndAnswerClass>()?.title}"
                    )
                    pageButtonActivationChild2(
                        binding.btnPreviousPage,
                        viewModel.queryForBoardBefore(firstVisible)
                    )
                    Log.e(TAG, "pageNumberCount : $pageNumberCount")
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "query error", e)
        }
    }

    private fun pageButtonActivationChild(
        nextButton: Boolean,
        previousButton: Boolean,
    ) {
        with(binding) {
            when (nextButton) {
                true -> {
                    if (previousButton) {
                        btnNextPage.visibility = View.VISIBLE
                        btnPreviousPage.visibility = View.VISIBLE
                    } else {
                        btnNextPage.visibility = View.VISIBLE
                        btnPreviousPage.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    if (previousButton) {
                        btnNextPage.visibility = View.INVISIBLE
                        btnPreviousPage.visibility = View.VISIBLE
                    } else {
                        btnNextPage.visibility = View.INVISIBLE
                        btnPreviousPage.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun pageButtonActivationChild2(view: View, query: Query) {
        view.setOnClickListener {
            if (view == binding.btnNextPage) {
                pageNumberCount += 1
                pageNumber.value = pageNumberCount
            } else {
                pageNumberCount -= 1
                pageNumber.value = pageNumberCount
            }
            viewModel.setQueryForLive(query)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


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

    //TODO : read 와 write 를 다른 fragment 로 관리한다. layout 관리 (scrollview) 의 편의성을 위해서
    //  write 는 작성 시 주의사항이 밑에 들어간다.
    override fun onBoardSelected(content: QuestionAndAnswerClass, documentId: String) {
        with(content) {
            Log.e(
                TAG,
                "선택된 item\n제목 : $title / 내용 : $content / timeStamp : ${content.timestamp} / uid : $uid"
            )
            val argument = QuestionAndAnswerEditClass(
                documentId,
                boardWriter,
                content.content,
                title,
                uid,
                replyCount,
                timestamp
            )
            findNavController().navigate(
                QuestionAndAnswerFragmentDirections.actionQuestionAndAnswerFragmentToQuestionAndAnswerEditFragment(
                    argument
                )
            )
        }
    }

    override fun onWriteBoardCompleted() {
        Log.e(TAG, "onWriteBoardCompleted")
        getProducts()
//        viewModel.onActiveByForce()
    }

    //TODO : query realtime 관련 에러들
    // 1) 질문 작성하기 or 선택 게시글 보고 돌아오면 register 등록 제거됨
    // 2) 날짜 변경?? 순서 변경 적용 안됨??
    // 3) 수신만을 계속하는 거보면 ProductListLivedata 가 여러개의 query 를 동시에 수신하는 것??★★★★★
    // 4) Fragment 가 destroy 될 때만, listenerRegistration!!.remove() 하자??!!
    // -> 수정창에서 수정을 함과 동시에 변화가 일어나고, 그 순간은 listenerRegistration!!.remove() 된 상태이므로, 변화를 알 수 없음
    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart")
//        getProducts()
        /** onActive 작동 query : com.google.firebase.firestore.Query@dd4ccdfa? */
//        viewModel.onActiveByForce()
//        getProductsWithRealtime()
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        /** 여기서 onInactive 작동, listenerRegistration 제거됨 */
        Log.e(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
//        viewModel.testTest()
        Log.e(TAG, "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.onInactiveByForce()
        Log.e(TAG, "onDestroy")
    }
}