package com.dreamreco.firebaseappstreamtest.ui.login

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentLoginBinding
import com.dreamreco.firebaseappstreamtest.ui.login.ui.login.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthCredential
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }
    private val loginViewModel by viewModels<LoginViewModel>()

    private lateinit var auth: FirebaseAuth

    // 구글 로그인 관련 변수
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var oneTapClientSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val intent = result.data
            Log.e(TAG, "oneTapClientSignInLauncher 작동 : $intent")
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(intent)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        Log.d(TAG, "id 토큰 받음 : $idToken")
                        // Firebase 와 연동하기
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        val googleCredential = GoogleAuthProvider.getCredential(idToken, null)

                        // 기존 로그인 된게 있다면, 병합한다.
                        if (auth.currentUser != null) {
                            auth.currentUser!!.linkWithCredential(googleCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        val user = task.result?.user
                                        Log.d(TAG, "linkWithCredential : 병합 성공 ${user?.uid}")
                                    } else {
                                        Log.w(TAG, "linkWithCredential: 병합 실패", task.exception)
                                        //TODO : 이미 다른 계정으로 생성이 된 경우 병합이 안됨.
                                        // 현재 계정을 로그아웃 후, 재 로그인
                                    }
                                }
                        } else {
                            auth.signInWithCredential(googleCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential : 성공함")
                                        val user = auth.currentUser
                                        if (user != null) {
                                            Log.d(TAG, "사용자 uid : ${user.uid} ")
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential: 실패 ", task.exception)
                                        Toast.makeText(
                                            requireContext(),
                                            "로그인 실패함",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                        }
                    }
                    else -> {
                        // Shouldn't happen.
                        Log.d(TAG, "id 토큰 없음")
                    }
                }
            } catch (e: ApiException) {
                // 에러 발생 시,
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(TAG, "One-tap dialog was closed.")
                        // Don't re-prompt the user.
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        Log.d(TAG, "One-tap encountered a network error.")
                        // Try again or just ignore.
                    }
                    else -> {
                        Log.d(
                            TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})"
                        )
                    }
                }
            }
        }

//    // 로그인 런쳐
//    private val signInLauncher = registerForActivityResult(
//        FirebaseAuthUIActivityResultContract()
//    ) { result -> this.onSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "로그인 되어 있음 uid : ${user.uid}")
            Log.d(TAG, "로그인 되어 있음 url : ${user.photoUrl}")
        } else {
            Log.d(TAG, "로그인 안됨")
        }

        initGoogleLogin()
    }

    private fun initGoogleLogin() {
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false) // 첫 인증 시에는 false 로 해야 보인다!!
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()


//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.your_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            .build()

    }

    override fun onStart() {
        super.onStart()

//        // Start sign in if necessary
//        if (shouldStartSignIn()) {
//            startSignIn()
//            return
//        }
    }

//    private fun startSignIn() {
//        Log.e("메인조각","startSignIn 작동")
//        // Sign in with FirebaseUI
//        val intent = AuthUI.getInstance().createSignInIntentBuilder()
//            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build(), AuthUI.IdpConfig.AnonymousBuilder().build()))
//            .setIsSmartLockEnabled(true)
//            .build()
//
//        signInLauncher.launch(intent)
//        loginViewModel.isSigningIn = true
//    }

//    private fun shouldStartSignIn(): Boolean {
//        return !loginViewModel.isSigningIn && Firebase.auth.currentUser == null
//    }
//
//    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//
//        Log.e("메인조각","onSignInResult 작동")
//
//        val response = result.idpResponse
//        loginViewModel.isSigningIn = false
//
//        if (result.resultCode != Activity.RESULT_OK) {
//            if (response == null) {
//                // User pressed the back button.
//                requireActivity().finish()
//            } else if (response.error != null && response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
//                showSignInErrorDialog(R.string.message_no_network)
//            } else {
//                showSignInErrorDialog(R.string.message_unknown)
//            }
//        }
//    }

