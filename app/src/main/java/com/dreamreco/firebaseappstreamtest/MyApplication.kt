package com.dreamreco.firebaseappstreamtest

import android.app.Application
import android.util.Log
import com.dreamreco.firebaseappstreamtest.util.PreferenceUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var mFirebaseAnalytics : FirebaseAnalytics
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        mFirebaseAnalytics = Firebase.analytics
        super.onCreate()

        val keyHash = Utility.getKeyHash(this)
        Log.e("카카오키해시", keyHash)
        // Kakao SDK 초기화
        val nativeAppKey = this.applicationContext.getString(R.string.KakaoNativeAppKey)
        KakaoSdk.init(this, nativeAppKey)
    }
}

