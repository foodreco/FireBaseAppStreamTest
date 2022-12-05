package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.content.Context
import android.text.TextUtils
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.WineUtil
import com.google.firebase.firestore.Query

/**
 * Object for passing filters around.
 */
class Filters {

    var category: String? = null
    var country: String? = null
    var price = -1
    var sortBy: String? = null
    var sortDirection: Query.Direction = Query.Direction.DESCENDING

    fun hasCategory(): Boolean {
        return !TextUtils.isEmpty(category)
    }

    fun hasCity(): Boolean {
        return !TextUtils.isEmpty(country)
    }

    fun hasPrice(): Boolean {
        return price > 0
    }

    fun hasSortBy(): Boolean {
        return !TextUtils.isEmpty(sortBy)
    }

    /** 필터 결과를 html 텍스트로 정리하는 코드 */
    fun getSearchDescription(context: Context): String {
        val desc = StringBuilder()

        if (category == null && country == null) {
            desc.append("<b>")
            desc.append(context.getString(R.string.all_restaurants))
            desc.append("</b>")
        }

        if (category != null) {
            desc.append("<b>")
            desc.append(category)
            desc.append("</b>")
        }

        if (category != null && country != null) {
            desc.append(" in ")
        }

        if (country != null) {
            desc.append("<b>")
            desc.append(country)
            desc.append("</b>")
        }

        if (price > 0) {
            desc.append(" for ")
            desc.append("<b>")
            desc.append(WineUtil.getPriceString(price))
            desc.append("</b>")
        }

        return desc.toString()
    }

    /** 필터 결과를 string 텍스트로 정리하는 코드 */
    fun getOrderDescription(context: Context): String {
        return when (sortBy) {
            Wine.FIELD_PRICE -> context.getString(R.string.sorted_by_price)
            Wine.FIELD_POPULARITY -> context.getString(R.string.sorted_by_popularity)
            else -> context.getString(R.string.sorted_by_rating)
        }
    }

    companion object {

        val default: Filters
            get() {
                val filters = Filters()
                filters.sortBy = Wine.FIELD_COUNTRY
                filters.sortDirection = Query.Direction.DESCENDING

                return filters
            }
    }
}
