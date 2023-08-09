package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Constants.Companion.DATA
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Constants.Companion.NAME_PROPERTY
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Constants.Companion.PRODUCTS_COLLECTION
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Constants.Companion.PRODUCT_NAMES
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Constants.Companion.PRODUCT_SEARCH_PROPERTY
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*
import javax.inject.Inject

class ProductRepository @Inject constructor() {
    private val rootRef = Firebase.firestore
    private val productSearchRef = rootRef.collection(DATA).document(PRODUCT_SEARCH_PROPERTY)
    private val productsRef = rootRef.collection(PRODUCTS_COLLECTION)
    private val storageRef = Firebase.storage.reference

    private val fireStoreReadTest = rootRef.collection("products")

    val productNameListMutableLiveData: MutableLiveData<List<String>>
        get() {
            val productNameListMutableLiveData = MutableLiveData<List<String>>()
            productSearchRef.get()
                .addOnCompleteListener { productNameListTask: Task<DocumentSnapshot> ->
                    if (productNameListTask.isSuccessful) {
                        val document = productNameListTask.result
                        if (document.exists()) {
                            val productNameList =
                                document.get(PRODUCT_NAMES) as List<String>
                            productNameListMutableLiveData.value = productNameList
                            Log.e("ProductRepository", "productNameList : $productNameList")
                        }
                    } else {
                        Log.e("ProductRepository", " 리스트 요청 실패함★★★")
                        Log.e(TAG, productNameListTask.exception?.message.toString())
                    }
                }
            return productNameListMutableLiveData
        }

    fun getDrinkInformationMutableLiveData(productName: String?): MutableLiveData<DrinkInfo?> {
        val productInfoMutableLiveData = MutableLiveData<DrinkInfo?>(null)
        productsRef.whereEqualTo(NAME_PROPERTY, productName).get().addOnCompleteListener(
            OnCompleteListener { productTask: Task<QuerySnapshot> ->
                if (productTask.isSuccessful) {

                    Log.e("ProductRepository", "쿼리 요청해서 가져옴★★★")

                    // 결과가 여러개일 경우, 마지막 것을 반환한다?
                    for (document in productTask.result) {
                        productInfoMutableLiveData.value =
                            document.toObject(DrinkInfo::class.java)
                    }
                } else {
                    Log.e("ProductRepository", "쿼리 요청 실패함★★★")
                    Log.e(TAG, productTask.exception?.message.toString())
                }
            })
        return productInfoMutableLiveData
    }

    //TODO : 확장자 고정??
    fun getDrinkImageMutableLiveData(type:String, productName: String): MutableLiveData<String?> {
        val resultLiveData = MutableLiveData<String?>(null)
        val filePath = "$type/$productName.png"
        storageRef.child(filePath).downloadUrl.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                resultLiveData.value = result.result.toString()
                Log.e("ProductRepository", "storageRef 쿼리 요청 성공 : $resultLiveData ★★★")
            } else {
                Log.e("ProductRepository", "storageRef 쿼리 요청 실패함 ${result.exception?.message.toString()}★★★")
            }
        }
        return resultLiveData
    }

    fun getImageTest(type:String, productName: String): StorageReference {
        val filePath = "$type/$productName.png"
        return storageRef.child(filePath)
    }


    /** 답글을 다는 코드 */
    fun readFireStoreData(): MutableLiveData<List<ReadTest>> {
        val resultLiveData = MutableLiveData<List<ReadTest>>(emptyList())
        val updateData = mutableListOf<ReadTest>()
        // questId 순으로 정렬
        fireStoreReadTest.get()
            .addOnCompleteListener { productTask: Task<QuerySnapshot> ->
                if (productTask.isSuccessful) {
                    Log.i(TAG, "fireStoreReadTest query success")
                    // 결과가 여러개일 경우, 마지막 것을 반환?
                    for (document in productTask.result) {
                        with(document.toObject(ReadTest::class.java)) {
                            updateData.add(this)
                            Log.w(TAG, "quest query\n$this")
                        }
                    }
                    resultLiveData.value = updateData
                } else {
                    Log.e(TAG, "questRef query failed★★★")
                    Log.e(TAG, productTask.exception?.message.toString())
                }
            }
        return resultLiveData
    }

    companion object {
        const val TAG = "ProductRepository"
    }
}

@Keep
data class DrinkInfo(
    var degree : Int = 0,
    var productName: String = "",
    var type : String = "",
    var volume : Int = 0,
)

@Keep
data class Product (
    var boardWriter: String = "",
    var content: String = "",
    var title: String = "",
    var uid: String = "",
    var replyCount : Int = 0,
    @ServerTimestamp
    var timestamp: Date? = null
)

data class ReadTest(
    var name : String = "",
    var price : Int = 0,
)

internal interface Constants {
    companion object {
        const val DATA = "productNameList"
        const val PRODUCT_SEARCH_PROPERTY = "drink_version_01"
        const val PRODUCT_NAMES = "productNames"
        const val PRODUCTS_COLLECTION = "drink_version_01"
        const val NAME_PROPERTY = "productName"
    }
}