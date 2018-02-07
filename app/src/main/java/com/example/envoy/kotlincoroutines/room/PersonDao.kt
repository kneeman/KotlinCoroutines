package com.example.envoy.kotlincoroutines.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface PersonDao {

    @Insert
    fun insert(person: Person)

    @Query("SELECT * FROM person")
    fun getAllPeople(): List<Person>
}