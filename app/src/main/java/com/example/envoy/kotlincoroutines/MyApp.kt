package com.example.envoy.kotlincoroutines

import android.app.Application
import android.arch.persistence.room.Room
import com.example.envoy.kotlincoroutines.room.PersonDatabase

class MyApp : Application() {

    companion object {
        var database: PersonDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        MyApp.database =  Room.databaseBuilder( this, PersonDatabase::class.java, "room-coroutine-poc").build()
    }
}