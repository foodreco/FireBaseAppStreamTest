package com.dreamreco.firebaseappstreamtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule.provideAlphaDao
import com.dreamreco.firebaseappstreamtest.ui.main.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule.provideDao
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule.provideDatabase
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule.provideGson
import com.dreamreco.firebaseappstreamtest.room.DataBaseModule.provideOnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBaseAlpha
import com.google.gson.Gson

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (MyApplication.prefs.getString(
                ROOM_DIARYBASE_MIGRATION_STATE,
                MIGRATION_NONE
            ) == MIGRATION_NONE
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val allDiaryBaseData = provideDao(
                    provideDatabase(
                        applicationContext,
                        provideGson()
                    )
                ).getAllDiaryBaseByDateDESCForBackUp()
                for (each in allDiaryBaseData) {
                    if (each.myDrink != null) {
                        val newDiaryBaseAlpha = DiaryBaseAlpha(
                            each.image,
                            each.date.year,
                            each.date.month,
                            each.date.day,
                            each.calendarDay,
                            each.title,
                            each.content,
                            each.myDrink!!.drinkType,
                            each.myDrink!!.drinkName,
                            each.myDrink!!.POA,
                            each.myDrink!!.VOD,
                            each.importance,
                            each.dateForSort,
                            each.bitmapForRecyclerView,
                            each.keywords,
                            0
                        )
                        provideAlphaDao(provideDatabase(applicationContext, provideGson())).insert(
                            newDiaryBaseAlpha
                        )
                    } else {
                        val newDiaryBaseAlpha = DiaryBaseAlpha(
                            each.image,
                            each.date.year,
                            each.date.month,
                            each.date.day,
                            each.calendarDay,
                            each.title,
                            each.content,
                            null,
                            null,
                            null,
                            null,
                            each.importance,
                            each.dateForSort,
                            each.bitmapForRecyclerView,
                            each.keywords,
                            0
                        )
                        provideAlphaDao(provideDatabase(applicationContext, provideGson())).insert(
                            newDiaryBaseAlpha
                        )
                    }
                }
                MyApplication.prefs.setString(ROOM_DIARYBASE_MIGRATION_STATE, MIGRATION_DONE)
            }
        }
    }
}

const val ROOM_DIARYBASE_MIGRATION_STATE = "room_diaryBase_migration_state"
const val MIGRATION_DONE = "migration_done"
const val MIGRATION_NONE = "migration_none"