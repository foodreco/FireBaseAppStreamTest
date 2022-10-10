package com.dreamreco.firebaseappstreamtest.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic


@Database(
    entities = [DiaryBase::class, OnlyBasic::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(value = [Converters::class, CalendarDayTypeConverter::class])
abstract class Database : RoomDatabase() {
    abstract val diaryDao: DiaryBaseDao
    abstract val onlyBasicDao : OnlyBasicDao
}

//@Database(
//    entities = [DiaryBase::class, OnlyBasic::class],
//    version = 2,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
//)
//@TypeConverters(value = [Converters::class, CalendarDayTypeConverter::class])
//abstract class Database : RoomDatabase() {
//    abstract val diaryDao: DiaryBaseDao
//    abstract val onlyBasicDao : OnlyBasicDao
//}

//
//@Database(
//    entities = [DiaryBase::class, CalendarDate::class, LoadImageSignal::class, MyDrinkRoom::class, Quest::class, KeywordRoomLive::class, KeywordRoom::class],
//    version = 2,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
//)
//@TypeConverters(value = [Converters::class, MyDateTypeConverter::class, MyDrinkTypeConverter::class, CalendarDayTypeConverter::class])
//abstract class Database : RoomDatabase() {
//
//    abstract val diaryDao: DiaryBaseDao
//    abstract val calendarDateDao: CalendarDateDao
//    abstract val loadImageSignalDao: LoadImageSignalDao
//    abstract val myDrinkRoomDao: MyDrinkRoomDao
//    abstract val questDao : QuestDao
//    abstract val keywordRoomDao : KeywordRoomDao
//    abstract val keywordRoomLiveDao : KeywordRoomLiveDao
//
//}