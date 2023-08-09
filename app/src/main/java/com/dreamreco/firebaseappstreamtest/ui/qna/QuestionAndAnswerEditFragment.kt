package com.dreamreco.firebaseappstreamtest.ui.qna

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentQuestionAndAnswerEditBinding
import com.dreamreco.firebaseappstreamtest.repository.QuestionAndAnswerEditClass
import com.dreamreco.firebaseappstreamtest.repository.ReplyClass
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.dreamreco.firebaseappstreamtest.util.FireBaseModule
import com.dreamreco.firebaseappstreamtest.util.MyCustomFragment
import com.dreamreco.firebaseappstreamtest.util.clearFocusAndHideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// TODO : 1) user 가 null 일 수 있다.
class QuestionAndAnswerEditFragment : MyCustomFragment(), ReplyAdapter.OnReplySelectedListener {

    private val binding by lazy { FragmentQuestionAndAnswerEditBinding.inflate(layoutInflater) }
    private val arg by navArgs<QuestionAndAnswerEditFragmentArgs>()
    private val viewModel by viewModels<QuestionAndAnswerViewModel>()

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    private var editActivated: Boolean = false

    private var mAdapter: ReplyAdapter? = null
    private var query: Query? = null

    private var editActionCompletedListener: EditActionCompletedListener? = null

    companion object {
        private const val TAG = "QuestionAndAnswerEditFragment"
    }

    interface EditActionCompletedListener {
        fun onJobDone() {
        }
    }

    fun setEditActionCompletedListener(listener: EditActionCompletedListener) {
        this.editActionCompletedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setBasicElements()

        Log.e(TAG, "typeface : $typeface\nthemeType : $themeType\nimageViewColor:$imageViewColor")
        Log.e(TAG, "arg \n ${arg.boardContent}")

        return binding.root
    }

    private fun setBasicElements() {
        with(binding) {
            dialogTitle.setText(arg.boardContent.title)
            dialogTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            if (arg.boardContent.timestamp != null) {
                dialogDate.text = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(arg.boardContent.timestamp!!)
            }
            dialogContent.setText(arg.boardContent.content)
            dialogContent.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            dialogTitle.isEnabled = false
            dialogContent.isEnabled = false

            // 작성자와 동일한 uid 일 경우, 수정가능
            if (user?.uid == arg.boardContent.uid) {
                dialogEdit.visibility = View.VISIBLE
            }

            if (user != null) {
                dialogEdit.visibility = View.VISIBLE
            } else {
                dialogEdit.visibility = View.GONE
            }

            dialogEdit.setOnClickListener {
                if (!editActivated) {
                    dialogTitle.isEnabled = true
                    dialogContent.isEnabled = true
                    dialogEdit.text = "수정완료"
                    editActivated = !editActivated
                } else {
                    editBoardContent()
                }
            }

            replyEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editString: Editable?) {
                    // EditText 의 텍스트가 변경된 후 호출됩니다.
                    // 여기서 텍스트를 처리할 수 있습니다.
                    Log.e(TAG, "editString : $editString")
                    if (editString?.isNotBlank() == true) {
                        btnReplyEnroll.visibility = View.VISIBLE
                    } else {
                        btnReplyEnroll.visibility = View.GONE
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // EditText 의 텍스트가 변경되기 전에 호출됩니다.
                    Log.e(TAG, "beforeTextChanged\n$s : start:$start / count:$count / after:$after")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // EditText 의 텍스트가 변경될 때 호출됩니다.
                    Log.e(TAG, "onTextChanged\n$s : start:$start / count:$count / before:$before")
                }
            })

            btnReplyEnroll.setOnClickListener {
                addReply()
            }

            binding.dialogDelete.setOnClickListener {
                deleteBoard()
            }

