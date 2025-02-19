package com.guit.edu.mynovelreader

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey val bookId: Int,
    val chapterIndex: Int,
    val page: Int
)
