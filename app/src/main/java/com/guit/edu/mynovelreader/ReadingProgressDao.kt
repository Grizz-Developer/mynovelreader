package com.guit.edu.mynovelreader

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    fun getReadingProgress(bookId: Int): Flow<ReadingProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(progress: ReadingProgress)

    @Update
    suspend fun updateReadingProgress(progress: ReadingProgress)
    @Delete
    suspend fun deleteReadingProgress(progress: ReadingProgress)

    @Query("SELECT chapterIndex FROM reading_progress WHERE bookId = :bookId")
    suspend fun getChapterIndex(bookId: Int): Int

    @Query("SELECT page FROM reading_progress WHERE bookId = :bookId")
    suspend fun getPageIndex(bookId: Int): Int
}
