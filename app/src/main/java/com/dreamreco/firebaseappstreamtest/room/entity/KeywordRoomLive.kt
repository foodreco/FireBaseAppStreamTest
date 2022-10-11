package com.dreamreco.firebaseappstreamtest.room.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "keywordsRoomLive")
data class KeywordRoomLive(
    var keywords: List<String>?,
    @PrimaryKey(autoGenerate = false)
    var id : Int = 0
) : Parcelable


