package com.dreamreco.firebaseappstreamtest.room.dao

import android.graphics.Bitmap
import androidx.room.*
import android.net.Uri
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBaseAlpha
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryBaseAlphaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryBaseAlpha: DiaryBaseAlpha)

    @Delete
    suspend fun delete(diaryBaseAlpha: DiaryBaseAlpha)

    @Update
    suspend fun update(diaryBaseAlpha: DiaryBaseAlpha)

    @Query("DELETE FROM diary_base_alpha")
    suspend fun clear()

    @Query("DELETE FROM diary_base_alpha WHERE calendarDay = :calendarDay AND title =:title")
    suspend fun deleteByTitleAndDate(calendarDay: CalendarDay, title: String)

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base_alpha WHERE title = :title ORDER BY title ASC LIMIT 1")
    suspend fun checkTitle(title: String): String?

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base_alpha WHERE calendarDay = :calendarDay AND title =:title ORDER BY dateForSort ASC LIMIT 1")
    suspend fun checkTitleAndDate(calendarDay: CalendarDay, title: String): String?

    // 특정 DiaryBase 를 가져오는 함수
    @Query("SELECT importance FROM diary_base_alpha WHERE calendarDay = :calendarDay AND title =:title LIMIT 1")
    suspend fun checkDiaryBaseImportanceByDateAndTitle(calendarDay: CalendarDay, title: String) : Boolean

    // myDrink 가 존재하는 날짜 중 가장 오래된 날짜
    @Query("SELECT dateForSort FROM diary_base_alpha ORDER BY dateForSort ASC LIMIT 1")
    suspend fun getMyDrinkStartDate(): Int

    // myDrink 가 존재하는 날짜 중 가장 오래된 날짜
    @Query("SELECT dateForSort FROM diary_base_alpha ORDER BY dateForSort DESC LIMIT 1")
    suspend fun getMyDrinkRecentDate(): Int





    // 모든 DiaryBase 를 날짜 오름차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base_alpha ORDER BY dateForSort ASC")
    fun getAllDiaryBaseByDateASC(): Flow<List<DiaryBaseAlpha>>

    // 모든 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base_alpha ORDER BY dateForSort DESC")
    fun getAllDiaryBaseByDateDESC(): Flow<List<DiaryBaseAlpha>>

    // 중요 표시된 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base_alpha WHERE importance = :important ORDER BY dateForSort DESC")
    fun getDiaryBaseByImportance(important : Boolean = true): Flow<List<DiaryBaseAlpha>>

    // 리스트 Fragment 검색 전용 #2
    // 모든 데이터를 가져오는 함수
    @Query("SELECT * FROM diary_base_alpha ORDER BY dateForSort ASC")
    fun getAllDiaryBase(): Flow<List<DiaryBaseAlpha>>

}