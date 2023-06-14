package com.dreamreco.firebaseappstreamtest.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.dreamreco.firebaseappstreamtest.repository.CustomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val mCustomRepository: CustomRepository,
    application: Application
) : AndroidViewModel(application) {

}