package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.FoamRepository

class FoamCounterApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { FoamRepository(database.batchDao(), database.scanDao()) }
}
