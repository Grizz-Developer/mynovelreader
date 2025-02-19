package com.guit.edu.mynovelreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import android.graphics.Color
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置状态栏和导航栏沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
            val books = remember { mutableStateOf(listOf<Book>()) }
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "shelf") {
                composable("shelf") {
                    ShelfScreen(navController = navController, books = books)
                }
                composable(
                    "reader/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                    ReaderScreen(
                        bookId = bookId,
                        navController = navController,
                        books = books,
                        onBackPressedDispatcherOwner = onBackPressedDispatcherOwner!!
                    )
                }
            }
        }
    }
}
