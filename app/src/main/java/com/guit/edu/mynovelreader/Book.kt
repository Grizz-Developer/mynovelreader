package com.guit.edu.mynovelreader

import android.net.Uri

data class Book(
    val id: Int,
    val title: String,
    val fileUri: Uri
)
