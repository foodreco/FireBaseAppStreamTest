package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */

@Keep
@IgnoreExtraProperties
data class Wine(
    var name: String? = null,
    var country: String? = null,
    var category: String? = null,
    var photo: String? = null,
    var price: Int = 0,
    var numRatings: Int = 0,
    var avgRating: Double = 0.toDouble()
) {

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_COUNTRY = "country"
        const val FIELD_CATEGORY = "category"
        const val FIELD_PRICE = "price"
        const val FIELD_POPULARITY = "numRatings"
        const val FIELD_AVG_RATING = "avgRating"
    }
}
