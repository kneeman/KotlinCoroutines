package com.example.envoy.kotlincoroutines.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Person::class), version = 1)
abstract class PersonDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
}