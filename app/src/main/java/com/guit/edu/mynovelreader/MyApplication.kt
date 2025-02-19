package com.guit.edu.mynovelreader

import android.app.Application

class MyApplication : Application() {

    lateinit var bookRepository: BookRepository
    lateinit var readingProgressRepository: ReadingProgressRepository

    override fun onCreate() {
        super.onCreate()
        bookRepository = BookRepository(this)
        readingProgressRepository = ReadingProgressRepository(this)
    }
}
