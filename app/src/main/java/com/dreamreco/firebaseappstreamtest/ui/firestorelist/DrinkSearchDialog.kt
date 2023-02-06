package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import com.dreamreco.firebaseappstreamtest.databinding.DialogDrinkSearchBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


//TODO : FireStore FTS 구현하기
class DrinkSearchDialog : DialogFragment(), WineAdapter.OnRestaurantSelectedListener,
    EventListener<QuerySnapshot> {

    private val binding by lazy { DialogDrinkSearchBinding.inflate(layoutInflater) }
    private var mAdapter: WineAdapter? = null
    private var query: Query? = null
    private lateinit var fireStore: FirebaseFirestore
    private var drinkSearchListener: DrinkSearchListener? = null
    private var searchText: String = ""


    private var deviceWidth : Int = 30
    private var deviceHeight : Int = 30

    private var mCollectionName : String? = null
    private val snapshots = ArrayList<DocumentSnapshot>()


    private var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(newText: String): Boolean {
                return if (newText != "") {
                    searchText = newText
                    setNewQuery(newText)
                    true
                } else {
                    Toast.makeText(requireContext(),"검색어를 입력하세요.",Toast.LENGTH_SHORT).show()
                    false
                }
            }

            //텍스트 입력/수정시에 호출 : 사용안함
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }


    interface DrinkSearchListener {
        fun onSearchDrink(wine: CustomDrink)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 부모 조각에 FilterListener 로 붙인다!
        if (parentFragment is DrinkSearchListener) {
            drinkSearchListener = parentFragment as DrinkSearchListener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireStore = Firebase.firestore
        binding.searchView.setOnQueryTextListener(searchViewTextListener)

        // 쿼리 검색용 단어를 불러오는 코드
        fireStore.collection("collectionname").addSnapshotListener(this)


        // 꼭 DialogFragment 클래스에서 선언하지 않아도 된다.
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        deviceWidth = size.x // 디바이스 가로 길이
        deviceHeight = size.y // 디바이스 세로 길이
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        // 50개 와인 목록 얻기 : 일부러 0 개의 목록 반환하기?
//        query = firestore.collection("wine")
//            .orderBy("name", Query.Direction.ASCENDING)
//            .limit(50.toLong())

        Log.e("DrinkSearchDialog","query1 : $query")


        query.let {
            mAdapter = object : WineAdapter(it, this@DrinkSearchDialog) {
                override fun onError(e: FirebaseFirestoreException) {
                    Snackbar.make(
                        binding.root,
                        "에러발생 : $e.", Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun onDataChanged() {
                    if (itemCount == 0) {
                        Toast.makeText(context, "리스트가 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "리스트 불러옴", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.drinkSearchListRecyclerView.adapter = mAdapter


        return binding.root
    }


    fun setNewQuery(text: String) {
        if (mCollectionName != null) {

            var filterQuery: Query = fireStore.collection(mCollectionName!!)

            //TODO : 쿼리 필터링 하기
            filterQuery = filterQuery.whereGreaterThanOrEqualTo(CustomDrink.FIELD_NAME, text)
            filterQuery = filterQuery.limit(50.toLong())

            // 쿼리 반영하기
            mAdapter?.setQuery(filterQuery)

            binding.resultText.text = text
        } else {
            Toast.makeText(context,"불러올 데이터가 없습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRestaurantSelected(wine: CustomDrink) {
        drinkSearchListener?.onSearchDrink(wine)
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (deviceWidth * 0.9).toInt()
        params?.height = (deviceHeight * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onStart() {
        super.onStart()

        // Start listening for FireStore updates
        mAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }


    // Query CollectionName 설정을 위한 eventListener
    override fun onEvent(documentSnapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {

        Log.e("DrinkSearchDialog", "컬렉션 명 불러오기 : onEvent")


        // 에러 발생 시,
        if (error != null) {
            Log.e("DrinkSearchDialog", "onEvent:error")
            return
        }

        // Dispatch the event
        if (documentSnapshots != null) {
            for (change in documentSnapshots.documentChanges) {
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
        Log.e("DrinkSearchDialog", "컬렉션 명 불러오기 : ADDED")
        snapshots.add(change.newIndex, change.document)
        Log.e("DrinkSearchDialog", "snapshots[0] : ${snapshots[0]}")
        Log.e("DrinkSearchDialog", "snapshots[0].data : ${snapshots[0].data}")
        Log.e("DrinkSearchDialog", "snapshots[0].data?.values : ${snapshots[0].data?.values}")

//        val collectionName = change.document.toObject<TestClass>()
        val collectionName = snapshots[0].data?.values
        val test = snapshots[0].toObject<TestClass>()
        if (collectionName != null) {
            for (each in collectionName) {
                mCollectionName = each.toString()
               Log.e("DrinkSearchDialog", "each : $each")
            }
        }
        Log.e("DrinkSearchDialog", "collectionName : $collectionName")
        Log.e("DrinkSearchDialog", "test : $test")

        val testName = test?.collectionName
        Log.e("DrinkSearchDialog", "testName : $testName")

    }

    private fun onDocumentModified(change: DocumentChange) {
        Log.e("DrinkSearchDialog", "컬렉션 명 불러오기 : MODIFIED")
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
        }

        val collectionName = snapshots[0].data?.values
        val test = snapshots[0].toObject<TestClass>()
        if (collectionName != null) {
            for (each in collectionName) {
                mCollectionName = each.toString()
                Log.e("DrinkSearchDialog", "each : $each")
            }
        }
        Log.e("DrinkSearchDialog", "collectionName : $collectionName")
        Log.e("DrinkSearchDialog", "test : $test")

        val testName = test?.collectionName
        Log.e("DrinkSearchDialog", "testName : $testName")

        // DB Collection Name 변경 시, 불러오기 재 설정 코드
        val filterQuery: Query = fireStore.collection(mCollectionName!!)
        mAdapter?.setQuery(filterQuery)
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        Log.e("DrinkSearchDialog", "컬렉션 명 불러오기 : REMOVED")
        snapshots.removeAt(change.oldIndex)
    }
}

@Keep
@IgnoreExtraProperties
data class TestClass(
    var collectionName : String? = null
)