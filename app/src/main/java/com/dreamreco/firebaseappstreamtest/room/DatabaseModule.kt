package com.dreamreco.firebaseappstreamtest.room

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    private const val DB_NAME = "JooDiary.db"

    // entity 에 data class 를 넣기 위해 필요
    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideDatabase (@ApplicationContext context: Context, gson: Gson): Database {
        return Room
            .databaseBuilder(context, Database::class.java, DB_NAME)
            .addTypeConverter(MyDateTypeConverter(gson)) // 'MyDate' converter
            .addTypeConverter(MyDrinkTypeConverter(gson)) // 'MyDrink' converter
            .addTypeConverter(CalendarDayTypeConverter(gson)) // 'CalendarDay' converter
            .build()
    }

    @Singleton
    @Provides
    fun provideDao(database: Database) = database.diaryDao

    @Singleton
    @Provides
    fun provideOnlyBasicDao(database: Database) = database.onlyBasicDao
//
//    @Singleton
//    @Provides
//    fun provideCalendarDateDao(database: Database) = database.calendarDateDao
//
//    @Singleton
//    @Provides
//    fun provideLoadImageSignalDao(database: Database) = database.loadImageSignalDao
//
//    @Singleton
//    @Provides
//    fun provideMyDrinkRoomDao(database: Database) = database.myDrinkRoomDao
//
//    @Singleton
//    @Provides
//    fun provideQuestDao(database: Database) = database.questDao
//
//    @Singleton
//    @Provides
//    fun provideAllKeywordDao(database: Database) = database.keywordRoomDao
//
//    @Singleton
//    @Provides
//    fun provideLiveKeywordDao(database: Database) = database.keywordRoomLiveDao

}