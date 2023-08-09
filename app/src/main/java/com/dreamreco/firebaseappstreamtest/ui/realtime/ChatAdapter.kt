package com.dreamreco.firebaseappstreamtest.ui.realtime

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.MyApplication
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.ItemMyChatBinding
import com.dreamreco.firebaseappstreamtest.databinding.ItemYourChatBinding

class ChatAdapter(val context: Context, val arrayList: ArrayList<ChatMessage>, val myEmail:String?)
    :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addItem(item: ChatMessage) {//아이템 추가
        arrayList.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        //getItemViewType 에서 뷰타입 1을 리턴받았다면 내채팅레이아웃을 받은 Holder를 리턴
        return if(viewType == 1){
            val view = ItemMyChatBinding.inflate(layoutInflater, parent, false)
            Holder(view)
        }
        //getItemViewType 에서 뷰타입 2을 리턴받았다면 상대채팅레이아웃을 받은 Holder2를 리턴
        else{
            val view = ItemYourChatBinding.inflate(layoutInflater, parent, false)
            Holder2(view)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        //onCreateViewHolder에서 리턴받은 뷰홀더가 Holder라면 내채팅, item_my_chat의 뷰들을 초기화 해줌
        if (viewHolder is Holder) {
            (viewHolder as Holder).chat_Text.setText(arrayList.get(i).message)
            (viewHolder as Holder).chat_Time.setText(arrayList.get(i).timestamp.toString())
        }
        //onCreateViewHolder에서 리턴받은 뷰홀더가 Holder2라면 상대의 채팅, item_your_chat의 뷰들을 초기화 해줌
        else if(viewHolder is Holder2) {
            (viewHolder as Holder2).chat_You_Image.setImageResource(R.mipmap.ic_launcher)
            (viewHolder as Holder2).chat_You_Name.setText(arrayList.get(i).sender)
            (viewHolder as Holder2).chat_Text.setText(arrayList.get(i).message)
            (viewHolder as Holder2).chat_Time.setText(arrayList.get(i).timestamp.toString())
        }

    }

    //내가친 채팅 뷰홀더
    inner class Holder(binding: ItemMyChatBinding) : RecyclerView.ViewHolder(binding.root) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_Text = binding.chatText
        val chat_Time = binding.chatTime
    }

    //상대가친 채팅 뷰홀더
    inner class Holder2(binding: ItemYourChatBinding) : RecyclerView.ViewHolder(binding.root) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_You_Image = binding.chatYouImage
        val chat_You_Name = binding.chatYouName
        val chat_Text = binding.chatText
        val chat_Time = binding.chatTime
    }

    override fun getItemViewType(position: Int): Int {//여기서 뷰타입을 1, 2로 바꿔서 지정해줘야 내채팅 너채팅을 바꾸면서 쌓을 수 있음
        //내 아이디와 arraylist의 name이 같다면 내꺼 아니면 상대꺼
        return if (arrayList.get(position).sender == myEmail) {
            1
        } else {
            2
        }
    }
}