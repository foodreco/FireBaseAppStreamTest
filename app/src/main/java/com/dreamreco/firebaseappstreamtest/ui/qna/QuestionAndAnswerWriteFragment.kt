package com.dreamreco.firebaseappstreamtest.ui.qna

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentQuestionAndAnswerWriteBinding
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerClass
import com.dreamreco.firebaseappstreamtest.util.MyCustomDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// TODO : user 가 null 이면 안된다!
class QuestionAndAnswerWriteFragment : MyCustomDialogFragment() {

    private val binding by lazy { FragmentQuestionAndAnswerWriteBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<QuestionAndAnswerViewModel>()

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    private var writeActionCompletedListener : WriteActionCompletedListener? = null

    companion object {
        private const val TAG = "QuestionAndAnswerWriteFragment"
    }

    interface WriteActionCompletedListener {
        fun onWriteBoardCompleted()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach 작동")
        if (parentFragment is WriteActionCompletedListener) {
            Log.e(TAG, "parentFragment : $parentFragment")
            writeActionCompletedListener = parentFragment as WriteActionCompletedListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setBasicElements()

        Log.e(TAG, "typeface : $typeface\nthemeType : $themeType\nimageViewColor:$imageViewColor")

        return binding.root
    }

    private fun setBasicElements() {
        with(binding) {
            val currentDateTime =
                Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            val dateResult = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .format(currentDateTime)
            dialogDate.text = dateResult.toString()

            // 등록 버튼 관련
            btnConfirm.setOnClickListener {
                if (dialogTitle.text.trim().isBlank() || (dialogContent.text.trim()
                        .isBlank())
                ) {
                    Toast.makeText(requireContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    val argumentDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        .format(currentDateTime).toLong()

                    var writer = user!!.email!!.substring(0, 3) + "***"
                    if (user!!.uid == "910Em692hcQUqCdAxisu042rNcm2") {
                        writer = getString(R.string.app_manager)
                    }
                    val questionAndAnswerClass = QuestionAndAnswerClass(
                        writer,
                        dialogContent.text.toString(),
                        dialogTitle.text.toString(),
                        user!!.uid,
                        0
                    )
                    enrollBoardContent(questionAndAnswerClass)
                }
            }
        }

        viewModel.enrollCompleted.observe(viewLifecycleOwner) { completed ->
            if (completed == null) {
                return@observe
            } else {
                when (completed) {
                    true -> {
                        Toast.makeText(requireContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show()
                        writeActionCompletedListener?.onWriteBoardCompleted()
//                        findNavController().navigateUp()
                        dismiss()
                    }
                    else -> Toast.makeText(requireContext(), "등록실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enrollBoardContent(questionAndAnswerClass: QuestionAndAnswerClass) {
        viewModel.enrollBoardContent(questionAndAnswerClass)
    }
}