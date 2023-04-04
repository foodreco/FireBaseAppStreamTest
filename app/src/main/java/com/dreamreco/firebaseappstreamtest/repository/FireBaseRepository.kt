package com.dreamreco.firebaseappstreamtest.repository

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dreamreco.firebaseappstreamtest.room.Database
import com.dreamreco.firebaseappstreamtest.ui.qna.QuestionAndAnswerEditFragment
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import java.util.*
import javax.inject.Inject

/** FireBase 관련 Data layer */
class FireBaseRepository @Inject constructor(
    private val database: Database, application: Application
) {

    companion object {
        private const val TAG = "FireBaseRepository"
    }

    private val fireStoreRef = Firebase.firestore
    private val storageRef = Firebase.storage.reference
    private val app = application

    private val questionAndAnswerRef = fireStoreRef.collection(FireStoreConst.QNA_COLLECTION)

    // 계정관련
    private val auth = FirebaseAuth.getInstance()

    private val _enrollBoardContentCompleted = MutableLiveData<Boolean?>(null)
    val enrollBoardContentCompleted: LiveData<Boolean?> = _enrollBoardContentCompleted

    /** 인터넷 연결 Check 함수 */
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    /** QNA 불러오는 코드 */
    /** 관리자 명의 우선, 날짜 내림차순 */
    val queryForBoard = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(FireStoreConst.LIMIT)

    /** 총 게시판 수를 가져오는 코드 */
    fun totalCountForBoard() {
        questionAndAnswerRef.count().get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                Log.e(TAG, "Board Total Count: ${snapshot.count}")
            } else {
                Log.e(TAG, "Board Count failed: ",task.exception)
            }
        }
    }

    /** 마지막 limit 이후의 query 를 가져오는 코드 */
    fun queryForBoardAfter(lastDocument:DocumentSnapshot) : Query = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .startAfter(lastDocument)
        .limit(FireStoreConst.LIMIT)

    /** 마지막 limit 이전의 query 를 가져오는 코드 */
    fun queryForBoardBefore(firstDocument:DocumentSnapshot) : Query = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .endBefore(firstDocument)
        .limitToLast(FireStoreConst.LIMIT)

    /** 끝에서 마지막 limit query 를 가져오는 코드 */
    fun queryForBoardEndAt() : Query = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limitToLast(FireStoreConst.LIMIT)

    /** 질문글을 등록하는 코드 */
    fun enrollBoardContent(questionAndAnswerContent: QuestionAndAnswerClass) {
        Log.e(TAG, "등록 진행 : $questionAndAnswerContent")
        questionAndAnswerRef.add(questionAndAnswerContent)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                _enrollBoardContentCompleted.value = true
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                _enrollBoardContentCompleted.value = false
            }
    }

    /** 등록한 질문글을 수정하는 코드 */
    fun editBoardContent(questionAndAnswerClass: QuestionAndAnswerEditClass) {
        questionAndAnswerRef.document(questionAndAnswerClass.documentId).update(
            mapOf(
                "boardWriter" to questionAndAnswerClass.boardWriter,
                "content" to questionAndAnswerClass.content,
                "title" to questionAndAnswerClass.title,
                "timestamp" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener {
            Log.d(TAG, "DocumentSnapshot successfully updated!")
            _enrollBoardContentCompleted.value = true
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                _enrollBoardContentCompleted.value = true
            }
    }

    /** 댓글 불러오는 코드 */
    fun queryForReply(documentId: String): Query =
        questionAndAnswerRef.document(documentId).collection(FireStoreConst.QNA_REPLY_COLLECTION)
            .orderBy(FireStoreConst.QNA_COLLECTION_ORDER_2, Query.Direction.DESCENDING)

    // TODO : 이런 방식으로 fragment 에서 callback 수신 가능
    /** 답글을 다는 코드 */
    fun addReply(documentId: String, reply: ReplyClass): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val qnaRef = questionAndAnswerRef.document(documentId)
//        productsRef.update("count", FieldValue.increment(1))
        // reply count 갯수 +1
        qnaRef.update("replyCount", FieldValue.increment(1))
        val replyRef = qnaRef.collection(FireStoreConst.QNA_REPLY_COLLECTION).document()

        // In a transaction, add the new rating and update the aggregate totals
        return fireStoreRef.runTransaction { transaction ->
            transaction.set(replyRef, reply)
            null
        }
    }

    /** 답글을 삭제하는 코드 */
    fun deleteReply(upperDocumentId: String, documentId: String): Task<Void> {
        val qnaRef = questionAndAnswerRef.document(upperDocumentId)
        qnaRef.update("replyCount", FieldValue.increment(-1))
        val replyRef = qnaRef.collection(FireStoreConst.QNA_REPLY_COLLECTION).document(documentId)

        return fireStoreRef.runTransaction { transaction ->
            transaction.delete(replyRef)
            null
        }
    }

    fun editReply(upperDocumentId: String, documentId: String) {
        // TODO : 댓글 edit 는 구현하지 않는다. 삭제만 구현할 것
    }

}

@Parcelize
@Keep
data class QuestionAndAnswerClass(
    var boardWriter: String = "",
    var content: String = "",
    var title: String = "",
    var uid: String = "",
    var replyCount : Int = 0,
    @ServerTimestamp var timestamp: Date? = null
) : Parcelable

@Parcelize
@Keep
data class QuestionAndAnswerEditClass(
    var documentId: String = "",
    var boardWriter: String = "",
    var content: String = "",
    var title: String = "",
    var uid: String = "",
    var replyCount : Int = 0,
    @ServerTimestamp var timestamp: Date? = null
) : Parcelable

@Parcelize
@Keep
data class QuestionAndAnswerEditClassExpended(
    var documentId: String = "",
    var boardWriter: String = "",
    var content: String = "",
    var title: String = "",
    var uid: String = "",
    @ServerTimestamp var timestamp: Date? = null,
) : Parcelable




@Parcelize
@Keep
data class ReplyClass(
    var boardWriter: String = "",
    var content: String = "",
    var uid: String = "",
    @ServerTimestamp var timestamp: Date? = null
) : Parcelable