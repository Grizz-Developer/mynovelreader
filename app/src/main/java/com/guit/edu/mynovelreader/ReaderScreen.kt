package com.guit.edu.mynovelreader

import android.net.Uri
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Int,
    navController: NavHostController,
    books: MutableState<List<Book>>,
    onBackPressedDispatcherOwner: androidx.activity.OnBackPressedDispatcherOwner
) {
    val book = books.value.find { it.id == bookId }

    // 使用 remember 缓存文本和章节列表
    val text = remember { mutableStateOf<String?>(null) }
    val chapters = remember { mutableStateOf<List<Chapter>>(emptyList()) }

    val currentPage = remember { mutableStateOf(0) } // 当前页码
    val currentChapterIndex = remember { mutableStateOf(0) } // 当前章节索引
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val chapterListState = rememberLazyListState()
    val listState = rememberLazyListState()
    val readingProgressRepository =
        remember { ReadingProgressRepository(context) } // 使用 remember 创建 ReadingProgressRepository

    // 添加一个加载状态
    val isLoading = remember { mutableStateOf(true) }

    val onBackPressedDispatcher = onBackPressedDispatcherOwner.onBackPressedDispatcher

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    // 在 LaunchedEffect 中加载书籍内容和解析章节
    LaunchedEffect(key1 = bookId) {
        isLoading.value = true // 开始加载时设置为 true
        book?.let {
            scope.launch(Dispatchers.IO) {
                val uri = Uri.parse(it.fileUri)
                val content = Utils.readTextFromUri(context, uri)
                val parsedChapters = content?.let { parseChapters(it) } ?: emptyList()

                withContext(Dispatchers.Main) {
                    text.value = content
                    chapters.value = parsedChapters
                    isLoading.value = false // 加载完成后设置为 false
                }
            }
        } ?: run {
            isLoading.value = false // 如果 book 为 null，也设置为 false
        }
    }

    LaunchedEffect(key1 = drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Open) {
            // 当侧滑栏打开时，滚动到当前章节
            chapterListState.scrollToItem(currentChapterIndex.value)
        }
    }

    // 初始化阅读进度
    LaunchedEffect(key1 = bookId) {
        val chapterIndex = readingProgressRepository.getChapterIndex(bookId)
        val pageIndex = readingProgressRepository.getPageIndex(bookId)
        currentChapterIndex.value = chapterIndex
        currentPage.value = pageIndex
        // 确保在获取到初始页码后滚动到对应的位置
        snapshotFlow { currentPage.value }.collect { page ->
            listState.scrollToItem(page)
        }
    }

    // 保存阅读进度
    LaunchedEffect(currentChapterIndex.value, currentPage.value) {
        scope.launch(Dispatchers.IO) {
            val progress = ReadingProgress(
                bookId = bookId,
                chapterIndex = currentChapterIndex.value,
                page = currentPage.value
            )
            readingProgressRepository.saveReadingProgress(progress)
        }
    }

    // 使用 derivedStateOf 优化章节标题的更新
    val currentChapterTitle by remember(chapters.value, currentChapterIndex.value) {
        derivedStateOf {
            chapters.value.getOrNull(currentChapterIndex.value)?.title ?: "加载中..."
        }
    }

    if (isLoading.value) {
        // 显示加载动画
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (book != null && text.value != null && chapters.value.isNotEmpty()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        text = "章节列表",
                        modifier = Modifier.padding(16.dp),
                        style = TextStyle(fontSize = 20.sp)
                    )
                    Divider()
                    LazyColumn(
                        state = chapterListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(
                            items = chapters.value.indices.toList(),
                            key = { index -> index }
                        ) { index ->
                            val chapter = chapters.value[index]
                            Text(
                                text = chapter.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        currentChapterIndex.value = index
                                        currentPage.value = 0 // 切换章节时回到第一页
                                        scope.launch {
                                            listState.scrollToItem(0)
                                            drawerState.close()
                                        }
                                    },
                                style = TextStyle(fontSize = 18.sp)

                            )
                        }
                    }
                }
            }
        ) {
            Scaffold(topBar = {
                TopAppBar(title = {
                    Text(
                        text = currentChapterTitle, // 使用 derivedStateOf 优化的章节标题
                        style = TextStyle(fontSize = 20.sp)
                    )
                },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }) { paddingValues ->
                // 使用 remember 缓存章节文本，避免重复分割
                val chapterText = remember(text.value, currentChapterIndex.value) {
                    getChapterText(
                        text.value!!,
                        chapters.value.getOrNull(currentChapterIndex.value)
                    )
                }
                val pages = remember(chapterText) {
                    mutableStateOf(splitTextIntoPages(chapterText, 30))
                }

                // 更新当前页面,确保滚动位置正确
                LaunchedEffect(listState.firstVisibleItemIndex) {
                    currentPage.value = listState.firstVisibleItemIndex
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    items(
                        items = pages.value.indices.toList(),
                        key = { it }
                    ) { pageIndex ->
                        Text(
                            text = pages.value[pageIndex],
                            modifier = Modifier.padding(16.dp),
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "书本加载失败", style = TextStyle(fontSize = 20.sp))
        }
    }
}

private fun parseChapters(text: String): List<Chapter> {
    val chapterRegex =
        Regex("^(第[\\d一二三四五六七八九十百千]+章|Chapter\\s+\\d+)(.*)$", RegexOption.MULTILINE)
    val chapters = mutableListOf<Chapter>()
    var lastMatchEnd = 0
    var chapterIndex = 1
    chapterRegex.findAll(text).forEach { matchResult ->
        val chapterTitle = matchResult.groupValues[1].trim() // 获取章节号
        val chapterContent = matchResult.groupValues[2].trim() // 获取章节内容
        val start = matchResult.range.first
        val title = if (chapterContent.isBlank()) {
            chapterTitle
        } else {
            "$chapterTitle  $chapterContent"
        }
        val chapter = Chapter(title, lastMatchEnd, start - lastMatchEnd)
        chapters.add(chapter)
        lastMatchEnd = matchResult.range.last + 1
        chapterIndex++
    }

    if (lastMatchEnd < text.length) {
        chapters.add(Chapter("第${chapterIndex}章", lastMatchEnd, text.length - lastMatchEnd))
    }
    return chapters
}

private fun getChapterText(text: String, currentChapter: Chapter?): String {
    return currentChapter?.let {
        text.substring(it.start, it.start + it.length)
    } ?: text
}

private fun splitTextIntoPages(text: String, linesPerPage: Int): List<String> {
    val lines = text.lines()
    val pages = mutableListOf<String>()
    for (i in 0 until lines.size step linesPerPage) {
        val pageLines = lines.subList(i, minOf(i + linesPerPage, lines.size))
        pages.add(pageLines.joinToString(separator = "\n"))
    }
    return pages
}
