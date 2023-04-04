package com.dreamreco.firebaseappstreamtest.ui.qna

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.databinding.ReplyAdapterChildBinding
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.dreamreco.firebaseappstreamtest.repository.ReplyClass
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class ReplyAdapter(query: Query?, myUid : String?, private val listener: OnReplySelectedListener) :
    FireStoreAdapter<ReplyAdapter.ViewHolder>(query) {

    companion object {
        private const val TAG = "ReplyAdapter"
    }

    private var mUid = myUid

    interface OnReplySelectedListener {
        fun onReplyDelete(content: ReplyClass, documentId: String)
        fun onReplyEdit(content: ReplyClass, documentId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReplyAdapterChildBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    inner class ViewHolder(val binding: ReplyAdapterChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var btnState = false

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnReplySelectedListener?
        ) {
            val replyData = snapshot.toObject<ReplyClass>()
            Log.e("BoardAdapter", "qnaData : $replyData")

            if (replyData == null) {
                return
            }

            if (mUid == replyData.uid) {
                binding.btnReplyOption.visibility = View.VISIBLE
            } else {
                binding.btnReplyOption.visibility = View.GONE
            }

            with(binding) {
                replyContent.text = replyData.content

                if (replyData.timestamp != null) {
                    replyDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(replyData.timestamp!!)
                }

                replyWriter.text = replyData.boardWriter

                btnReplyOption.setOnClickListener {
                    if (!btnState) {
                        btnOptionLayout.visibility = View.VISIBLE
                    } else {
                        btnOptionLayout.visibility = View.GONE
                    }
                    btnState = !btnState
                }

                btnReplyEdit.setOnClickListener {
                    Log.e(TAG, "${snapshot.id} 수정")
                    listener?.onReplyEdit(replyData, snapshot.id)
                    btnState = false
                    btnOptionLayout.visibility = View.GONE
                }

                btnReplyDelete.setOnClickListener {
                    Log.e(TAG, "${snapshot.id} 삭제")
                    listener?.onReplyDelete(replyData, snapshot.id)
                    btnState = false
                    btnOptionLayout.visibility = View.GONE
                }
            }
        }
    }
}
