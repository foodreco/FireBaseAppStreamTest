package com.dreamreco.firebaseappstreamtest.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseAlphaDao
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.dao.KeywordRoomLiveDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBaseAlpha
import com.dreamreco.firebaseappstreamtest.room.entity.KeywordRoomLive
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.dreamreco.firebaseappstreamtest.toDateInt
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    private val databaseAlpha: DiaryBaseAlphaDao,
    private val databaseOnlyBasic: OnlyBasicDao,
    private val databaseKeywordList: KeywordRoomLiveDao,
    application: Application
) : AndroidViewModel(application) {


    fun insertDiaryBase() {
        viewModelScope.launch {
//            val newMyDate = MyDate((2020..2022).random(), (1..12).random(), (1..31).random())
//            val newCalendarDay = CalendarDay.from(newMyDate.year, newMyDate.month, newMyDate.day)
//            val newMyDrink = MyDrink(
//                getRandomString(3),
//                getRandomString(4),
//                (5..30).random().toString(),
//                (100..500).random().toString()
//            )
//            var newImportance = false
//            if ((1..5).random() > 4) {
//                newImportance = true
//            }
//            val newKeywords = getRandomString(8)
//            val insertDiaryBase = DiaryBase(
//                null,
//                newMyDate,
//                newCalendarDay,
//                getRandomString(8),
//                getRandomString(20),
//                newMyDrink,
//                newImportance,
//                newCalendarDay.toDateInt(),
//                null,
//                newKeywords,
//                0
//            )
//            database.insert(insertDiaryBase)

            val newYear = (2020..2022).random()
            val newMonth = (1..12).random()
            val newDay = (1..31).random()
            val newCalendarDay = CalendarDay.from(newYear, newMonth - 1, newDay)
            val newDrinkType = getRandomString(3)
            val newDrinkName = getRandomString(4)
            val newPOA = (5..30).random().toString()
            val newVOD = (100..500).random().toString()
            var newImportance = false
            if ((1..5).random() > 3) {
                newImportance = true
            }
            val newKeywords = getRandomString(8)
            val insertDiaryBaseAlpha = DiaryBaseAlpha(
                null,
                newYear,
                newMonth,
                newDay,
                newCalendarDay,
                getRandomString(8),
                getRandomString(20),
                newDrinkType,
                newDrinkName,
                newPOA,
                newVOD,
                newImportance,
                newCalendarDay.toDateInt(),
                null,
                newKeywords,
                0
            )
            databaseAlpha.insert(insertDiaryBaseAlpha)
        }
    }

    fun insertOnlyBasic() {
        viewModelScope.launch {
            val newMyDate = MyDate((2020..2022).random(), (1..12).random(), (1..31).random())
            val newCalendarDay = CalendarDay.from(newMyDate.year, newMyDate.month, newMyDate.day)
            val newMyDrink = MyDrink(
                getRandomString(3),
                getRandomString(4),
                (5..30).random().toString(),
                (100..500).random().toString()
            )
            var newImportance = false
            if ((1..5).random() > 4) {
                newImportance = true
            }
            val newKeywords = getRandomString(8)

            val insertOnlyBasic = OnlyBasic(
                getRandomString(5),
                getRandomString(20),
                newImportance,
                newCalendarDay.toDateInt(),
                newKeywords,
                0
            )
            databaseOnlyBasic.insert(insertOnlyBasic)
        }
    }

    fun makeKeywordList() {
        viewModelScope.launch {
            val newListInt = (1..6).random()
            val newList = mutableListOf<String>()
            for (each in 0 until newListInt) {
                newList.add(getRandomString(4))
            }
            val newKeywordList = KeywordRoomLive(newList, 0)
            databaseKeywordList.insert(newKeywordList)
        }
    }


    fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }

}