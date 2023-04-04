package com.dreamreco.firebaseappstreamtest.ui.qna

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.databinding.BoardAdapterChildBinding
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.Product
import com.dreamreco.firebaseappstreamtest.ui.qna.ProductsAdapterSecond.ProductViewHolder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Boolean
import kotlin.Int
import kotlin.with


/** realtime update 수동 타입 */
class ProductsAdapter : PagingDataAdapter<QuestionAndAnswerEditClass, ProductsAdapter.QuestionAndAnswerViewHolder>(Companion) {

    /** 스크롤 위치를 유지하는 코드 */
    private var scrollPosition: Int? = null

    private var listener : OnBoardSelectedListener? = null

    interface OnBoardSelectedListener {
        fun onBoardSelected(content: QuestionAndAnswerEditClass)
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
        val product = getItem(position) ?: return
        holder.bindProduct(product)
    }

    companion object : DiffUtil.ItemCallback<QuestionAndAnswerEditClass>() {
        override fun areItemsTheSame(oldItem: QuestionAndAnswerEditClass, newItem: QuestionAndAnswerEditClass): Boolean {
            return oldItem.documentId == newItem.documentId
        }

        override fun areContentsTheSame(oldItem: QuestionAndAnswerEditClass, newItem: QuestionAndAnswerEditClass): Boolean {
            return oldItem == newItem
        }
    }



//    override fun notifyDataSetChanged() {
//        // notifyDataSetChanged() 메소드에서 스크롤 위치를 저장하고 호출합니다.
//        scrollPosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
//        super.notifyDataSetChanged()
//    }

    inner class QuestionAndAnswerViewHolder(
        private val binding: BoardAdapterChildBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindProduct(qnaData: QuestionAndAnswerEditClass) {
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

                val replyCountNumber = qnaData.replyCount
                if (replyCountNumber == 0) {
                    replyCount.visibility = View.GONE
                } else {
                    replyCount.visibility = View.VISIBLE
                    replyCount.text = qnaData.replyCount.toString()
                }

            }
        }
    }
}

/** Paging + Realtime 용 어뎁터 */
class ProductsAdapterSecond(private val productList: List<QuestionAndAnswerEditClass>) :
    RecyclerView.Adapter<ProductViewHolder>() {

    private var listener : OnBoardSelectedListener? = null

    interface OnBoardSelectedListener {
        fun onBoardSelected(content: QuestionAndAnswerEditClass)
    }

    fun setProductsAdapterListener(listener: OnBoardSelectedListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        return ProductViewHolder(
            BoardAdapterChildBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        val product = productList[position]
        holder.bindProduct(product)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductViewHolder(private val binding: BoardAdapterChildBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindProduct(qnaData: QuestionAndAnswerEditClass) {
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