package com.example.envoy.kotlincoroutines

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.envoy.kotlincoroutines.room.Person
import kotlinx.android.synthetic.main.activity_image_load.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by Envoy on 1/24/18.
 */
class RoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_load)
        addPerson(Person(0, firstName = "Craig", lastName = "Knee"))
    }

    private fun addPerson(person: Person) {
        var people : List<Person>? = null
        val job = launch {
            MyApp.database?.personDao()?.insert(person)
        }

        launch {
            job.join()
            people = MyApp.database?.personDao()?.getAllPeople()
        }

        Log.i("Room", "People: ${people.toString()}")

    }

//    private fun loadLoremPixelImage() {
//        launch() {
//            val uri = Uri.parse("http://lorempixel.com/600/600/sports/")
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//            launch(UI) {
//                imageViewLoad.setImageBitmap(bitmap)
//            }
//        }
//    }
}