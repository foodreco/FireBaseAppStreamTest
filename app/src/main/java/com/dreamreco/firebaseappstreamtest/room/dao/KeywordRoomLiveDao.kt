package com.dreamreco.firebaseappstreamtest.room.dao

import androidx.room.*
import com.dreamreco.firebaseappstreamtest.room.entity.KeywordRoomLive
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordRoomLiveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keywordRoomLive: KeywordRoomLive)

    @Delete
    suspend fun delete(keywordRoomLive: KeywordRoomLive)

    @Update
    suspend fun update(keywordRoomLive: KeywordRoomLive)

    @Query("DELETE FROM keywordsRoomLive")
    suspend fun clear()

    @Query("SELECT * FROM keywordsRoomLive ORDER BY id ASC LIMIT 1")
    fun getLiveKeywordsData(): Flow<KeywordRoomLive?>

}