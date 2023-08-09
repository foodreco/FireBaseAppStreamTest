package com.dreamreco.firebaseappstreamtest.ui.realtime

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamreco.firebaseappstreamtest.databinding.FragmentRealTimeBinding
import com.dreamreco.firebaseappstreamtest.util.clearFocusAndHideKeyboardInScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RealTimeFragment : Fragment() {

    private val binding by lazy { FragmentRealTimeBinding.inflate(layoutInflater) }

    companion object {
        const val TAG = "RealTimeFragment"
    }

    private val viewModel by viewModels<RealTimeViewModel>()

    private var firebaseUser : FirebaseUser? = null

    private var myChatRef : DatabaseReference? = null

    // DB 참조 child
    private var messagesRef : DatabaseReference? = null

    override fun onStart() {
        super.onStart()
        if (firebaseUser != null) {
//            FirebaseDatabase.getInstance().goOnline()
        }
    }

//    private val testRef = Firebase.database.reference.child("messages")

    //리사이클러뷰 관련 변수
    var arrayList = arrayListOf<ChatMessage>()
    private lateinit var mAdapter : ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            Log.e(TAG,"messagesRef 활성화")
            messagesRef = FirebaseDatabase.getInstance().getReference("messages")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(binding) {
            mAdapter = ChatAdapter(requireContext(), arrayList, firebaseUser?.email)
            //어댑터 선언
            chatRecyclerview.adapter = mAdapter
            //레이아웃 매니저 선언
            chatRecyclerview.setHasFixedSize(true)//아이템이 추가삭제될때 크기측면에서 오류 안나게 해줌
            chatSendButton.setOnClickListener {
                //아이템 추가 부분
                sendMessage()
            }
        }

        binding.chatSendButton.setOnClickListener {
            sendMessage()
        }

        if (firebaseUser != null) {
            // 데이터 변경 리스너 등록
            Log.e(TAG,"messagesRef 리스너 등록")
            messagesRef?.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(ChatMessage::class.java)
                    // 새 메시지를 UI에 표시하거나 처리
                    if (chatMessage != null) {
                        with(chatMessage) {
                            val format = ChatMessage(sender, message, timestamp)
                            mAdapter.addItem(format)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        return binding.root
    }

    private fun sendMessage() {
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "로그인 필요", Toast.LENGTH_SHORT).show()
        } else {

            val uploadText = binding.chatingText.text

            val timestamp = System.currentTimeMillis()
            val chatMessage = ChatMessage(firebaseUser!!.email.toString(),
                uploadText.toString(), timestamp)

            messagesRef?.push()?.setValue(chatMessage)

            binding.chatingText.setText("")

            // 키보드 내리기
            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)

            // recyclerView 스크롤 최하단 이동
            // LinearLayoutManager를 가져옴
            val layoutManager = binding.chatRecyclerview.layoutManager as LinearLayoutManager

            // 스크롤을 제일 하단으로 이동
            val itemCount = layoutManager.itemCount
            binding.chatRecyclerview.scrollToPosition(itemCount - 1)
        }
    }

    override fun onStop() {
        super.onStop()
//        FirebaseDatabase.getInstance().goOffline()
//        messagesRef?.database?.goOffline()
    }
}

@IgnoreExtraProperties
data class User(val username: String? = null, val email: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

@Keep
@IgnoreExtraProperties
data class ChatMessage(val sender: String = "", val message: String = "", val timestamp: Long = 0)