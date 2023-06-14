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
import androidx.lifecycle.Transformations
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.room.Database
import com.dreamreco.firebaseappstreamtest.ui.fireStoreStatistics.StatisticsRecord
import com.dreamreco.firebaseappstreamtest.util.FireStoreConst
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

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
                Log.e(TAG, "Board Count failed: ", task.exception)
            }
        }
    }

    /** 마지막 limit 이후의 query 를 가져오는 코드 */
    fun queryForBoardAfter(lastDocument: DocumentSnapshot): Query = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .startAfter(lastDocument)
        .limit(FireStoreConst.LIMIT)

    /** 마지막 limit 이전의 query 를 가져오는 코드 */
    fun queryForBoardBefore(firstDocument: DocumentSnapshot): Query = questionAndAnswerRef
        .orderBy("boardWriter", Query.Direction.DESCENDING)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .endBefore(firstDocument)
        .limitToLast(FireStoreConst.LIMIT)

    /** 끝에서 마지막 limit query 를 가져오는 코드 */
    fun queryForBoardEndAt(): Query = questionAndAnswerRef
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

    /** Test */
    fun updateRecord(record: StatisticsRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            val collectionRef = fireStoreRef.collection("test")
            val documentRef = collectionRef.document("totalRecord")

            var names: Long? = null
            var types: Long? = null
            var volume: Long? = null
            var recordsCount: Long? = null
            var alcohols: Long? = null

            // 데이터 로드
            val loadDataTask = documentRef.get().continueWith { documentSnapshot ->
                documentSnapshot.result?.data
            }

            // 데이터 업데이트 및 intResult 초기화
            loadDataTask.onSuccessTask { records ->
                var data1: List<Long>? = null
                var data2: List<Long>? = null
                var data3: List<Long>? = null
                var data4: List<Long>? = null
                var data5: List<Long>? = null

                records?.forEach { (recordType, recordResult) ->
                    val list = recordResult as MutableList<Long>
                    Log.e(TAG, "$recordType : $list")
                    when (recordType) {
                        "numberOfAlcohols" -> {
                            data1 = replaceListElement(
                                MyApplication.prefs.getLong("numberOfAlcohols", -1L),
                                record.alcohols,
                                list
                            )
                            alcohols = record.alcohols
                        }

                        "numberOfNames" -> {
                            data2 = replaceListElement(
                                MyApplication.prefs.getLong("numberOfNames", -1L),
                                record.names,
                                list
                            )
                            names = record.names
                        }

                        "numberOfRecords" -> {
                            data3 = replaceListElement(
                                MyApplication.prefs.getLong("numberOfRecords", -1L),
                                record.records,
                                list
                            )
                            recordsCount = record.records
                        }

                        "numberOfTypes" -> {
                            data4 = replaceListElement(
                                MyApplication.prefs.getLong("numberOfTypes", -1L),
                                record.types,
                                list
                            )
                            types = record.types
                        }

                        "numberOfVolume" -> {
                            data5 = replaceListElement(
                                MyApplication.prefs.getLong("numberOfVolume", -1L),
                                record.volumes,
                                list
                            )
                            volume = record.volumes
                        }
                    }
                }

                // 변경된 배열로 문서 업데이트
                documentRef.update(
                    mapOf(
                        "numberOfAlcohols" to data1,
                        "numberOfNames" to data2,
                        "numberOfRecords" to data3,
                        "numberOfTypes" to data4,
                        "numberOfVolume" to data5
                    )
                ).continueWith {
                    if (names != null && types != null && volume != null && alcohols != null && recordsCount != null) {
                        NowRecordInt(names!!, alcohols!!, types!!, volume!!, recordsCount!!)
                        Log.e("FireStoreStatisticsViewModel", "updatePref 작동함")
                        MyApplication.prefs.setLong("numberOfAlcohols", alcohols!!)
                        MyApplication.prefs.setLong("numberOfNames", names!!)
                        MyApplication.prefs.setLong("numberOfRecords", recordsCount!!)
                        MyApplication.prefs.setLong("numberOfTypes", types!!)
                        MyApplication.prefs.setLong("numberOfVolume", volume!!)
                    } else {
                        null
                    }
                }
            }
        }
    }

    data class NowRecordInt(
        var names: Long,
        var alcohols: Long,
        var types: Long,
        var volume: Long,
        var records: Long,
    )

    private fun replaceListElement(
        preValue: Long,
        nextValue: Long,
        allList: MutableList<Long>
    ): List<Long> {
        val list = mutableListOf<Long>()
        list.addAll(allList)
        return if (preValue != -1L) {
            val index = list.indexOf(preValue)
            Log.e(TAG, "index : $index")
            if (index != -1) {
                Log.e(TAG, "가지고 있다!!")
                list[index] = nextValue
                Log.e(TAG, "교체함 $preValue -> $nextValue / $index")
            } else {
                Log.e(TAG, "없다..ㅜㅠ")
                list.add(nextValue)
            }
            list
        } else {
            list.add(nextValue)
            list
        }
    }

    fun readRecord(): MutableLiveData<List<AllRecordData>> {
        val result = MutableLiveData<List<AllRecordData>>()
        val docRef = fireStoreRef.collection("test").document("totalRecord")
        docRef.get().addOnCompleteListener { recordListTask: Task<DocumentSnapshot> ->
            if (recordListTask.isSuccessful) {
                val smallList = mutableListOf<AllRecordData>()
                Log.e("fireStoreRef", "totalRecord 요청해서 가져옴★★★")
                val document = recordListTask.result
                val data = document?.data
                data?.forEach { (recordType, recordResult) ->
                    val print = AllRecordData(recordType, recordResult as List<Int>)
                    smallList.add(print)
                    Log.e(TAG, "$print")
                }
                result.value = smallList
            } else {
                Log.e("fireStoreRef", "totalRecord 요청 실패★★★")
                Log.e(TAG, recordListTask.exception?.message.toString())
            }
        }
        return result
    }

    @Keep
    data class MyOnLineRank(
        var alcoholsRank: Float? = null,
        var namesRank: Float? = null,
        var recordsRank: Float? = null,
        var typesRank: Float? = null,
        var volumeRank: Float? = null
    )

    fun getMyRank(): Task<MyOnLineRank> {
        val collectionRef = fireStoreRef.collection("test")
        val documentRef = collectionRef.document("totalRecord")

        // 데이터 로드
        val loadDataTask = documentRef.get().continueWith { documentSnapshot ->
            documentSnapshot.result?.data
        }

        // 데이터 업데이트 및 intResult 초기화
        return loadDataTask.onSuccessTask { records ->
            var alcoholsRank: Float? = null
            var namesRank: Float? = null
            var recordsRank: Float? = null
            var typesRank: Float? = null
            var volumeRank: Float? = null

            records?.forEach { (recordType, recordResult) ->
                val list = recordResult as MutableList<Long>
                when (recordType) {
                    "numberOfAlcohols" -> {
                        alcoholsRank = calculateMyRank(
                            MyApplication.prefs.getLong("numberOfAlcohols", -1L),
                            list
                        )
                    }

                    "numberOfNames" -> {
                        namesRank = calculateMyRank(
                            MyApplication.prefs.getLong("numberOfNames", -1L),
                            list
                        )
                    }

                    "numberOfRecords" -> {
                        recordsRank = calculateMyRank(
                            MyApplication.prefs.getLong("numberOfRecords", -1L),
                            list
                        )
                    }

                    "numberOfTypes" -> {
                        typesRank = calculateMyRank(
                            MyApplication.prefs.getLong("numberOfTypes", -1L),
                            list
                        )
                    }

                    "numberOfVolume" -> {
                        volumeRank = calculateMyRank(
                            MyApplication.prefs.getLong("numberOfVolume", -1L),
                            list
                        )
                    }
                }
            }
            Tasks.forResult(
                MyOnLineRank(
                    alcoholsRank,
                    namesRank,
                    recordsRank,
                    typesRank,
                    volumeRank
                )
            )
        }
    }


    private fun calculateMyRank(myValue: Long, totalList: List<Long>): Float {
        Log.e(TAG, "calculateMyRank 작동")
        val totalCount = totalList.count().toFloat()
        val sortedList = totalList.sortedDescending()
        val rank = sortedList.indexOf(myValue).takeIf { it != -1 }
        Log.e(TAG, "totalCount : $totalCount")
        Log.e(TAG, "sortedList : $sortedList")
        Log.e(TAG, "rank : $rank")
        return if (rank != null) {
            /** 소수 첫재짜리 출력 */
            ((((rank.toFloat() + 1f) / totalCount) * 100f) * 10f).roundToInt() / 10f
        } else {
            0f
        }
    }

}

@Parcelize
@Keep
data class QuestionAndAnswerClass(
    var boardWriter: String = "",
    var content: String = "",
    var title: String = "",
    var uid: String = "",
    var replyCount: Int = 0,
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
    var replyCount: Int = 0,
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

@Parcelize
@Keep
data class AllRecordData(
    var recordType: String,
    var recordResult: List<Int>,
) : Parcelable

@Keep
data class MyOnLineRank(
    var alcoholsRank: Float? = null,
    var namesRank: Float? = null,
    var recordsRank: Float? = null,
    var typesRank: Float? = null,
    var volumeRank: Float? = null
)
