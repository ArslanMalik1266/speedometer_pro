package com.webscare.speedometer.util

sealed class DrawerDestination {
    data object History : DrawerDestination()
    data object Settings : DrawerDestination()
}