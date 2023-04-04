package com.dreamreco.firebaseappstreamtest.ui.fireStorage

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentStorageBinding
import com.dreamreco.firebaseappstreamtest.ui.fireStorage.MakeFileByUri.getFile
import com.dreamreco.firebaseappstreamtest.util.CAMERA_PERMISSION
import com.dreamreco.firebaseappstreamtest.util.ImageOrientation
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.net.URI
import java.text.SimpleDateFormat


@AndroidEntryPoint
class StorageFragment : Fragment() {

    private val binding by lazy { FragmentStorageBinding.inflate(layoutInflater) }
    private val storageViewModel by viewModels<StorageViewModel>()

    private var mPhotoUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private val storageRef = Firebase.storage.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        with(binding) {
            btnCamera.setOnClickListener {
                getImageFromGalleryOrCamera(CAMERA_PERMISSION)
            }
            btnGallery.setOnClickListener {
                getImageFromGalleryOrCamera(GET_DATA_PERMISSIONS)
            }

            // 프로필 이미지를 업로드하는 코드
            btnTest.setOnClickListener {
                uploadImageToStorage(user.uid)
            }

            /** 프로필 이미지 print 코드 */
            try {
                val photo = user.photoUrl
                Glide.with(requireContext()).load(photo).into(binding.diaryImageView)
                Log.e(TAG, "가져온 photoUrl : $photo")
            } catch (e: Exception) {
                Log.e(TAG, "유저 이미지 가져오기 실패", e)
            }
        }

