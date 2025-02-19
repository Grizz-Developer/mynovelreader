package com.guit.edu.mynovelreader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(navController: NavHostController, books: MutableState<List<Book>>) {
    val context = LocalContext.current
    val bookRepository = (context.applicationContext as MyApplication).bookRepository
    val coroutineScope = rememberCoroutineScope() // 使用 rememberCoroutineScope
    val openFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // 将调用 addBook 的操作移动到 LaunchedEffect 中
        if (uri != null) {
            // 获取持久权限
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            coroutineScope.launch {
                val newBook = Book(
                    title = uri.lastPathSegment ?: "无标题",
                    fileUri = uri.toString()
                )
                bookRepository.insertBook(newBook)
            }
        }
    }

    LaunchedEffect(key1 = true) {
        bookRepository.allBooks.collect {
            books.value = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "我的书架", style = TextStyle(fontSize = 20.sp))
                    Spacer(modifier = Modifier.weight(1f))
                }
            },
                actions = {
                    IconButton(onClick = { openFileLauncher.launch(arrayOf("text/*")) }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    }
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .safeDrawingPadding() // 添加 safeDrawingPadding
        ) {
            BookList(books, navController)
        }
    }
}

@Composable
fun BookList(books: MutableState<List<Book>>, navController: NavHostController) {
    if (books.value.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "书架空空如也", style = TextStyle(fontSize = 20.sp))
        }
    } else {
        LazyColumn {
            items(
                items = books.value,
                key = { it.id }
            ) { book ->
                BookListItem(book = book, navController = navController, books = books)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookListItem(book: Book, navController: NavHostController, books: MutableState<List<Book>>) {
    val context = LocalContext.current
    val bookRepository = (context.applicationContext as MyApplication).bookRepository
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate("reader/${book.id}")
                },
                onLongClick = {
                    showMenu = true
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book.title,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 18.sp)
            )
        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("删除") },
            onClick = {
                coroutineScope.launch {
                    bookRepository.deleteBook(book)
                }
                books.value = books.value.toMutableList().also {
                    it.remove(book)
                }
                showMenu = false
            }
        )
    }
}