//    private fun showSignInErrorDialog(@StringRes message: Int) {
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle(R.string.title_sign_in_error)
//            .setMessage(message)
//            .setCancelable(false)
//            .setPositiveButton(R.string.option_retry) { _, _ -> startSignIn() }
//            .setNegativeButton(R.string.option_exit) { _, _ -> requireActivity().finish() }.create()
//        dialog.show()
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            loginByEmail.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginFragment2())
            }

            loginByKakao.setOnClickListener {
//                loginWithKaKao()
                getKaKaoTokens(requireContext())
            }

            logoutByKakao.setOnClickListener {
                // 로그아웃
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                    } else {
                        Log.e(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                    }
                }

                // 연결 끊기
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        Log.e(TAG, "연결 끊기 실패", error)
                    } else {
                        Log.e(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                    }
                }
            }

            loginByGoogle.setOnClickListener {
                loginByGoogle()
            }
            logoutByGoogle.setOnClickListener {
                Firebase.auth.signOut()
                Toast.makeText(requireContext(), "로그아웃됨", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    /** 구글로 로그인하는 코드 */
    private fun loginByGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapClientSignInLauncher.launch(intentSenderRequest)

                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.e(TAG, "원탭 로그인 기준 ID 없음", e)
            }
    }


    /** 카카오 토큰 가져오는 코드 */
    fun getKaKaoTokens(context: Context) {
        // 로그인 조합 예제
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.e(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)

                } else if (token != null) {
                    Log.e(TAG, "카카오톡으로 로그인 성공 액세스 토큰 : ${token.accessToken}")
                    Log.e(TAG, "카카오톡으로 로그인 성공 token.idToken : ${token.idToken}")
                    Log.e(TAG, "카카오톡으로 로그인 성공 token.scopes : ${token.scopes}")

                    // firebase??
//                    test(token.idToken)

                    loginWithKaKao()
                }
            }
        } else {
            // 카카오계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /** 암시적 흐름 id token 사용 */
    private fun test(idToken: String?) {
        val providerId = "oidc.kakao-oidc.kakao" // As registered in Firebase console.

        if (idToken != null) {
            val credential = oAuthCredential(providerId) {
                setIdToken(idToken) // ID token from OpenID Connect flow.
            }

            Firebase.auth
                .signInWithCredential(credential)
                .addOnSuccessListener { authResult ->

                    Log.e(TAG, "signInWithCredential 성공함")

                    // User is signed in.

                    // IdP data available in:
                    //    authResult.additionalUserInfo.profile
                }
                .addOnFailureListener { e ->
                    // Handle failure.
                    Log.e(TAG, "signInWithCredential 실패 : $e")
                }
        } else {
            // 카카오 id 토큰 가져오기 실패
            Log.e(TAG, "카카오톡 id 토큰 가져오기 실패")
        }
    }

    /** 코드 흐름 (권장) */
    private fun loginWithKaKao() {

        //1. OAuthProvider 의 인스턴스를 생성
        val providerBuilder = OAuthProvider.newBuilder("oidc.kakao-second")

        //2. 보류 중인 결과가 있는지 확인
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {

                    Log.e(TAG, "pendingResultTask 존재함")
                    Log.e(TAG, "pendingResultTask 로 로그인 완료!")

                    // User is signed in.
                    // IdP data available in
                    // authResult.getAdditionalUserInfo().getProfile().
                    // The OAuth access token can also be retrieved:
                    // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                    // The OAuth secret can be retrieved by calling:
                    // ((OAuthCredential)authResult.getCredential()).getSecret().
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "pendingResultTask 실패 : $e")
                    // Handle failure.
                }
        } else {
            Log.e(TAG, "pendingResultTask null, 로그인 흐름 시작!")
            // There's no pending result so you need to start the sign-in flow.
            // See below.
        }

        //3. 로그인 흐름 시작
        auth
            .startActivityForSignInWithProvider(requireActivity(), providerBuilder.build())
            .addOnSuccessListener { authResult ->

                Log.e(TAG, "startActivityForSignInWithProvider 완료!")

                auth.signInWithCredential(authResult.credential!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.e(TAG, "signInWithCredential 완료!")
                            val userName = task.result.user?.displayName
                            Toast.makeText(requireContext(),"${userName}님 환영합니다.!!",Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "signInWithCredential 실패", task.exception)
                        }
                    }
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
//                 ((OAuthCredential)authResult.getCredential()).getAccessToken()
                // The OAuth secret can be retrieved by calling:
//                 ((OAuthCredential)authResult.getCredential()).getSecret()
            }
            .addOnFailureListener { e ->
                //TODO : 여기서 addOnCompleteListener 진행이 안됨(무조건 addOnFailureListener 발동됨...)
                //There was an internal error in the web widget. [ {"code":"auth/invalid-credential","message":"Error connecting to the given credential's issuer."} ]
                // TODO : 카카오말고 일단 이메일 링크, 구글 계정을 통한 가입은 됨.
                //  당장 포맷기능만을 구현하려면 이메일링크로만 가입을 하게 해놓자.
                //  Firebase auth 와 FireStore 를 연동하는 것부터 해보자...

                Log.e(TAG, "startActivityForSignInWithProvider 실패 : $e")
                // Handle failure.
            }
//
//        val providerId = "oidc.kakao-oidc.kakao" // As registered in Firebase console.
//        val idToken = getKaKaoTokens(requireContext())
//
//        if (idToken != null) {
//            val credential = oAuthCredential(providerId) {
//                setIdToken(idToken) // ID token from OpenID Connect flow.
//            }
//            Firebase.auth
//                .signInWithCredential(credential)
//                .addOnSuccessListener { authResult ->
//
//                    Log.e(TAG, "signInWithCredential 성공함")
//
//                    // User is signed in.
//
//                    // IdP data available in:
//                    //    authResult.additionalUserInfo.profile
//                }
//                .addOnFailureListener { e ->
//                    // Handle failure.
//                    Log.e(TAG, "signInWithCredential 실패 : $e")
//                }
//        } else {
//            // 카카오 id 토큰 가져오기 실패
//            Log.e(TAG, "카카오톡 id 토큰 가져오기 실패")
//        }
    }


    companion object {
        const val TAG = "로그인 조각"
        const val REQ_ONE_TAP = 23
    }


}