        return binding.root
    }

    // 권한 허용 및 카메라 작동 코드
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getImageFromGalleryOrCamera(
        permissions: Array<String>,
        activatedByWidget: Boolean = false
    ) {
        when (permissions) {
            GET_DATA_PERMISSIONS -> {
                if (!checkNeedPermissionBoolean(permissions)) {
                    // 허용 안되어 있는 경우, 요청
                    requestMultiplePermissionsForGallery.launch(
                        permissions
                    )
                } else {
                    // 허용 되어있는 경우, 코드 작동
                    // 갤러리 작동
                    getImageFromGallery()
                }
            }
            CAMERA_PERMISSION -> {
                // 위젯에서 실행된 경우,
                if (!checkNeedPermissionBoolean(permissions)) {
                    // 허용 안되어 있는 경우, 요청
                    requestMultiplePermissionsForCamera.launch(
                        permissions
                    )
                } else {
                    // 허용 되어있는 경우, 코드 작동
                    // 카메라 작동
                    getImageFromCamera()
                }
            }
        }
    }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<String>): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // 허용 요청 코드 및 작동 #1(갤러리 가져오기)
    @RequiresApi(Build.VERSION_CODES.P)
    private val requestMultiplePermissionsForGallery =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_access),
                    Toast.LENGTH_SHORT
                ).show()
                // 갤러리 작동
                getImageFromGallery()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_gallery),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("IntentReset")
    private fun getImageFromGallery() {
        try {
            // 지속 권한을 얻기 위해서 ACTION_OPEN_DOCUMENT 으로 접속
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            getImageFromGallery.launch(intent) // 여기서 오류발생??

        } catch (e: Exception) {
            Log.e("디테일다이알로그", "getImageFromGallery 에러발생 : $e")
        }
    }

    // 허용 요청 코드 및 작동 #2(카메라 사용하기)
    @RequiresApi(Build.VERSION_CODES.P)
    private val requestMultiplePermissionsForCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_access),
                    Toast.LENGTH_SHORT
                ).show()
                // 카메라 작동
                getImageFromCamera()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // Uri 접속 영구 허가 코드
                it.data?.data.let { uri ->
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                mPhotoUri = it.data?.data as Uri
                setImageFromUri()
            }
        }

    @SuppressLint("SimpleDateFormat")
    private fun newJpgFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getImageFromCamera(activatedByWidget: Boolean = false) {
        try {
            // 원본 사진 공용 저장소 특정 폴더에 저장
            val values = ContentValues()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 이상 적용
                values.apply {
                    // 저장 파일 이름 설정
                    put(MediaStore.MediaColumns.DISPLAY_NAME, newJpgFileName())
                    // 저장 타입 설정
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    // 저장 경로 설정
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "Pictures/${getString(R.string.app_name)}"
                    )
                }
            } else {
                values.apply {
                    // 저장 파일 이름 설정
                    put(MediaStore.Images.Media.DISPLAY_NAME, newJpgFileName())
                    // 저장 타입 설정
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                    // 구버전은 경로 저장 어케함??
                }
            }

            mPhotoUri =
                requireContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri)
                // uri 영구 접속 허용 코드(둘다 설정)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cameraAndSaveFile.launch(takePictureIntent)
            }
        } catch (e: Exception) {
            Log.e("디테일다이알로그", "getImageFromCamera 에러 : $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val cameraAndSaveFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                setImageFromUri()
                Log.e("디테일다이알로그", "cameraAndSaveFile.launch(takePictureIntent) RESULT_OK 됨")
            } else {
                Log.e("디테일다이알로그", "cameraAndSaveFile.launch(takePictureIntent) else 발생임")
            }
        }

    // 새로운 이미지를 불러올 때 최종 작동하는 코드
    private fun setImageFromUri() {
        // 원본 사진은 지정 경로에 저장됨.
        Log.e(TAG, "가져온 이미지 Uri : $mPhotoUri")
        try {
            with(binding) {
                diaryImageView.imageTintList = null
                // TODO : 사용자가 사진 잘라서 지정 가능하게??

                val reducedBitmap = mPhotoUri?.let {
                    decodeSampledBitmapFromInputStream(
                        it,
                        200,
                        200,
                        requireContext()
                    )
                }
                diaryImageView.setImageBitmap(reducedBitmap)

                // here i override the original image file
                val mFile = mPhotoUri?.let { getFile(requireContext(), it) }
                mFile?.createNewFile()

                val fileSize = mFile?.length()

                Log.e(TAG, "mFile?.path 1: ${mFile?.path}")
                Log.e(TAG, "mFile?.length() 1: $fileSize")

                // 50KB 제한
                if ((fileSize != null) && (fileSize > 50000)) {
                    Log.e(TAG, "축소 진행함")
                    val outputStream = FileOutputStream(mFile)
                    reducedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                    Log.e(TAG, "mFile?.path 2: ${mFile?.path}")
                    Log.e(TAG, "mFile?.length() 2: ${mFile?.length()}")
                }
            }

            //TODO : 생성 완료 후(굿), 업로드 그리고 삭제까지 할 것


//            val fileDescriptor: AssetFileDescriptor? =
//                mPhotoUri?.let { getApplicationContext().contentResolver.openAssetFileDescriptor(it, "r") }
//
//            val fileSize = fileDescriptor?.length
//            Log.e(TAG, "그냥 파일용량 : $fileSize")
//
//            val originFile = mPhotoUri?.let { getFile(requireContext(), it) }
//            if (originFile != null) {
//                Log.e(TAG, "그냥 파일용량2 : ${originFile.length()}")
//                val result : File? = reduceSizeOfFile(originFile)
//                Log.e(TAG, "작업 후 파일용량 : ${result?.length()}")
//            }
//
//            if (fileSize != null) {
//                // 100KB 초과 시 크기 줄임
//                if (fileSize > 100000) {
//
//                } else {
//
//                }
//            }
//
//
//            val juri = URI(mPhotoUri.toString())
//            val mediaFile = File(juri.path)
//
////            val myFile = Uri.(mPhotoUri.toString())

        } catch (
            e: Exception
        ) {
            Log.e("디테일다이알로그", "setImageFromUri 에러 : $e")
        }
    }

    /** 이미지 업로드, profile photoUrl 변경 코드 */
    fun uploadImageToStorage(fileName: String) {
        try {
            if ((mPhotoUri != null) && (user.photoUrl != mPhotoUri)) {
                // Storage 저장 폴더
                val storagePath = "profileImages"

                // 기존에 존재하던 파일 삭제
                storageRef.child("$storagePath/$fileName.jpg").delete()

                // 메타 데이터 추가
                val metadata = storageMetadata {
                    contentType = "image/jpg"
                }

                // 로컬파일에서 업로드
                val profileImageRef = storageRef.child("$storagePath/$fileName.jpg")
//                val uploadTask = profileImageRef.putFile(mPhotoUri!!, metadata)

                // 업로트 파일 용량 제한
                val mFile = mPhotoUri?.let { getFile(requireContext(), it) }
                mFile?.createNewFile()

                val fileSize = mFile?.length()

                Log.e(TAG, "mFile?.path 1: ${mFile?.path}")
                Log.e(TAG, "mFile?.length() 1: $fileSize")

                // 50KB 제한
                if ((fileSize != null) && (fileSize > 50000)) {
                    Log.e(TAG, "축소 진행함")
                    val outputStream = FileOutputStream(mFile)
                    val reducedBitmap = mPhotoUri?.let {
                        decodeSampledBitmapFromInputStream(
                            it,
                            200,
                            200,
                            requireContext()
                        )
                    }
                    reducedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    Log.e(TAG, "mFile?.path 2: ${mFile?.path}")
                    Log.e(TAG, "mFile?.length() 2: ${mFile?.length()}")
                }

                val stream = FileInputStream(mFile)

//                val uploadTask = profileImageRef.putFile(resultUri, metadata)

                val uploadTask = profileImageRef.putStream(stream, metadata)
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener { e ->
                    // Handle unsuccessful uploads
                    Log.e(TAG, "uploadTask 실패", e)
                }.addOnSuccessListener { taskSnapshot ->
                    Log.e(TAG, "uploadTask 완료됨!")
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    // ...
                    /** 업로드 완료 시, metaData 에 url 추가 */
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        profileImageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            Log.e(TAG, "downloadUri : $downloadUri")
                            val profileUpdates = userProfileChangeRequest {
                                photoUri = downloadUri
                            }
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Log.d(TAG, "User profile updated.")
                                        // 업로드 완료 시, 파일 삭제
                                        Log.e(TAG, "업로드 완료, 파일 삭제합니다.")
                                        mFile?.delete()
                                    } else {
                                        Log.e(TAG, "User profile updated failed!")
                                    }
                                }
                        } else {
                            // Handle failures
                            // ...
                            Log.e(TAG, "addOnCompleteListener 실패함")
                        }
                    }
                }
            } else {
                Log.e(TAG, "uploadImageToStorage 실패 : mPhotoUri 없음")
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImageToStorage 실패", e)
        }
    }

    companion object {
        private const val TAG = "StorageFragment"
    }

}


