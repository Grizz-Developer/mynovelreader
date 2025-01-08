package com.guit.edu.mynovelreader

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

object CharsetDetector {
    private const val BUFFER_SIZE = 4096
    private const val DEFAULT_CHARSET = "UTF-8"

    fun detectCharset(inputStream: InputStream): String {
        val buffer = ByteArray(BUFFER_SIZE)
        val bytesRead = inputStream.read(buffer)
        if (bytesRead == -1) {
            return DEFAULT_CHARSET; // Handle empty stream
        }

        // UTF-8 BOM detection
        if (bytesRead >= 3 && (buffer[0].toInt() and 0xFF) == 0xEF && (buffer[1].toInt() and 0xFF) == 0xBB && (buffer[2].toInt() and 0xFF) == 0xBF) {
            return "UTF-8"
        }
        // UTF-16 Big Endian BOM detection
        if(bytesRead >= 2 && (buffer[0].toInt() and 0xFF) == 0xFE && (buffer[1].toInt() and 0xFF) == 0xFF){
            return "UTF-16BE"
        }

        // UTF-16 Little Endian BOM detection
        if (bytesRead >= 2 && (buffer[0].toInt() and 0xFF) == 0xFF && (buffer[1].toInt() and 0xFF) == 0xFE) {
            return "UTF-16LE"
        }

        // 高频字符检测（简化的版本）
        // 假设如果大多数字符都在ASCII码范围内，那么有可能是GBK或者UTF-8
        var isAscii = true
        for(i in 0 until bytesRead){
            if((buffer[i].toInt() and 0xFF) > 127){
                isAscii = false
                break
            }
        }

        return if(isAscii){
            DEFAULT_CHARSET
        }else{
            "GBK"
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Throws(IOException::class)
    fun readText(inputStream: InputStream, charsetName: String): String {
        var byteArrayInputStream: ByteArrayInputStream? = null
        try {
            byteArrayInputStream = ByteArrayInputStream(inputStream.readAllBytes())
            val charset = Charset.forName(charsetName)
            val reader = InputStreamReader(byteArrayInputStream, charset)
            val sb = StringBuilder()
            val buffer = CharArray(BUFFER_SIZE)
            var read: Int
            while (reader.read(buffer).also { read = it } != -1) {
                sb.append(buffer, 0, read)
            }
            return sb.toString()
        } finally {
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close()
            }
        }
    }
}
