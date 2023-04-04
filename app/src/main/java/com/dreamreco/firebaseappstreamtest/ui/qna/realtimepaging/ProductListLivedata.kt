package com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging

import android.util.Log
import androidx.lifecycle.LiveData
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Product
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst.LIMIT
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject

// FireStoreProductListRepository 에서 소환됨
class ProductListLiveData constructor(
    private val query: Query,
    private val onLastVisibleProductCallback: OnLastVisibleProductCallback,
    private val onLastProductReachedCallback: OnLastProductReachedCallback
) : LiveData<Operation?>(), EventListener<QuerySnapshot?> {

    private var listenerRegistration: ListenerRegistration? = null

    /** 소환됨과 둥시에, query 등록 */
    override fun onActive() {
        Log.e(TAG, "onActive 작동 query : $query")
        Log.e(TAG, "onActive 작동 listenerRegistration : $listenerRegistration")

        if (listenerRegistration == null) {
            listenerRegistration = query.addSnapshotListener(this)
            Log.e(TAG, "onActive listenerRegistration 등록 : $listenerRegistration")
        }
    }

//    TODO : 제거를 수동으로 해볼까?? onInactive
//    현재 문제점 : 방금 작성한 글도 업데이트 되지 않음 / 수정글도 반영되지 않음
//     -> editFragment 에서 글을 작성할 때, listenerRegistration 이 제거된 상태라서 그런거 같음
//     -> 제거를 수동으로 해볼까?? onInactive를 blank하고 직접 remove를 설정하는 거임...

    fun onActiveByForce() {
        Log.e(TAG, "onActiveByForce 작동 query : $query")
        Log.e(TAG, "onActiveByForce 작동 listenerRegistration : $listenerRegistration")

        if (listenerRegistration == null) {
            listenerRegistration = query.addSnapshotListener(this)
            Log.e(TAG, "onActiveByForce listenerRegistration 등록 : $listenerRegistration")
        }
    }

    fun onInactiveByForce() {
        if (listenerRegistration != null) {
            listenerRegistration!!.remove()
            Log.e(TAG, "onInactive 작동, listenerRegistration 제거 : $listenerRegistration")
        }
    }

    /** 연계된 parent 의 수명주기에 따라서 자동으로 remove 되게 함 */
    override fun onInactive() {
//        Log.e(TAG, "onInactive 작동, listenerRegistration 제거 : $listenerRegistration")
//        listenerRegistration!!.remove()
    }

    interface OnLastVisibleProductCallback {
        fun setLastVisibleProduct(lastVisibleProduct: DocumentSnapshot?)
    }

    interface OnLastProductReachedCallback {
        fun setLastProductReached(isLastProductReached: Boolean)
    }

    override fun hasActiveObservers(): Boolean {
        return super.hasActiveObservers()
    }

    override fun hasObservers(): Boolean {
        return super.hasObservers()
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) return
        if (querySnapshot != null) {

            //TODO : romove 수동 설정으로 활성화 해놓더라도, 이전 fragment 로 돌아가기 전에 onevent 이미 작동해버림
            //  -> Room 을 별도로 만들어서, back end 에서 리스트를 넣고 빼자??
            //  -> 또는 SharePreference 적용으로??
            Log.e(TAG, "옵저버 존재 유무 : ${this.hasObservers()}")
            Log.e(TAG, "액티브 옵저버?? : ${this.hasActiveObservers()}")

            for (documentChange in querySnapshot.documentChanges) {
                Log.e(
                    TAG,
                    "onEvent 호출 ${documentChange.document.toObject<Product>().title} : ${documentChange.type}"
                )
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        documentChange.document.let {
                            val id = it.id
                            val addedProduct = it.toObject(Product::class.java)
                            with(addedProduct) {
                                val questionAndAnswerEditClass = QuestionAndAnswerEditClass(
                                    id,
                                    boardWriter,
                                    content,
                                    title,
                                    uid,
                                    replyCount,
                                    timestamp
                                )
                                val addOperation = Operation(questionAndAnswerEditClass, 1)
                                /** 옵저버 값 설정 ex.viewModel.getProductListLiveData().observer */
                                value = addOperation
                            }
                        }
                    }
                    DocumentChange.Type.MODIFIED -> {

                        documentChange.document.let {
                            val id = it.id
                            val addedProduct = it.toObject(Product::class.java)
                            with(addedProduct) {
                                val questionAndAnswerEditClass = QuestionAndAnswerEditClass(
                                    id,
                                    boardWriter,
                                    content,
                                    title,
                                    uid,
                                    replyCount,
                                    timestamp
                                )
                                val addOperation = Operation(questionAndAnswerEditClass, 2)
                                /** 옵저버 값 설정 ex.viewModel.getProductListLiveData().observer */
                                value = addOperation
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {

                        documentChange.document.let {
                            val id = it.id
                            val addedProduct = it.toObject(Product::class.java)
                            with(addedProduct) {
                                val questionAndAnswerEditClass = QuestionAndAnswerEditClass(
                                    id,
                                    boardWriter,
                                    content,
                                    title,
                                    uid,
                                    replyCount,
                                    timestamp
                                )
                                val addOperation = Operation(questionAndAnswerEditClass, 3)
                                /** 옵저버 값 설정 ex.viewModel.getProductListLiveData().observer */
                                value = addOperation
                            }

                        }
                    }
                }
            }

            val querySnapshotSize = querySnapshot.size()
            Log.e(TAG, "querySnapshotSize 갯수 : $querySnapshotSize")

            /** 호출된 querySnapshotSize 갯수가 query limit 보다 작으면 다음부터 더이상 query 추가 호출하지 않음 */
            if (querySnapshotSize < LIMIT.toInt()) {
                onLastProductReachedCallback.setLastProductReached(true)

                /** querySnapshot 갯수가 0 개인 경우, null 을 미리 반환하여 신호를 준다.*/
                value = null

                Log.e(TAG, "setLastProductReached(true) 작동")
            } else {
                val lastVisibleProduct = querySnapshot.documents[querySnapshotSize - 1]
                Log.e(
                    TAG,
                    "lastVisibleProduct : ${lastVisibleProduct.toObject<QuestionAndAnswerClass>()?.title}"
                )
                onLastVisibleProductCallback.setLastVisibleProduct(lastVisibleProduct)
                Log.e(TAG, "setLastVisibleProduct(lastVisibleProduct) 작동")
            }
        } else {
            onLastProductReachedCallback.setLastProductReached(true)
        }
    }

    companion object {
        private const val TAG = "ProductListLiveData"
    }
}

class Operation(var product: QuestionAndAnswerEditClass, var type: Int)