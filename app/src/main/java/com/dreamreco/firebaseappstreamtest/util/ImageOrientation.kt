package com.dreamreco.firebaseappstreamtest.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

object ImageOrientation {
    @Throws(IOException::class)
    fun modifyOrientation(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {

        var exif: ExifInterface? = null

        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.run {
                exif = ExifInterface(this)
                inputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val orientation =
            exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}