package com.guit.edu.mynovelreader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReadingProgressRepository(context: Context) {
    private val readingProgressDao = AppDatabase.getDatabase(context).readingProgressDao()

    fun getReadingProgress(bookId: Int): Flow<ReadingProgress?> {
        return readingProgressDao.getReadingProgress(bookId)
    }

    suspend fun saveReadingProgress(progress: ReadingProgress) {
        withContext(Dispatchers.IO) {
            readingProgressDao.insertReadingProgress(progress)
        }
    }
    suspend fun getChapterIndex(bookId: Int):Int{
        return withContext(Dispatchers.IO){
            readingProgressDao.getChapterIndex(bookId)
        }
    }
    suspend fun getPageIndex(bookId: Int):Int{
        return withContext(Dispatchers.IO){
            readingProgressDao.getPageIndex(bookId)
        }
    }
}
