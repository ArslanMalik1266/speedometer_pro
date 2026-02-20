package com.example.speedometerpro.util

sealed class DrawerDestination {
    data object History : DrawerDestination()
    data object Settings : DrawerDestination()
}