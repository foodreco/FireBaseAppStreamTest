package com.dreamreco.firebaseappstreamtest.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.dreamreco.firebaseappstreamtest.MyDate
import com.dreamreco.firebaseappstreamtest.MyDrink
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.io.ByteArrayOutputStream


class Converters {
    @TypeConverter
    fun toByteArray(bitmap: Bitmap?): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return byteArray?.size?.let { BitmapFactory.decodeByteArray(byteArray, 0, it) }
    }

    @TypeConverter
    fun fromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun toString(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun listToJson(value: List<String>?) : String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String?): List<String> {
        return Gson().fromJson(value, Array<String>::class.java).toList()
    }

    @TypeConverter
    fun myDrinkToJson(value : MyDrink?): String? {
        return if(value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToMyDrink(value: String?): MyDrink? {
        return if (value == null) null else Gson().fromJson(value, MyDrink::class.java)
    }

    @TypeConverter
    fun myDateToJson(value: MyDate): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToMyDate(value: String): MyDate {
        return Gson().fromJson(value, MyDate::class.java)
    }

}

// entity 에 data class 를 넣기 위해 필요
@ProvidedTypeConverter
class MyDateTypeConverter(private val gson: Gson) {

//    @TypeConverter
//    fun listToJson(value: MyDate): String {
//        return gson.toJson(value)
//    }
//
//    @TypeConverter
//    fun jsonToList(value: String): MyDate {
//        return gson.fromJson(value, MyDate::class.java)
//    }
}

// entity 에 data class 를 넣기 위해 필요 #2
@ProvidedTypeConverter
class MyDrinkTypeConverter(private val gson: Gson) {

//    @TypeConverter
//    fun listToJson(value : MyDrink?): String? {
//        return if(value == null) null else gson.toJson(value)
//    }
//
//    @TypeConverter
//    fun jsonToList(value: String?): MyDrink? {
//        return if (value == null) null else gson.fromJson(value, MyDrink::class.java)
//    }
}

@ProvidedTypeConverter
class CalendarDayTypeConverter(private val gson: Gson) {

    @TypeConverter
    fun listToJson(value: CalendarDay): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): CalendarDay {
        return gson.fromJson(value, CalendarDay::class.java)
    }
}
