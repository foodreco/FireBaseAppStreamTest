package com.dreamreco.firebaseappstreamtest.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentDataBaseBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DataBaseFragment : Fragment() {

    private val binding by lazy { FragmentDataBaseBinding.inflate(layoutInflater) }
    private lateinit var viewModel: DataBaseViewModel
    private lateinit var fireBaseDatabase : FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fireBaseDatabase = Firebase.database("https://fir-appstreamtest-default-rtdb.asia-southeast1.firebasedatabase.app/")
        myRef = fireBaseDatabase.getReference("메세지")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.btnToSend.setOnClickListener {
            myRef.setValue(binding.textSendToDb.text.toString())
            Log.e("DB 조각", "${binding.textSendToDb.text} 전송함")
        }

        binding.btnToSendPart2.setOnClickListener {
            myRef.push().setValue(binding.textSendToDb.text.toString())
            Log.e("DB 조각", "${binding.textSendToDb.text} 누적 전송함")
        }

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                binding.textReadToDb.text = value.toString()
                Log.e("DB 조각", "onDataChange")
            }

            override fun onCancelled(error: DatabaseError) {
                // DB 콜 실패 시,
                Log.e("DB 조각", "호출 실패 : ${error.toException()}")
            }
        })

        return binding.root
    }


}