///topLine//////topLine//////topLine//////topLine//////topLine//////topLine//////topLine///
///topLine//////topLine//////topLine//////topLine//////topLine//////topLine//////topLine///
///topLine//////topLine//////topLine//////topLine//////topLine//////topLine//////topLine///


// Permissions
val GET_DATA_PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

// uri 를 받아 이미지를 원하는 사이즈 이하로 줄여주는 코드
fun decodeSampledBitmapFromInputStream(
    photoUri: Uri,
    reqWidth: Int,
    reqHeight: Int,
    context: Context
): Bitmap {
    var fileInputStream: InputStream =
        context.contentResolver.openInputStream(photoUri)!!

    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeStream(fileInputStream, null, this)

        val photoW: Int = outWidth
        val photoH: Int = outHeight

        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        fileInputStream.close()

        fileInputStream = context.contentResolver.openInputStream(photoUri)!!

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        // 이미지 회전을 본래대로 반영하는 코드
        val result = ImageOrientation.modifyOrientation(
            context,
            BitmapFactory.decodeStream(fileInputStream, null, this)!!,
            photoUri
        )

        fileInputStream.close()

        result
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    // 불러온 이미지의 폭, 넓이
    val (height: Int, width: Int) = options.run { outHeight to outWidth }

    var inSampleSize = 1

    // 이미지 크기가 기준 초과하는 경우
    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (true) {
            inSampleSize *= 2
            if ((halfHeight / inSampleSize < reqHeight) && (halfWidth / inSampleSize < reqWidth)) {
                break
            }
        }

    }

    // 배수만큼 본 사이즈를 줄임
    return inSampleSize
}

/** Uri 로 File 을 만드는 코드 */
object MakeFileByUri {
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(
                    ins!!,
                    destinationFilename
                )
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
        return destinationFilename
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}


fun reduceSizeOfFile(file: File): File? {
    return try {
        // BitmapFactory options to downsize the image
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        o.inSampleSize = 6
        // factor of downsizing the image
        var inputStream = FileInputStream(file)
        //Bitmap selectedBitmap = null;
        BitmapFactory.decodeStream(inputStream, null, o)
        inputStream.close()

        // The new size we want to scale to
        val REQUIRED_SIZE = 20

        // Find the correct scale value. It should be the power of 2.
        var scale = 1

        while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
            o.outHeight / scale / 2 >= REQUIRED_SIZE
        ) {
            Log.e("reduceSizeOfFile", "o.outWidth : ${o.outWidth}")
            Log.e("reduceSizeOfFile", "o.outHeight : ${o.outHeight}")
            Log.e("reduceSizeOfFile", "scale : $scale")
            scale *= 2
        }


        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        inputStream = FileInputStream(file)
        val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
        inputStream.close()

        // here i override the original image file
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        file
    } catch (e: java.lang.Exception) {
        null
    }
}