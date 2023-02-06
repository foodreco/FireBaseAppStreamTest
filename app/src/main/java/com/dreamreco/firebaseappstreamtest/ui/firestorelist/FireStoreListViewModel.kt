package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch


class FireStoreListViewModel : ViewModel() {

    var filters: Filters = Filters.default

    private var productRepository = ProductRepository()

    fun getProductNameListLiveData(): MutableLiveData<List<String>> {
        return productRepository.productNameListMutableLiveData
    }

    fun getDrinkInfoLiveData(productName: String?): LiveData<DrinkInfo?> {
        return productRepository.getDrinkInformationMutableLiveData(productName)
    }

    fun getDrinkInfoImage(type:String, productName: String): LiveData<String?> {
        return productRepository.getDrinkImageMutableLiveData(type, productName)
    }
}