package com.dreamreco.firebaseappstreamtest.ui.qna

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.databinding.BoardAdapterChildBinding
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class BoardAdapter(query: Query?, private val listener: OnBoardSelectedListener) :
    FireStoreAdapter<BoardAdapter.ViewHolder>(query) {

    interface OnBoardSelectedListener {
        fun onBoardSelected(content: QuestionAndAnswerClass, documentId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BoardAdapterChildBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    inner class ViewHolder(val binding: BoardAdapterChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnBoardSelectedListener?
        ) {

            val qnaData = snapshot.toObject<QuestionAndAnswerClass>() ?: return

            with(binding) {
                boardTitle.text = qnaData.title

                if (qnaData.timestamp != null) {
                    binding.boardDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(qnaData.timestamp!!)
                }

                boardWriter.text = qnaData.boardWriter
            }

            // Click listener
            binding.root.setOnClickListener {
                listener?.onBoardSelected(qnaData, snapshot.id)
            }
        }
    }
}
