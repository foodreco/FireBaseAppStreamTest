package com.dreamreco.firebaseappstreamtest.ui.fireStoreStatistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.databinding.FragmentFireStoreStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FireStoreStatisticsFragment : Fragment() {

    companion object {
        const val TAG = "FireStoreStatisticsFragment"
    }

    private val viewModel by viewModels<FireStoreStatisticsViewModel>()
    private val binding by lazy { FragmentFireStoreStatisticsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            btnToUpdateRecord.setOnClickListener {
                updateRecord()
            }

            btnToRead.setOnClickListener {
                viewModel.readData.observe(viewLifecycleOwner) { list ->
                    Log.e(TAG, "list \n$list")
                }
            }

            btnToInit.setOnClickListener {
                viewModel.resetPref()
            }

            btnToInitRank.setOnClickListener {
                getRankAndPrint()
            }
        }

        return binding.root
    }

    private fun updateRecord() {
        with(binding) {
            val names = namesNumber.text.toString().toLong()
            val types = typesNumber.text.toString().toLong()
            val frequency = frequencyNumber.text.toString().toLong()
            val records = recordNumber.text.toString().toLong()
            val volumes = volumeNumber.text.toString().toLong()
            val alcohols = alcoholsNumber.text.toString().toLong()
            val add = StatisticsRecord(names, types, frequency, records, volumes, alcohols)
            viewModel.updateRecord(add)
        }
    }

    private fun getRankAndPrint() {
        viewModel.getMyRank().addOnSuccessListener {
            binding.namesRank.text = "제품 : ${it.namesRank}%"
            binding.recordRank.text = "기록 : ${it.recordsRank}%"
            binding.alcoholsRank.text = "알콜 : ${it.alcoholsRank}%"
            binding.typesRank.text = "주종 : ${it.typesRank}%"
            binding.volumeRank.text = "주량 : ${it.volumeRank}%"
        }.addOnFailureListener {
            binding.namesRank.text = "출력에 실패했습니다."
        }
    }

}


data class StatisticsRecord(
    var names: Long = 0,
    var types: Long = 0,
    var frequency: Long = 0,
    var records: Long = 0,
    var volumes: Long = 0,
    var alcohols: Long = 0,
)
