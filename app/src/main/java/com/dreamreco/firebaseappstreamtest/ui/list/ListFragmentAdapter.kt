package com.dreamreco.firebaseappstreamtest.ui.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.CalendarEmptyHeaderBinding
import com.dreamreco.firebaseappstreamtest.databinding.DateHeaderBinding
import com.dreamreco.firebaseappstreamtest.databinding.ListFragmentChildBinding
import com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase
import com.dreamreco.firebaseappstreamtest.util.setGlobalFont
import java.io.FileNotFoundException

class ListFragmentAdapter(
    ctx: Context,
    typeface: Typeface,
    theme: String
) :
    ListAdapter<ListFragmentAdapterBase, RecyclerView.ViewHolder>(ListFragmentDiffCallback()) {
    // 기본 코드
    private var mContext: Context = ctx
    private var mTypeface = typeface
    private val thisTheme = theme

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListFragmentChildBinding.inflate(layoutInflater, parent, false)

        return when (viewType) {
            ListFragmentAdapterBase.Item.VIEW_TYPE -> ListFragmentItemViewHolder(binding)
            ListFragmentAdapterBase.EmptyHeader.VIEW_TYPE -> ListFragmentEmptyHeaderViewHolder.from(
                parent, mTypeface
            )
            ListFragmentAdapterBase.DateHeader.VIEW_TYPE -> ListFragmentDateHeaderViewHolder.from(
                parent,
                mContext, mTypeface
            )
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ListFragmentItemViewHolder -> {
                val item = getItem(position) as ListFragmentAdapterBase.Item
                val diaryBase = item.diaryBase
                with(viewHolder) {
                    // bind
                    bind(diaryBase)
                }
            }
            is ListFragmentDateHeaderViewHolder -> {
                val item = getItem(position) as ListFragmentAdapterBase.DateHeader
                viewHolder.bind(item)
            }
            is ListFragmentEmptyHeaderViewHolder -> {
                viewHolder.bind()
            }
        }
    }

    // 리스트용 뷰홀더
    inner class ListFragmentItemViewHolder constructor(private val binding: ListFragmentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(item: DiaryBase) {

            setGlobalFont(binding.root, mTypeface)

            binding.apply {

                diaryTitle.text = item.title
                diaryContent.text = item.content

                if (item.image != null) {
                    try {
                        with(diaryImage) {
                            imageTintList = null
//                            setImageBitmap(
//                                decodeSampledBitmapFromInputStream(
//                                    item.image!!,
//                                    50,
//                                    50,
//                                    mContext
//                                )
//                            )
                            setImageBitmap(
                                item.bitmapForRecyclerView
                            )
                        }
                    } catch (e: FileNotFoundException) {
                        // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
                        // FileNotFoundException 에러 발생
                        diaryImage.imageTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.black))
                        diaryImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                mContext, R.drawable.ic_image
                            )
                        )
                    }
                } else {
                    diaryImage.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.black))
                    diaryImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            mContext, R.drawable.ic_image
                        )
                    )
                }

                if (item.myDrink != null) {
                    diaryDrinkType.text = item.myDrink!!.drinkType
                    diaryDrinkName.text = " · ${item.myDrink!!.drinkName}"
                    diaryVOD.text = " · ${item.myDrink!!.VOD}mL"
                    diaryPOA.text = " · ${item.myDrink!!.POA}%"
                } else {
                    diaryDrinkType.text = ""
                    diaryDrinkName.text = ""
                    diaryVOD.text = ""
                    diaryPOA.text = ""
                }

//                if (item.myDrink != null) {
//                    diaryDrinkType.text = item.myDrink!!.drinkType
//                } else {
//                    diaryDrinkType.text = ""
//                }

                if (item.importance) {
                    diaryBaseImportance.visibility = View.VISIBLE
//                    when (thisTheme) {
//                        THEME_2 -> diaryBaseImportance.imageTintList =
//                            mContext.getColorStateList(R.color.theme2_primary_touch_color)
//                        else -> diaryBaseImportance.imageTintList =
//                            mContext.getColorStateList(R.color.basic_primary)
//                    }
                } else {
                    diaryBaseImportance.visibility = View.INVISIBLE
                }

//                //리싸이클러 길게 터치 시,
//                recyclerViewChildLayout.setOnLongClickListener {
//                    return@setOnLongClickListener true
//                }

                //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
                recyclerViewChildLayout.setOnClickListener {
                    val action = ListFragmentDirections.actionListFragmentToPopUpFragment(item)
                    it.findNavController().navigate(action)
                }
            }
        }
    }

}

// empty 헤더용 뷰홀더
class ListFragmentEmptyHeaderViewHolder constructor(private val binding: CalendarEmptyHeaderBinding, typeface: Typeface
) :
    RecyclerView.ViewHolder(binding.root) {

    private val mTypeface = typeface

    fun bind() {
        setGlobalFont(binding.root, mTypeface)
    }

    companion object {
        fun from(parent: ViewGroup, typeface: Typeface): ListFragmentEmptyHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CalendarEmptyHeaderBinding.inflate(layoutInflater, parent, false)
            return ListFragmentEmptyHeaderViewHolder(binding, typeface)
        }
    }
}

// date 헤더용 뷰홀더
class ListFragmentDateHeaderViewHolder constructor(
    private val binding: DateHeaderBinding,
    context: Context, typeface: Typeface
) :
    RecyclerView.ViewHolder(binding.root) {

    private val mContext = context
    private val mTypeface = typeface

    companion object {
        fun from(parent: ViewGroup, context: Context, typeface: Typeface): ListFragmentDateHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DateHeaderBinding.inflate(layoutInflater, parent, false)
            return ListFragmentDateHeaderViewHolder(binding, context, typeface)
        }
    }

    fun bind(item: ListFragmentAdapterBase) {

         setGlobalFont(binding.root, mTypeface)

        val diaryBase = (item as ListFragmentAdapterBase.DateHeader).diaryBase
        binding.apply {
            textDate.text = mContext.getString(
                R.string.diary_month,
                diaryBase.date.year,
                diaryBase.date.month
            )
        }
    }
}

class ListFragmentDiffCallback : DiffUtil.ItemCallback<ListFragmentAdapterBase>() {
    override fun areItemsTheSame(
        oldItem: ListFragmentAdapterBase,
        newItem: ListFragmentAdapterBase
    ): Boolean {
        return oldItem.layoutId == newItem.layoutId
    }

    override fun areContentsTheSame(
        oldItem: ListFragmentAdapterBase,
        newItem: ListFragmentAdapterBase
    ): Boolean {
        return oldItem == newItem
    }
}

// ListFragment Adapter sealed class
sealed class ListFragmentAdapterBase {
    abstract val layoutId: Int

    // Item
    data class Item(
        val diaryBase: DiaryBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.list_fragment_child
        }
    }

    // Date
    data class DateHeader(
        val diaryBase: DiaryBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.date_header
        }
    }

    // 리스트가 없을 때,
    data class EmptyHeader(
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.calendar_empty_header
        }
    }
}