            setAndLoadReplyData()
        }

        viewModel.enrollCompleted.observe(viewLifecycleOwner) { completed ->
            if (completed == null) {
                return@observe
            } else {
                when (completed) {
                    true -> {
                        Toast.makeText(requireContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }

                    else -> Toast.makeText(requireContext(), "수정실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 게시글을 삭제하는 코드 */
    private fun deleteBoard() {
        viewModel.deleteBoard(arg.boardContent.documentId).addOnSuccessListener(requireActivity()) {
            Log.e(TAG, "delete reply success")
            Toast.makeText(requireContext(), "게시글 삭제됨", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
            .addOnFailureListener(requireActivity()) { e ->
                Log.e(TAG, "delete reply failed", e)
                Toast.makeText(requireContext(), "게시글 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editBoardContent() {
        with(binding) {
            if (dialogTitle.text.trim().isBlank() || (dialogContent.text.trim()
                    .isBlank())
            ) {
                Toast.makeText(requireContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {

                if ((arg.boardContent.content == dialogContent.text.trim()
                        .toString()) && (arg.boardContent.title == dialogTitle.text.trim()
                        .toString())
                ) {
                    Toast.makeText(requireContext(), "수정했다 칩시다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    editActivated = !editActivated
                    dialogTitle.isEnabled = false
                    dialogContent.isEnabled = false
                    dialogEdit.text = "수정하기"

                    val currentDateTime =
                        Instant.ofEpochMilli(System.currentTimeMillis())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    val argumentDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        .format(currentDateTime).toLong()

//                    var writer = user!!.email!!.substring(0, 3) + "***"
//                    if (user!!.uid == "910Em692hcQUqCdAxisu042rNcm2") {
//                        writer = getString(R.string.app_manager)
//                    }
                    val questionAndAnswerClass = QuestionAndAnswerEditClass(
                        arg.boardContent.documentId,
                        arg.boardContent.boardWriter,
                        dialogContent.text.trim().toString(),
                        dialogTitle.text.trim().toString(),
                        user!!.uid,
                        arg.boardContent.replyCount
                    )
                    viewModel.editBoardContent(questionAndAnswerClass)
                }
            }
        }
    }

    private fun setAndLoadReplyData() {
        // Board 목록 얻기
        // 기본 정렬 date 순으로
        // TODO : 별점 넣는 것처럼 답변넣기??
        //  query 는 count 어떻게 적용되는지??
        query = viewModel.queryForReply(arg.boardContent.documentId)
        query?.let {
            mAdapter = object : ReplyAdapter(it, FireBaseModule.getUser(), this@QuestionAndAnswerEditFragment) {
                override fun onError(e: FirebaseFirestoreException) {
                    Snackbar.make(
                        binding.root,
                        "에러발생 : $e.", Snackbar.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "에러발생", e)
                }

                override fun onDataChanged() {
                    if (itemCount == 0) {
                        Toast.makeText(requireContext(), "댓글이 없습니다.", Toast.LENGTH_SHORT).show()
                        binding.replyRecyclerView.visibility = View.GONE
                        //TODO : 리사이클러뷰 gone + layout 처리
                    } else {
                        binding.replyRecyclerView.visibility = View.VISIBLE
                    }

//                    if (itemCount == 0) {
//                        binding.recyclerRatings.visibility = View.GONE
//                        binding.viewEmptyRatings.visibility = View.VISIBLE
//                    } else {
//                        binding.recyclerRatings.visibility = View.VISIBLE
//                        binding.viewEmptyRatings.visibility = View.GONE
//                    }
                }
            }
        }
        binding.replyRecyclerView.adapter = mAdapter
    }

    //TODO : RecyclerView 구현 시, 꼭 붙어야 함
    override fun onStart() {
        super.onStart()
        // Start listening for Firestore updates
        mAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    /** 답글을 다는 코드 */
    private fun addReply() {
        if (user != null) {
            // In a transaction, add the new rating and update the aggregate totals
            viewModel.addReply(
                user!!,
                arg.boardContent.documentId,
                binding.replyEditText.text.trim().toString()
            )
                .addOnSuccessListener(requireActivity()) {
                    Log.d(TAG, "reply success")
                    Toast.makeText(requireContext(), "답글 등록", Toast.LENGTH_SHORT).show()
                    // Hide keyboard and scroll to top
                    binding.replyEditText.clearFocusAndHideKeyboard(requireContext())
                    binding.replyEditText.setText("")
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Log.e(TAG, "Add reply failed", e)
                    Toast.makeText(requireContext(), "답글 실패", Toast.LENGTH_SHORT).show()
                    // Show failure message and hide keyboard
                    binding.replyEditText.clearFocusAndHideKeyboard(requireContext())
                }
        } else {
            Toast.makeText(requireContext(), "로그인 필요",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReplyDelete(content: ReplyClass, documentId: String) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            Log.e(TAG, "댓글 삭제 발동")
            viewModel.deleteReply(arg.boardContent.documentId, documentId)
                .addOnSuccessListener(requireActivity()) {
                    Log.d(TAG, "delete reply success")
                    Toast.makeText(requireContext(), "댓글 삭제됨", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Log.e(TAG, "delete reply failed", e)
                    Toast.makeText(requireContext(), "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ -> }
        builder.setTitle("댓글 삭제")
        builder.setMessage("댓글을 삭제하시겠습니까?")
        builder.create().show()
    }

    override fun onReplyEdit(content: ReplyClass, documentId: String) {
        Log.e(TAG, "댓글 수정 발동")
        viewModel.editReply(arg.boardContent.documentId, documentId)
    }
}