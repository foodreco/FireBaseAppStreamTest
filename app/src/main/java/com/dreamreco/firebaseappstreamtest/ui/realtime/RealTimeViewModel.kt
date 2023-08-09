package com.dreamreco.firebaseappstreamtest.ui.realtime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RealTimeViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

}