package com.guit.edu.mynovelreader

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(navController: NavHostController, books: MutableState<List<Book>>) {
    val openFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            addBook(it, books)
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
            items(books.value, key = { it.id }) { book ->
                BookListItem(book = book, navController = navController)
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("reader/${book.id}")
            },
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
}

private fun addBook(uri: Uri, books: MutableState<List<Book>>) {
    val newBook = Book(
        id = generateUniqueId(),
        title = uri.lastPathSegment ?: "无标题",
        fileUri = uri
    )
    books.value = books.value + newBook
}
private fun generateUniqueId(): Int {
    return UUID.randomUUID().hashCode()
    // return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}
