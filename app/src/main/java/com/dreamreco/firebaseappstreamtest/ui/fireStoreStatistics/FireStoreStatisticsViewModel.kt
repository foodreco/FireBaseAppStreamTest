package com.dreamreco.firebaseappstreamtest.ui.fireStoreStatistics

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.repository.CustomRepository
import com.dreamreco.firebaseappstreamtest.repository.FireBaseRepository
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FireStoreStatisticsViewModel @Inject constructor(
    private val fireBaseRepository: FireBaseRepository,
    private val roomRepository: CustomRepository,
    application: Application
) : AndroidViewModel(application) {

    fun updateRecord(record: StatisticsRecord) {
        fireBaseRepository.updateRecord(record)
    }

    fun getMyRank() : Task<FireBaseRepository.MyOnLineRank> {
        return fireBaseRepository.getMyRank()
    }

    // TODO : 테스트 기능 삭제할 것
    fun resetPref() {
        MyApplication.prefs.removePref("numberOfAlcohols")
        MyApplication.prefs.removePref("numberOfNames")
        MyApplication.prefs.removePref("numberOfRecords")
        MyApplication.prefs.removePref("numberOfTypes")
        MyApplication.prefs.removePref("numberOfVolume")
    }

    val readData = fireBaseRepository.readRecord()

}