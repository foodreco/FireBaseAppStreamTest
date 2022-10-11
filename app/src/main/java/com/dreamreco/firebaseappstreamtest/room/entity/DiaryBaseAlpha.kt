package com.dreamreco.firebaseappstreamtest.room.entity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "diary_base_alpha")
data class DiaryBaseAlpha(
    var image : Uri?,
    var year : Int,
    var month : Int,
    var day : Int,
    var calendarDay : CalendarDay,
    var title : String,
    var content : String?,
    var drinkType: String?,
    var drinkName: String?,
    var POA: String?,
    var VOD: String?,
    var importance : Boolean = false,
    var dateForSort : Int = 0,
    var bitmapForRecyclerView : Bitmap?,
    @ColumnInfo(name = "keywords")
    var keywords : String?,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
) : Parcelable



