package com.dreamreco.firebaseappstreamtest.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseAlphaDao
import com.dreamreco.firebaseappstreamtest.room.dao.KeywordRoomLiveDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBaseAlpha
import com.dreamreco.firebaseappstreamtest.room.entity.KeywordRoomLive
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic


//@Database(
//    entities = [DiaryBase::class, OnlyBasic::class],
//    version = 1,
//    exportSchema = true
//)
//@TypeConverters(value = [Converters::class, MyDateTypeConverter::class, MyDrinkTypeConverter::class, CalendarDayTypeConverter::class])
//abstract class Database : RoomDatabase() {
//    abstract val diaryDao: DiaryBaseDao
//    abstract val onlyBasicDao : OnlyBasicDao
//}

//@Database(
//    entities = [DiaryBase::class, DiaryBaseAlpha::class, OnlyBasic::class, KeywordRoomLive::class],
//    version = 2,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
//)
//@TypeConverters(value = [Converters::class, MyDateTypeConverter::class, MyDrinkTypeConverter::class, CalendarDayTypeConverter::class])
//abstract class Database : RoomDatabase() {
//    abstract val diaryDao: DiaryBaseDao
//    abstract val diaryAlphaDao: DiaryBaseAlphaDao
//    abstract val onlyBasicDao: OnlyBasicDao
//    abstract val keywordRoomLiveDao: KeywordRoomLiveDao
//}

@Database(
    entities = [DiaryBaseAlpha::class, OnlyBasic::class, KeywordRoomLive::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = DataBaseModule.MyAutoMigration::class)
    ]
)
@TypeConverters(value = [Converters::class, MyDateTypeConverter::class, MyDrinkTypeConverter::class, CalendarDayTypeConverter::class])
abstract class Database : RoomDatabase() {
//    abstract val diaryDao: DiaryBaseDao
    abstract val diaryAlphaDao: DiaryBaseAlphaDao
    abstract val onlyBasicDao: OnlyBasicDao
    abstract val keywordRoomLiveDao: KeywordRoomLiveDao
}