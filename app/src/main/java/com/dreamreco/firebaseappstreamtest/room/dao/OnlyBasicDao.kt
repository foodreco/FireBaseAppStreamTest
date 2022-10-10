package com.dreamreco.firebaseappstreamtest.room.dao

import android.graphics.Bitmap
import androidx.room.*
import android.net.Uri
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.flow.Flow

@Dao
interface OnlyBasicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(onlyBasic: OnlyBasic)

    @Delete
    suspend fun delete(onlyBasic: OnlyBasic)

    @Update
    suspend fun update(onlyBasic: OnlyBasic)

    @Query("DELETE FROM onlyBasic")
    suspend fun clear()



    // 모든 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM onlyBasic ORDER BY dateForSort DESC")
    fun getAllDiaryBaseByDateDESC(): Flow<List<OnlyBasic>>

    // 중요 표시된 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM onlyBasic WHERE importance = :important ORDER BY dateForSort DESC")
    fun getDiaryBaseByImportance(important : Boolean = true): Flow<List<OnlyBasic>>

}