package com.example.a24

import android.app.Application
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository

class App : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }
}