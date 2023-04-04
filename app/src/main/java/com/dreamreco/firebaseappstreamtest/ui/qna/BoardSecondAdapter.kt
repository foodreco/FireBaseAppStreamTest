package com.dreamreco.firebaseappstreamtest.ui.qna

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.databinding.BoardAdapterChildBinding
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class BoardSecondAdapter :
    ListAdapter<QuestionAndAnswerClass, BoardSecondAdapter.QuestionAndAnswerViewHolder>(Companion){

    private var listener : OnBoardSelectedListener? = null

    interface OnBoardSelectedListener {
        fun onBoardSelected(content: QuestionAndAnswerClass)
    }

    fun setProductsAdapterListener(listener: OnBoardSelectedListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAndAnswerViewHolder {
        return QuestionAndAnswerViewHolder(
            BoardAdapterChildBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: QuestionAndAnswerViewHolder, position: Int) {
        // currentList: 해당 Adapter에 "submitList()"를 통해 삽입한 아이템 리스트
        holder.bindProduct(currentList[position])
    }

    // diffUtil: currentList에 있는 각 아이템들을 비교하여 최신 상태를 유지하도록 한다.
    companion object : DiffUtil.ItemCallback<QuestionAndAnswerClass>() {
        override fun areItemsTheSame(oldItem: QuestionAndAnswerClass, newItem: QuestionAndAnswerClass): Boolean {
            Log.e("BoardSecondAdapter","areItemsTheSame oldItem : $oldItem\nnewItem : $newItem")
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: QuestionAndAnswerClass, newItem: QuestionAndAnswerClass): Boolean {
            Log.e("BoardSecondAdapter","areContentsTheSame oldItem : $oldItem\nnewItem : $newItem")
            return oldItem == newItem
        }
    }

    inner class QuestionAndAnswerViewHolder(
        private val binding: BoardAdapterChildBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindProduct(qnaData: QuestionAndAnswerClass) {
            with(binding) {
                boardTitle.text = qnaData.title

                if (qnaData.timestamp != null) {
                    binding.boardDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(qnaData.timestamp!!)
                }

                Log.e("QuestionAndAnswerViewHolder", "출력 title : ${qnaData.title}")

                boardWriter.text = qnaData.boardWriter

                // Click listener
                root.setOnClickListener {
                    listener?.onBoardSelected(qnaData)
                }
            }
        }
    }
}
