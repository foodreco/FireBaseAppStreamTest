package com.dreamreco.firebaseappstreamtest.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentMainBinding
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.DrinkSearchDialog
import com.dreamreco.firebaseappstreamtest.util.LocalDataKey
import com.dreamreco.firebaseappstreamtest.util.MyDataStoreUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storageMetadata
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class MainFragment : Fragment(), OnFailureListener {

    companion object {
        const val TAG = "MainFragment"
        const val DB_NAME = "JooDiary.db"
    }

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    @Inject
    lateinit var myDataStoreUtil: MyDataStoreUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        mFirebaseAnalytics = Firebase.analytics
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            btnToList.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "btnToList")
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "btnToList")
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "리스트로이동")
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToListFragment())
            }
            btnToOnly.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "btnToOnly")
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "btnToOnly")
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "온니로이동")
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToOnlyFragment())
            }
            btnToAddList.setOnClickListener {
                listAdd()
            }

            // 길게 터치 시 테스트
            btnToAddList.setOnLongClickListener {
                testCachesOrData()

                return@setOnLongClickListener true
            }

            btnDelete.setOnClickListener {
                mainViewModel.deleteDataAll()
            }


            btnCustom1.setOnClickListener {
                Toast.makeText(requireContext(),"dataStore 저장",Toast.LENGTH_SHORT).show()
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.saveDataToDataStore(LocalDataKey.DATASTORE_TEST_KEY,"좋아")
                    myDataStoreUtil.saveDataToDataStore(LocalDataKey.DATASTORE_TEST_KEY2,1)
                    myDataStoreUtil.saveDataToDataStore(LocalDataKey.DATASTORE_TEST_KEY3,23451233551)
                    myDataStoreUtil.saveDataToDataStore(LocalDataKey.DATASTORE_TEST_KEY4,true)
                    myDataStoreUtil.saveDataToDataStore(LocalDataKey.DATASTORE_TEST_KEY5, listOf("hello","world!!"))
                }
            }
            btnCustom2.setOnClickListener {
                Toast.makeText(requireContext(),"dataStore 읽기",Toast.LENGTH_SHORT).show()
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.getStringDataFromDataStore(requireContext(),LocalDataKey.DATASTORE_TEST_KEY, "없음").collect { value ->
                        Log.e("DataStore 테스트","String : $value")
                    }}
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.getIntDataFromDataStore(requireContext(),LocalDataKey.DATASTORE_TEST_KEY2, 0).collect { value ->
                        Log.e("DataStore 테스트","Int : $value")
                    }}
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.getLongDataFromDataStore(requireContext(),LocalDataKey.DATASTORE_TEST_KEY3, 0).collect { value ->
                        Log.e("DataStore 테스트","Long : $value")
                    }}
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.getBooleanDataFromDataStore(requireContext(),LocalDataKey.DATASTORE_TEST_KEY4, null).collect { value ->
                        Log.e("DataStore 테스트","Boolean : $value")
                    }}
                lifecycleScope.launchWhenCreated {
                    myDataStoreUtil.getStringListDataFromDataStore(requireContext(),LocalDataKey.DATASTORE_TEST_KEY5).collect { value ->
                        Log.e("DataStore 테스트","List : $value")
                    }
                }
            }
            btnCustom3.setOnClickListener {
//                val bundle = Bundle()
//                bundle.putString("이름", "버튼")
//                bundle.putString("장소", "메인조각")
//                bundle.putString("이벤트", "btnCustom3")
//                mFirebaseAnalytics.logEvent("myCustomEvent", bundle)
            }

            // Firebase Message 토큰을 불러오는 코드
            btnGetToken.setOnClickListener {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    Log.d(TAG, "토큰값 : $token")
                    Toast.makeText(requireContext(), token, Toast.LENGTH_SHORT).show()
                    binding.textToken.setText(token)
                })
            }

            btnToFireStoreList.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToFireStoreListFragment())
            }

            btnToSearchDialog.setOnClickListener {
                val dialog = DrinkSearchDialog()
                dialog.show(childFragmentManager, "DrinkSearchDialog")
            }

            btnToFireStatistics.setOnClickListener {
                it.findNavController().navigate(MainFragmentDirections.actionMainFragmentToFireStoreStatisticsFragment())
            }

            btnToFTSTest.setOnClickListener {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToFTSFragment())
            }

            btnMoveToChart.setOnClickListener {
                it.findNavController().navigate(MainFragmentDirections.actionMainFragmentToRadarChartFragment())
            }

            // 로그아웃 작동
            btnLogOut.setOnClickListener {
                Log.e("메인조각", "로그아웃 터치 작동")
//                AuthUI.getInstance().signOut(requireContext())
                Firebase.auth.signOut()
                Toast.makeText(requireContext(), "로그아웃됨", Toast.LENGTH_SHORT).show()
            }

            btnLogIn.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToLoginFragment())
            }

            btnGetUser.setOnClickListener {

                val keyHash = Utility.getKeyHash(requireContext())
                Log.e("카카오 키해시", "키 해시 : $keyHash")

                val user = Firebase.auth.currentUser
                if (user != null) {
                    // User is signed in

                    user.let {
                        // Name, email address, and profile photo Url
                        val name = it.displayName
                        val email = it.email
                        val photoUrl = it.photoUrl

                        // Check if user's email is verified
                        val emailVerified = it.isEmailVerified

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getIdToken() instead.
                        val uid = it.uid

                        Log.e(
                            "메인조각",
                            "유저정보 \nname : $name\nemail : $email\nphotoUrl : $photoUrl\nemail확인 : $emailVerified\nuid:$uid"
                        )
                    }

                    user.let {
                        for (profile in it.providerData) {
                            // Id of the provider (ex: google.com)
                            val providerId = profile.providerId

                            // UID specific to the provider
                            val uid = profile.uid

                            // Name, email address, and profile photo Url
                            val name = profile.displayName
                            val email = profile.email
                            val photoUrl = profile.photoUrl

                            Log.e(
                                "메인조각",
                                "유저정보 #2 \nname : $name\nemail : $email\nphotoUrl : $photoUrl\nproviderId : $providerId\nuid:$uid"
                            )
                        }
                    }

                    Toast.makeText(requireContext(), "로그인 완료 : \n${user.uid}", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // No user is signed in
                    Toast.makeText(requireContext(), "사용자 없음", Toast.LENGTH_SHORT).show()
                }
            }

            /** 파일을 Storage에 백업하는 코드 */
            btnToBackUpToStorage.setOnClickListener {
                mainViewModel.dbCheck()
                mainViewModel.checkDone.observe(viewLifecycleOwner) { checkDone ->
                    if (checkDone) {
                        mainViewModel.checkDoneReset()
                        backUpToStorage(requireContext())
                    }
                }
            }
            /** 파일을 Storage에서 불러오는 코드 */
            btnToLoadFromStorage.setOnClickListener {
                loadFromStorage(requireContext())
            }

            /** 이미지를 Storage 에 올리는 코드 */
            btnToUploadImageToStorage.setOnClickListener {
                if (auth.currentUser != null) {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToStorageFragment())
                } else {
                    Toast.makeText(requireContext(), "로그인하세요.", Toast.LENGTH_SHORT).show()
                }
            }

            /** 게시판으로 이동하는 버튼 */
            btnMoveToBoard.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainFragment_to_questionAndAnswerFragment)
            }

            btnMoveToAnalyticsTest.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainFragment_to_analyticsFragment)
            }

            btnToMoveRealTime.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainFragment_to_realTimeFragment)
            }
        }

        return binding.root
    }


    private fun backUpToStorage(context: Context) {
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "로그인하세요.", Toast.LENGTH_SHORT).show()
        } else {
            // 로그인 된 상태에서만 작동
            try {
                // Create a storage reference from our app
                val storageRef = storage.reference

                // Create a reference to "mountains.jpg"
                val storagePath = "backupFiles"


                // uid 로 파일이름 설정
                val fileName = auth.currentUser!!.uid

                // 기존에 존재하던 파일 삭제
                storageRef.child("$storagePath/$fileName.db").delete()

                // 새로 저장
                val backupFileRef = storageRef.child("$storagePath/$fileName.db")

                val stream = requireContext().contentResolver.openInputStream(
                    context.getDatabasePath(DB_NAME).toUri()
                )

                val timeNow = System.currentTimeMillis()
                // 메타 데이터 추가
                var metadata = storageMetadata {
                    contentType = "database/db"
                }

                if (stream != null) {
                    val uploadTask = backupFileRef.putStream(stream, metadata)

                    uploadTask.addOnFailureListener { e ->
                        // Handle unsuccessful uploads
                        Log.e(TAG, "uploadTask 실패", e)
                        Toast.makeText(context,"백업 실패",Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener { taskSnapshot ->
                        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                        Log.e(TAG, "uploadTask 완료됨!")
                        Toast.makeText(context,"백업이 완료되었습니다.",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "backUpToStorage 실패 : Stream 을 찾을 수 없음")
                    Toast.makeText(context,"백업 실패",Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "backUpToStorage 실패", e)
                Toast.makeText(context,"백업 실패",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFromStorage(context: Context) {
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "로그인하세요.", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val storageRef = storage.reference

                // Create a reference to "mountains.jpg"
                val storagePath = "backupFiles"

                // uid 로 파일이름
                val fileName = auth.currentUser!!.uid

                val backupFileRef = storageRef.child("$storagePath/$fileName.db")

                val localFile = File.createTempFile("JooDiary_", ".db")

                backupFileRef.metadata.addOnSuccessListener { metadata ->

                    val data = Date(metadata.creationTimeMillis)

                    Log.e(TAG, "메타데이터 가져오기 성공!! data1 : $data")
                    Log.e(TAG, "메타데이터 가져오기 성공!! data2 : ${data.time.days}")
                    Log.e(TAG, "메타데이터 가져오기 성공!! data3 : ${data.time.hours}")
                    Log.e(TAG, "메타데이터 가져오기 성공!! data4 : ${data.time.minutes}")

                }

                backupFileRef.getFile(localFile).addOnSuccessListener {
                    Log.e(TAG, "getFile 성공!!")

                    // Local temp file has been created
                    val test = Uri.fromFile(localFile)

                    copyDataFromOneToAnother(
                        test,
                        context.getDatabasePath(DB_NAME).toUri()
                    )
                    Toast.makeText(requireContext(), "가져오기 완료", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener { e ->
                    Log.e(TAG, "getFile 실패", e)
                    onFailure(e)
                }

            } catch (e: Exception) {
                Log.e(TAG, "loadFromStorage 실패", e)
            }
        }
    }

    private fun copyDataFromOneToAnother(fromUri: Uri, toUri: Uri) {
        try {
            val inStream = requireContext().contentResolver.openInputStream(fromUri)
            val outStream = requireContext().contentResolver.openOutputStream(toUri)

            inStream.use { input ->
                outStream.use { output ->
                    if (output != null) {
                        input?.copyTo(output)
                    }
                }
            }

            Log.e(TAG, "copyDataFromOneToAnother 완료")

            deletePreviousDatabaseFile(requireContext(), fromUri)

        } catch (e: Exception) {
            Log.e(TAG, "copyDataFromOneToAnother 실패", e)
        }

    }

    /** DB 파일을 기존 DB에 import 후, 기존 shm, wal 파일을 삭제하는 함수 */
    private fun deletePreviousDatabaseFile(context: Context, deleteCacheFileUri : Uri) {
        val dbPath1 = Paths.get(context.getDatabasePath("$DB_NAME-shm").path)
        val dbPath2 = Paths.get(context.getDatabasePath("$DB_NAME-wal").path)

        try {
            val result1 = Files.deleteIfExists(dbPath1)
            val result2 = Files.deleteIfExists(dbPath2)

            // 임시 Cache 다운로드 DB 파일도 삭제
            val file = File(deleteCacheFileUri.path!!)
            file.delete()

            if (result1) {
                Log.e(TAG, "shm 삭제완료")
            } else {
                Log.e(TAG, "shm 삭제실패")
            }

            if (result2) {
                Log.e(TAG, "wal 삭제완료")
            } else {
                Log.e(TAG, "wal 삭제실패")
            }

            restartApp2()

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "에러발생 : $e ")
        }
    }

    // 데이터 가져오기 후 재시작 #2 최신? 버전
    private fun restartApp2() {

        Toast.makeText(requireContext(), "앱을 재실행합니다.", Toast.LENGTH_SHORT).show()

        val packageManager = requireContext().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        requireContext().startActivity(mainIntent)
//        Runtime.getRuntime().exit(0)

        // TODO:프로세스를 종료해야, database reset 됨?
        exitProcess(0)
    }


    private fun testCachesOrData() {
        val result = MyApplication.prefs.getString("testCachesOrData", "초기화됨")
        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
    }

    // 리스트 신규 추가 코드
    private fun listAdd() {

        MyApplication.prefs.setString("testCachesOrData", "세팅됨")

        mainViewModel.insertDiaryBase()
        mainViewModel.insertOnlyBasic()
        sendFirebaseLog("MainFragment", "listAdd", "리스트추가")
    }

    /** FireBase 에 로그를 보내는 함수 (데이터 저장 또는 업데이트 시 발동) */
    private fun sendFirebaseLog(itemID: String, itemName: String, contentType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemID)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

        Log.e(TAG, "fireBase 로그 전송")
    }

    override fun onResume() {
        super.onResume()
        val screenViewBundle = Bundle()
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "메인화면")
        screenViewBundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainFragment")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenViewBundle)
    }

    // Storage error 코드 처리 리스너
    override fun onFailure(exception: Exception) {
        val errorCode = (exception as StorageException).errorCode
        val errorMessage = exception.message
        // test the errorCode and errorMessage, and handle accordingly
        when (errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND -> Log.e(TAG, "파일을 찾지 못함 : $errorMessage")
            else -> Log.e(TAG, "다른 이유로 실패")
        }
    }
}