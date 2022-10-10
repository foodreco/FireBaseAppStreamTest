package com.dreamreco.firebaseappstreamtest.ui.list

import android.app.Application
import androidx.lifecycle.*
import com.dreamreco.firebaseappstreamtest.MyMonth
import com.dreamreco.firebaseappstreamtest.room.dao.DiaryBaseDao
import com.dreamreco.firebaseappstreamtest.room.dao.OnlyBasicDao
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.room.entity.OnlyBasic
import com.dreamreco.firebaseappstreamtest.toMyMonth
import com.dreamreco.firebaseappstreamtest.ui.list2.OnlyFragmentAdapterBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    private val databaseOnly:OnlyBasicDao,
    application: Application
) : AndroidViewModel(application) {

    private val _listFragmentDiaryData = MutableLiveData<List<ListFragmentAdapterBase>>()
    val listFragmentDiaryData: LiveData<List<ListFragmentAdapterBase>> = _listFragmentDiaryData

    private val _onlyFragmentDiaryData = MutableLiveData<List<OnlyFragmentAdapterBase>>()
    val onlyFragmentDiaryData: LiveData<List<OnlyFragmentAdapterBase>> = _onlyFragmentDiaryData

    fun getAllDataDESC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllOnlyDataDESC(): LiveData<List<OnlyBasic>> {
        return databaseOnly.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllDataASC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateASC().asLiveData()
    }

    fun getDiaryDataImportant(): LiveData<List<DiaryBase>> {
        return database.getDiaryBaseByImportance().asLiveData()
    }

    fun getOnlyDataImportant(): LiveData<List<OnlyBasic>> {
        return databaseOnly.getDiaryBaseByImportance().asLiveData()
    }

    fun makeList(diaryData: List<DiaryBase>) {
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
    private fun List<DiaryBase>.toListItems(): List<ListFragmentAdapterBase> {
        val result = arrayListOf<ListFragmentAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<DiaryBase>()) {
            result.add(ListFragmentAdapterBase.EmptyHeader())
        } else {
            var headerMonth = MyMonth(0, 0) // 기준
            this.forEach { diaryBase ->
                // month 가 달라지면 그룹헤더를 추가.
                if (headerMonth != diaryBase.calendarDay.toMyMonth()) {
                    result.add(ListFragmentAdapterBase.DateHeader(diaryBase))
                }

                // 그때의 item 추가.
                result.add(ListFragmentAdapterBase.Item(diaryBase))

                // 그룹날짜를 바로 이전 날짜로 설정.
                headerMonth = diaryBase.calendarDay.toMyMonth()
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

    fun getAllDiaryBase(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBase().asLiveData()
    }

    /** list 검색 시 주종, 제품, 도수, 키워드 항목을 검색하는 코드 */
    fun filtering(searchQuery: String, totalList: List<DiaryBase>) {
        viewModelScope.launch {
            if (totalList == emptyList<DiaryBase>()) {
                makeList(totalList)
            } else {
                val resultList = mutableListOf<DiaryBase>()
                for (each in totalList) {
                    if ((each.myDrink?.drinkType?.contains(searchQuery) == true) || (each.myDrink?.drinkName?.contains(
                            searchQuery
                        ) == true) || (each.myDrink?.POA?.contains(searchQuery) == true) ||
                        (each.keywords?.contains(searchQuery) == true)
                    ) {
                        resultList.add(each)
                    }
                }
                makeList(resultList)
            }
        }
    }
}