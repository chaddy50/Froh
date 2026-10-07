package com.chaddy50.froh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Stable
class AppNavigator(val backStack: NavBackStack<NavKey>) {
    val currentKey: NavKey? get() = backStack.lastOrNull()
    val canGoBack: Boolean get() = backStack.size > 1

    fun push(route: NavKey) {
        backStack.add(route)
    }

    fun pop() {
        backStack.removeLastOrNull()
    }
}

@Composable
fun rememberAppNavigator(startDestination: NavKey): AppNavigator {
    val backStack = rememberNavBackStack(startDestination)
    return remember(backStack) { AppNavigator(backStack) }
}
