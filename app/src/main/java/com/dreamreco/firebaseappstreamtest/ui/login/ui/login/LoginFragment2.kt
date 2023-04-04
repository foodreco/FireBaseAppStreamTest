package com.dreamreco.firebaseappstreamtest.ui.login.ui.login

import android.content.Intent.getIntent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentLogin2Binding
import com.dreamreco.firebaseappstreamtest.ui.login.LoginFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment2 : Fragment() {

    private val loginViewModel by viewModels<LoginViewModel>()
    private var _binding: FragmentLogin2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var emailAddress: String? = null

    private lateinit var auth: FirebaseAuth
    private var doneAction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            Log.d(LoginFragment.TAG, "로그인 되어 있음 uid : ${user.uid}")
        } else {
            Log.d(LoginFragment.TAG, "로그인 안됨")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogin2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }

                // 에러인 경우, 키보드 엔터 안되게??
                if (loginFormState.usernameError != null) {
                    usernameEditText.imeOptions = EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION
                    doneAction = false
                    // IME_FLAG_NO_ACCESSORY_ACTION
                } else {
                    usernameEditText.imeOptions = EditorInfo.IME_ACTION_DONE
                    doneAction = true
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->

                Log.e("로그인2조각", "loginResult 옵저버 발동")

                loginResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                    Log.e("로그인2조각", "loginResult 실패")
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                    findNavController().navigate(LoginFragment2Directions.actionLoginFragment2ToMainFragment())
                    Log.e("로그인2조각", "loginResult 성공")
                }
            })

        /** 텍스트 변환을 추적하는 리스너 */
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        /** 이메일 링크로 로그인하기 */
        binding.loginWithEmailLink.setOnClickListener {
//            loginWithEmailLink()
            Toast.makeText(requireContext(),"로그인 테스트!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithEmailLink() {

        Log.e("로그인프레그먼트2", "loginWithEmailLink 작동")

        val actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://fir-appstreamtest.firebaseapp.com"
            // This must be true
            handleCodeInApp = true
//            setIOSBundleId("com.example.ios")
            setAndroidPackageName(
                "com.dreamreco.firebaseappstreamtest",
                true, /* installIfNotAvailable */
                "12" /* minimumVersion */
            )
        }


        val emailAddress = binding.username.text.toString().trim()

        Log.e("로그인프레그먼트2", "이메일 주소 : $emailAddress")

        auth.sendSignInLinkToEmail(emailAddress, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("로그인프레그먼트2", "이메일 전송됨?? : $emailAddress")
                    Toast.makeText(requireContext(), "이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("로그인프레그먼트2", "이메일 전송실패 : $e")
                Toast.makeText(requireContext(), "이메일이 전송실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()

        Log.e("로그인2조각", "updateUiWithUser 작동")
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}