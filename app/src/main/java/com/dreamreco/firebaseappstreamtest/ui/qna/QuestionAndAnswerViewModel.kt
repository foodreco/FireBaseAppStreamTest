package com.dreamreco.firebaseappstreamtest.ui.qna

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.repository.*
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.FireStoreProductListRepository
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.ProductListLiveData
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst.LIMIT
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class QuestionAndAnswerViewModel @Inject constructor(
    private val mFireBaseRepository: FireBaseRepository,
    application: Application
) : AndroidViewModel(application) {

    private val app = application

    private val _queryForLive = MutableLiveData<Query>()
    val queryForLive: LiveData<Query> = _queryForLive

    private val _listForBoardSecondAdapter = MutableLiveData<List<QuestionAndAnswerClass>>()
    val listForBoardSecondAdapter: LiveData<List<QuestionAndAnswerClass>> =
        _listForBoardSecondAdapter


    companion object {
        private const val TAG = "QuestionAndAnswerViewModel"
    }

    fun enrollBoardContent(questionAndAnswerContent: QuestionAndAnswerClass) {
        mFireBaseRepository.enrollBoardContent(questionAndAnswerContent)
    }

    fun editBoardContent(questionAndAnswerClass: QuestionAndAnswerEditClass) {
        mFireBaseRepository.editBoardContent(questionAndAnswerClass)
    }

    fun addReply(user: FirebaseUser, documentId: String, replyString: String): Task<Void> {
        var writer = user.email!!.substring(0, 3) + "***"
        if (user.uid == "910Em692hcQUqCdAxisu042rNcm2") {
            writer = app.getString(R.string.app_manager)
        }
        val newReply = ReplyClass(
            writer,
            replyString,
            user.uid
        )
        return mFireBaseRepository.addReply(documentId, newReply)
    }

    fun setQueryForLive(query: Query) {
        _queryForLive.value = query
    }

    val enrollCompleted = mFireBaseRepository.enrollBoardContentCompleted

    val queryForBoard = mFireBaseRepository.queryForBoard

    fun queryForBoardAfter(lastDocumentId: DocumentSnapshot): Query =
        mFireBaseRepository.queryForBoardAfter(lastDocumentId)

    fun queryForBoardBefore(lastDocumentId: DocumentSnapshot): Query =
        mFireBaseRepository.queryForBoardBefore(lastDocumentId)

    fun queryForBoardEndAt(): Query = mFireBaseRepository.queryForBoardEndAt()

    fun queryForReply(documentId: String): Query = mFireBaseRepository.queryForReply(documentId)

    fun deleteReply(upperDocumentId: String, documentId: String): Task<Void> =
        mFireBaseRepository.deleteReply(upperDocumentId, documentId)

    fun editReply(upperDocumentId: String, documentId: String) {
        mFireBaseRepository.editReply(upperDocumentId, documentId)
    }

    fun totalCountForBoard() {
        mFireBaseRepository.totalCountForBoard()
    }

    /////2.
//    val flow = Pager(
//        PagingConfig(
//            pageSize = LIMIT.toInt()
//        )
//    ) {
//        FireStorePagingSource(mFireBaseRepository.queryForBoard)
//    }.flow.cachedIn(viewModelScope)

    fun getFlow() = Pager(
        PagingConfig(
            pageSize = LIMIT.toInt()
        )
    ) {
        FireStorePagingSource(mFireBaseRepository.queryForBoard)
    }.flow.cachedIn(viewModelScope)

    fun getQueryLive() : LiveData<PagingData<QuestionAndAnswerEditClass>> =
        Pager(
            config = PagingConfig(
                pageSize = LIMIT.toInt()
            ),
            pagingSourceFactory = {
                FireStorePagingSource(mFireBaseRepository.queryForBoard)
            }).flow.cachedIn(viewModelScope).asLiveData()

    ////3.

    // 처음 변수 지정
    private val productListRepository: ProductListRepository = FireStoreProductListRepository()

    // 시작점 : 호출 시작
    // 스크롤 완료 시에도 다시 호출됨
    fun getProductListLiveData(): ProductListLiveData? {

        // 인터페이스를 자극하면 연계된 리스너 FireStoreProductListRepository 에서 받는다!!
        val result = productListRepository.productListLiveData

        Log.e(TAG, "호출되는 ProductListLiveData : ${result.hashCode()}")
        return result
    }

    fun onActiveByForce() {
        productListRepository.onActiveByForce()
    }

    fun onInactiveByForce() {
        productListRepository.onInactiveByForce()
    }


    interface ProductListRepository {
        val productListLiveData: ProductListLiveData?
        fun onActiveByForce()
        fun onInactiveByForce()
    }


    fun setListForBoardSecond(list: List<QuestionAndAnswerClass>) {
        Log.e(TAG, "setListForBoardSecond 작동 : $list")
        _listForBoardSecondAdapter.value = list
    }

}