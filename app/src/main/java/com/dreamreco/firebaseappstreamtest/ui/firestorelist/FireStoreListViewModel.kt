package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FireStoreListViewModel @Inject constructor(
    private val productRepository: ProductRepository, application: Application
) : AndroidViewModel(application) {

    var filters: Filters = Filters.default

    fun getProductNameListLiveData(): MutableLiveData<List<String>> {
        return productRepository.productNameListMutableLiveData
    }

    fun getDrinkInfoLiveData(productName: String?): LiveData<DrinkInfo?> {
        return productRepository.getDrinkInformationMutableLiveData(productName)
    }

    fun getDrinkInfoImage(type: String, productName: String): LiveData<String?> {
        return productRepository.getDrinkImageMutableLiveData(type, productName)
    }

    fun getFireStoreData() : LiveData<List<ReadTest>> {
        return productRepository.readFireStoreData()
    }
}