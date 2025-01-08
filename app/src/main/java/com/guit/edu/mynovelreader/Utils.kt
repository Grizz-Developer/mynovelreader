package com.guit.edu.mynovelreader

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.InputStream

object Utils {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val charset = CharsetDetector.detectCharset(inputStream)
            val text = CharsetDetector.readText(context.contentResolver.openInputStream(uri)!!, charset)
            return text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
