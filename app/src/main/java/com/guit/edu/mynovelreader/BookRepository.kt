package com.guit.edu.mynovelreader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BookRepository(context: Context) {
    private val bookDao = AppDatabase.getDatabase(context).bookDao()

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun insertBook(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.insertBook(book)
        }
    }

    suspend fun deleteBook(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.deleteBook(book)
        }
    }
    suspend fun getBookById(bookId: Int): Book? {
        return withContext(Dispatchers.IO){
            bookDao.getBookById(bookId)
        }
    }
}
