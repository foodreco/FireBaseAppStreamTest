package com.dreamreco.firebaseappstreamtest

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.RoomDatabase
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule
import com.dreamreco.firebaseappstreamtest.ui.login.LoginFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 이메일 답변 터치 시, 앱으로 다시 가져오는 코드
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->

                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                Log.e("메인엑티비티", "getDynamicLink 성공 deepLink : $deepLink")

                val auth = Firebase.auth
                val emailLink = intent.data.toString()

                // Confirm the link is a sign-in with email link.
                if (auth.isSignInWithEmailLink(emailLink)) {
                    // Retrieve this from wherever you stored it
                    val email = "huny357@naver.com"

                    Log.e("메인엑티비티", "인증 성공?? email : $email")
                    Log.e("메인엑티비티", "인증 성공?? emailLink : $emailLink")

                    if (Firebase.auth.currentUser != null) {
                        // 이미 FireBase 계정에 로그인 되어 있는 경우,
                        // 연결
                        val emailCredential =
                            EmailAuthProvider.getCredentialWithLink(email, emailLink)
                        Firebase.auth.currentUser!!.linkWithCredential(emailCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val result = task.result
                                    val user = result.user
                                    Log.d(TAG, "linkWithCredential : 병합 성공 ${user?.uid}")
                                } else {
                                    Log.w(TAG, "linkWithCredential: 병합 실패", task.exception)
                                }
                            }
                    } else {
                        // 새로 Firebase 계정 등록
                        // The client SDK will parse the code from the link for you.
                        auth.signInWithEmailLink(email, emailLink)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val result = task.result
                                    val user = result.user
                                    Log.d(TAG, "signInWithEmailLink : 성공 ${user?.uid}")
                                } else {
                                    Log.w(TAG, "signInWithEmailLink : 실패", task.exception)
                                }
                            }
                    }
                }
            }
            .addOnFailureListener(this) { e -> Log.e("메인엑티비티", "getDynamicLink:onFailure", e) }
    }

    companion object {
        const val TAG = "메인액티비티"
    }
}