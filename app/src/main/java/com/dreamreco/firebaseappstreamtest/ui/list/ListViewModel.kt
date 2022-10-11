package com.dreamreco.firebaseappstreamtest.ui.list

import android.app.Application
import androidx.lifecycle.*
import com.dreamreco.firebaseappstreamtest.MyMonth
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseAlphaDao
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.dao.KeywordRoomLiveDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBaseAlpha
import com.dreamreco.firebaseappstreamtest.room.entity.KeywordRoomLive
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.dreamreco.firebaseappstreamtest.toMyMonth
import com.dreamreco.firebaseappstreamtest.ui.list2.OnlyFragmentAdapterBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: DiaryBaseAlphaDao,
    private val databaseOnly: OnlyBasicDao,
    private val databaseKeyword: KeywordRoomLiveDao,
    application: Application
) : AndroidViewModel(application) {

    private val _listFragmentDiaryData = MutableLiveData<List<ListFragmentAdapterBase>>()
    val listFragmentDiaryData: LiveData<List<ListFragmentAdapterBase>> = _listFragmentDiaryData

    private val _onlyFragmentDiaryData = MutableLiveData<List<OnlyFragmentAdapterBase>>()
    val onlyFragmentDiaryData: LiveData<List<OnlyFragmentAdapterBase>> = _onlyFragmentDiaryData

    fun getAllDataDESC(): LiveData<List<DiaryBaseAlpha>> {
        return database.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllOnlyDataDESC(): LiveData<List<OnlyBasic>> {
        return databaseOnly.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllDataASC(): LiveData<List<DiaryBaseAlpha>> {
        return database.getAllDiaryBaseByDateASC().asLiveData()
    }

    fun getDiaryDataImportant(): LiveData<List<DiaryBaseAlpha>> {
        return database.getDiaryBaseByImportance().asLiveData()
    }

    fun getOnlyDataImportant(): LiveData<List<OnlyBasic>> {
        return databaseOnly.getDiaryBaseByImportance().asLiveData()
    }

    fun makeList(diaryData: List<DiaryBaseAlpha>) {
        viewModelScope.launch {
            val listItems = diaryData.toListItems()
            _listFragmentDiaryData.postValue(listItems)
        }
    }

    fun makeOnlyList(diaryData: List<OnlyBasic>) {
        viewModelScope.launch {
            val listItems = diaryData.toOnlyListItems()
            _onlyFragmentDiaryData.postValue(listItems)
        }
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<DiaryBaseAlpha>.toListItems(): List<ListFragmentAdapterBase> {
        val result = arrayListOf<ListFragmentAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<DiaryBaseAlpha>()) {
            result.add(ListFragmentAdapterBase.EmptyHeader())
        } else {
            var headerMonth = MyMonth(0, 0) // 기준
            this.forEach { DiaryBaseAlpha ->
                // month 가 달라지면 그룹헤더를 추가.
                if (headerMonth != DiaryBaseAlpha.calendarDay.toMyMonth()) {
                    result.add(ListFragmentAdapterBase.DateHeader(DiaryBaseAlpha))
                }

                // 그때의 item 추가.
                result.add(ListFragmentAdapterBase.Item(DiaryBaseAlpha))

                // 그룹날짜를 바로 이전 날짜로 설정.
                headerMonth = DiaryBaseAlpha.calendarDay.toMyMonth()
            }
        }
        return result
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<OnlyBasic>.toOnlyListItems(): List<OnlyFragmentAdapterBase> {
        val result = arrayListOf<OnlyFragmentAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<OnlyBasic>()) {
            result.add(OnlyFragmentAdapterBase.EmptyHeader())
        } else {
            var headerMonth = MyMonth(0, 0) // 기준
            this.forEach { onlyBasic ->
                // month 가 달라지면 그룹헤더를 추가.
                if (headerMonth != onlyBasic.dateForSort.toMyMonth()) {
                    result.add(OnlyFragmentAdapterBase.DateHeader(onlyBasic))
                }

                // 그때의 item 추가.
                result.add(OnlyFragmentAdapterBase.Item(onlyBasic))

                // 그룹날짜를 바로 이전 날짜로 설정.
                headerMonth = onlyBasic.dateForSort.toMyMonth()
            }
        }
        return result
    }

    fun getAllDiaryBase(): LiveData<List<DiaryBaseAlpha>> {
        return database.getAllDiaryBase().asLiveData()
    }

    /** list 검색 시 주종, 제품, 도수, 키워드 항목을 검색하는 코드 */
    fun filtering(searchQuery: String, totalList: List<DiaryBaseAlpha>) {
        viewModelScope.launch {
            if (totalList == emptyList<DiaryBaseAlpha>()) {
                makeList(totalList)
            } else {
                val resultList = mutableListOf<DiaryBaseAlpha>()
                for (each in totalList) {
                    if ((each.drinkType?.contains(searchQuery) == true) || (each.drinkName?.contains(
                            searchQuery
                        ) == true) || (each.POA?.contains(searchQuery) == true) ||
                        (each.keywords?.contains(searchQuery) == true)
                    ) {
                        resultList.add(each)
                    }
                }
                makeList(resultList)
            }
        }
    }

    /** DiaryDetailDialog 에 표시할 MyDrinkRoom data 를 가져오는 코드 */
    fun getMyKeywordData(): LiveData<KeywordRoomLive?> { // List<String> 을 String 으로 인식하므로, KeywordRoomLive 통째로 가져온다.
        return databaseKeyword.getLiveKeywordsData().asLiveData()
    }

}