package com.dreamreco.firebaseappstreamtest.repository

import com.dreamreco.firebaseappstreamtest.room.Database
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.dreamreco.firebaseappstreamtest.util.MyDate
import com.dreamreco.firebaseappstreamtest.util.MyDrink
import com.dreamreco.firebaseappstreamtest.util.toDateInt
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CustomRepository @Inject constructor(private val database: Database) {

    suspend fun insertDiaryBase() {
        withContext(Dispatchers.IO) {
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
            database.diaryDao.insert(insertDiaryBase)
        }
    }


    suspend fun insertOnlyBasic() {
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
            database.onlyBasicDao.insert(insertOnlyBasic)
    }

    fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }

    suspend fun deleteRoomData() {
        database.diaryDao.clear()
        database.onlyBasicDao.clear()
    }

}