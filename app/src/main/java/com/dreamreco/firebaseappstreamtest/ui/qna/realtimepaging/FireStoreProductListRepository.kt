package com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging

import android.util.Log
import com.dreamreco.firebaseappstreamtest.repository.FireBaseRepository
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.ui.qna.QuestionAndAnswerViewModel
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.ProductListLiveData.OnLastProductReachedCallback
import com.dreamreco.firebaseappstreamtest.ui.qna.realtimepaging.ProductListLiveData.OnLastVisibleProductCallback
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst.LIMIT
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject


// viewModel 에서 처음 불러와서 시작됨
// productListLiveData 소환
class FireStoreProductListRepository : QuestionAndAnswerViewModel.ProductListRepository,
    OnLastVisibleProductCallback, OnLastProductReachedCallback {

    private val firebaseFireStore = FirebaseFirestore.getInstance()
    private val productsRef = firebaseFireStore.collection(FireStoreConst.QNA_COLLECTION)

    // 첫 지정 query
    private var query: Query = productsRef.orderBy(FireStoreConst.QNA_COLLECTION_ORDER_1, Query.Direction.DESCENDING).orderBy(FireStoreConst.QNA_COLLECTION_ORDER_2, Query.Direction.DESCENDING).limit(LIMIT)

    private var lastVisibleProduct: DocumentSnapshot? = null
    private var isLastProductReached = false

    override val productListLiveData: ProductListLiveData?
        get() {
            /** 마지막 data 에 닿으면 전송 중단 */
            if (isLastProductReached) {
                Log.e(TAG, "productListLiveData - isLastProductReached 닿음")
                return null
            }
            if (lastVisibleProduct != null) {

                Log.e(TAG, "productListLiveData - lastVisibleProduct : ${lastVisibleProduct!!.toObject<QuestionAndAnswerClass>()?.title}")
                /** 반드시 nonnull !! 처리를 해줘야 오류없이 작동함 ★★★★  */
                query = query.startAfter(lastVisibleProduct!!)
            }

            val result = ProductListLiveData(query, this, this)

            Log.e(TAG, "getProductListLiveData() 호출 query : $query")

            return result
        }

    override fun onActiveByForce() {
        productListLiveData?.onActiveByForce()
    }

    override fun onInactiveByForce() {
        productListLiveData?.onInactiveByForce()
    }

    override fun setLastVisibleProduct(lastVisibleProduct: DocumentSnapshot?) {
        Log.e(TAG, "override lastVisibleProduct : ${lastVisibleProduct?.toObject<QuestionAndAnswerClass>()?.title}")
        this.lastVisibleProduct = lastVisibleProduct
    }

    override fun setLastProductReached(isLastProductReached: Boolean) {
        Log.e(TAG, "override isLastProductReached : $isLastProductReached")
        this.isLastProductReached = isLastProductReached
    }

    companion object {
        private const val TAG = "FireStoreProductListRepository"
    }
}