package com.example.bookloginapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookloginapp.model.Book
import com.example.bookloginapp.ui.screens.*
import com.example.bookloginapp.viewmodel.AuthViewModel
import com.example.bookloginapp.viewmodel.BooksViewModel
import com.example.bookloginapp.viewmodel.SharedBookViewModel

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Login : Screen("login", "Login", { Icon(Icons.Default.Person, contentDescription = "Login") })
    object Home : Screen("home", "Inicio", { Icon(Icons.Default.Home, contentDescription = "Inicio") })
    object Search : Screen("search", "Buscar", { Icon(Icons.Default.Search, contentDescription = "Buscar") })
    object Favorites : Screen("favorites", "Favoritos", { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") })
    object Profile : Screen("profile", "Perfil", { Icon(Icons.Default.Person, contentDescription = "Perfil") })
    object BookDetail : Screen("book_detail", "Detalle", { Icon(Icons.Default.Home, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val booksViewModel: BooksViewModel = viewModel()
    val sharedBookViewModel: SharedBookViewModel = viewModel()

    val startDestination = if (authViewModel.isUserLoggedIn()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    val bottomNavigationItems = listOf(
        Screen.Search,
        Screen.Favorites,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = remember(currentRoute) {
        bottomNavigationItems.any { currentRoute == it.route }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon() },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    booksViewModel = booksViewModel,
                    onBookClick = { book ->
                        sharedBookViewModel.setSelectedBook(book)
                        navController.navigate(Screen.BookDetail.route)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    booksViewModel = booksViewModel,
                    onBookClick = { book ->
                        sharedBookViewModel.setSelectedBook(book)
                        navController.navigate(Screen.BookDetail.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.BookDetail.route) {
                val selectedBook = sharedBookViewModel.selectedBook.value
                if (selectedBook != null) {
                    BookDetailScreen(
                        book = selectedBook,
                        onBackClick = { navController.navigateUp() },
                        booksViewModel = booksViewModel
                    )
                } else {
                    // Fallback en caso de que no haya un libro seleccionado
                    navController.navigateUp()
                }
            }
        }
    }
}