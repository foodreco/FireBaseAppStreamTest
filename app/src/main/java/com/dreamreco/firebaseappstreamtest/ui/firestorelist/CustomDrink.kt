package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class CustomDrink(
    var productName: String? = null,
    var degree: Int? = null,
    var type: String? = null,
    var volume: Int? = null
) {
    companion object {
        const val FIELD_NAME = "productName"
        const val FIELD_DEGREE = "degree"
        const val FIELD_TYPE = "type"
        const val FIELD_VOLUME = "volume"
    }
}