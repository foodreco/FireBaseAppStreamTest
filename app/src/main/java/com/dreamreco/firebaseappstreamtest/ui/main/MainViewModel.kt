package com.dreamreco.firebaseappstreamtest.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
}