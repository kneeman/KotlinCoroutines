package com.example.envoy.kotlincoroutines.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Person(
        @PrimaryKey(autoGenerate = true)
        val uid: Long,
        val firstName: String = "",
        val lastName: String = ""
)