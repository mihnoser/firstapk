package ru.netology.nmedia.application

import android.app.Application
import ru.netology.nmedia.di.DependecyContainer

class NMediaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DependecyContainer.initApp(this)
    }
}