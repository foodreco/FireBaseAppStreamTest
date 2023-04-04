package com.dreamreco.firebaseappstreamtest.ui.qna

import android.util.Log
import androidx.annotation.Keep
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dreamreco.firebaseappstreamtest.repository.FireBaseRepository
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.*

class FireStorePagingSource(
    private val mQuery: Query
) : PagingSource<QuerySnapshot, QuestionAndAnswerEditClass>() {

    private val fireStoreRef = Firebase.firestore
    private val questionAndAnswerRef = fireStoreRef.collection(FireStoreConst.QNA_COLLECTION)

    override fun getRefreshKey(state: PagingState<QuerySnapshot, QuestionAndAnswerEditClass>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, QuestionAndAnswerEditClass> {
        return try {

            // get() 메소드가 비동기로 동작하므로, 해당 메소드의 실행 결과를 기다리기 위해 .await() 해야 함
            val currentPage = params.key ?: mQuery.get().await()
            val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]

            Log.e(
                "FireStorePagingSource",
                "마지막 title : ${lastVisibleProduct.toObject<QuestionAndAnswerClass>()?.title}"
            )

            val nextPage = mQuery.startAfter(lastVisibleProduct).get().await()


            /** snapshot id 포함하여 데이터 변환 전송 */
            val convertedData : List<QuestionAndAnswerEditClass> = currentPage.documents.map { snapshot ->

                var count = 0

//                val subCollectionRef = questionAndAnswerRef.document(snapshot.id).collection(FireStoreConst.QNA_REPLY_COLLECTION).count().get(AggregateSource.SERVER).await()

//                val test = snapshot.data?.contains(FireStoreConst.QNA_REPLY_COLLECTION)
//                val test = snapshot.data
//                val test = snapshot.data?.get("title")
//                val test2 = questionAndAnswerRef.document(snapshot.id).collection(FireStoreConst.QNA_REPLY_COLLECTION).count().get(AggregateSource.SERVER).await()

//                Log.e("FireStorePagingSource","${snapshot.id} : $test \n 댓글 존재 : ${test2.count}")
//                Log.e("FireStorePagingSource","${snapshot.id} 댓글 존재 : $test2")


//                if (snapshot.contains(FireStoreConst.QNA_REPLY_COLLECTION)) {
//                    // QNA_REPLY_COLLECTION 컬렉션이 존재하는 경우
//                    val subCollectionRef = questionAndAnswerRef.document(snapshot.id).collection(FireStoreConst.QNA_REPLY_COLLECTION).count().get(AggregateSource.SERVER).await()
////                    val subCollectionRef = mQuery .document(snapshot.id).collection(FireStoreConst.QNA_REPLY_COLLECTION).count().get(AggregateSource.SERVER).await()
//
////                    count = subCollectionRef.count.toInt()
////                    val subCollectionSnapshot = subCollectionRef.get().await()
////                    count = subCollectionSnapshot.size() // 문서 갯수 가져오기
//                    Log.e("FireStorePagingSource","snapshot.id 댓글 존재함!")
//
//                }


//                questionAndAnswerRef.count().get(AggregateSource.SERVER)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val snapshot = task.result
//                            Log.e(FireBaseRepository.TAG, "Board Total Count: ${snapshot.count}")
//                        } else {
//                            Log.e(FireBaseRepository.TAG, "Board Count failed: ", task.exception)
//                        }
//                    }

                mapToQuestionAndAnswerEditClass(
                    snapshot.id,
                    snapshot.toObject<QuestionAndAnswerClass>()!!
                )
            }



            LoadResult.Page(
                data = convertedData,
                prevKey = null,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

@Keep
data class QuestionAndAnswerEditClassTest(
    var id: String = "",
    var questionAndAnswerClass: QuestionAndAnswerClass = QuestionAndAnswerClass()
)


fun mapToQuestionAndAnswerEditClass(
    id: String,
    pre: QuestionAndAnswerClass
): QuestionAndAnswerEditClass {
    return with(pre) {
        QuestionAndAnswerEditClass(
            id, boardWriter, content, title, uid, replyCount, timestamp
        )
    }
}

fun mapToQuestionAndAnswerEditClass(data: QuestionAndAnswerEditClassTest): QuestionAndAnswerEditClass {
    return QuestionAndAnswerEditClass(
        documentId = data.id,
        boardWriter = data.questionAndAnswerClass.boardWriter,
        content = data.questionAndAnswerClass.content,
        title = data.questionAndAnswerClass.title,
        uid = data.questionAndAnswerClass.uid,
        timestamp = data.questionAndAnswerClass.timestamp
    )
}


