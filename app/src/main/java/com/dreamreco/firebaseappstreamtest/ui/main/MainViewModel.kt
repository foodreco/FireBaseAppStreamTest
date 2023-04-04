package com.dreamreco.firebaseappstreamtest.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dreamreco.firebaseappstreamtest.repository.CustomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mCustomRepository:CustomRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _checkDone = MutableLiveData<Boolean>()
    val checkDone : LiveData<Boolean> = _checkDone


    fun insertDiaryBase() {
        viewModelScope.launch {
            mCustomRepository.insertDiaryBase()
        }
    }

    fun insertOnlyBasic() {
        viewModelScope.launch {
            mCustomRepository.insertOnlyBasic()
        }
    }

    fun deleteDataAll() {
        viewModelScope.launch {
            mCustomRepository.deleteRoomData()
        }
    }

    fun dbCheck() {
        viewModelScope.launch {
            mCustomRepository.dbCheck()
            _checkDone.postValue(true)
        }
    }

    fun checkDoneReset() {
        _checkDone.value = false
    }
}