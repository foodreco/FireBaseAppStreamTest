package com.dreamreco.firebaseappstreamtest.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.dreamreco.firebaseappstreamtest.toDateInt
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    private val databaseOnlyBasic: OnlyBasicDao,
    application: Application
) : AndroidViewModel(application) {


    fun insertDiaryBase() {
        viewModelScope.launch {
            val newMyDate = MyDate((2020..2022).random(), (1..12).random(), (1..31).random())
            val newCalendarDay = CalendarDay.from(newMyDate.year, newMyDate.month-1, newMyDate.day)
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
            val insertDiaryBase = DiaryBase(
                null,
                newMyDate,
                newCalendarDay,
                getRandomString(8),
                getRandomString(20),
                newMyDrink,
                newImportance,
                newCalendarDay.toDateInt(),
                null,
                newKeywords,
                0
            )
            database.insert(insertDiaryBase)
        }
    }

    fun insertOnlyBasic() {
        viewModelScope.launch {
            val newMyDate = MyDate((2020..2022).random(), (1..12).random(), (1..31).random())
            val newCalendarDay = CalendarDay.from(newMyDate.year, newMyDate.month-1, newMyDate.day)
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


    fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }

}