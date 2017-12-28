package com.example.envoy.kotlincoroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    //https://proandroiddev.com/android-coroutine-recipes-33467a4302e9
    //https://github.com/kotlin/kotlinx.coroutines
    //https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md#launch-ui-coroutine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        center_button.setOnClickListener() { handleClickListener(it) }
    }

    private fun handleClickListener(it: View) {
        when (it.id) {
            R.id.center_button -> launchDelayNoUI()
            else -> println("Unhandled view click listener")
        }
    }

    private fun launchDelayNoUI(longDelay: Boolean) {
        launch() {
            var delayTime: Long = if (longDelay) 10000 else 5000
            Log.i("Coroutines", "starting launchDelayNoUI coroutine with $delayTime delay")
            delay(delayTime)
            Log.i("Coroutines", "launchDelayNoUI with $delayTime delay is complete")
        }
    }

    private fun launchDelayWithToastOnCompletion(longDelay: Boolean) {
        launch() {
            var delayTime: Long = if (longDelay) 10000 else 5000
            Log.i("Coroutines", "starting launchSmallDelayWithToastOnCompletion coroutine with $delayTime delay")
            delay(delayTime)
            Log.i("Coroutines", "launchSmallDelayWithToastOnCompletion with $delayTime delay is complete")
            Toast.makeText(this@MainActivity, "launchSmallDelayWithToastOnCompletion with $delayTime delay is complete", Toast.LENGTH_LONG).show()
        }
    }

    private fun combiningTasksSequentially() {
        val timeToCompletion = measureTimeMillis {
            launch {
                Log.i("Coroutines", "combiningTasksSequentially() coroutine started")
                val firstTask = doSomethingUsefulFiveSeconds()
                val secondTask = doSomethingUsefulTenSeconds()
                Log.i("Coroutines", "combiningTasksSequentially() coroutine ended with values $firstTask & $secondTask")
            }
        }
        Log.i("Coroutines", "combiningTasksSequentially() completed in $timeToCompletion milliseconds")
    }

    private fun combiningTasksAsynchronously() {
        launch {
            val timeToCompletion = measureTimeMillis {
                Log.i("Coroutines", "combiningTasksAsynchronously() coroutines started")
                val firstTask : Deferred<Int> = async { doSomethingUsefulFiveSeconds() }  //Could use implicit on the Deferred.
                val secondTask = async { doSomethingUsefulTenSeconds() }
                //the await function is part of the Deferred class which is returned by the async coroutine.
                Log.i("Coroutines", "combiningTasksAsynchronously() coroutines ended with values ${firstTask.await()} & ${secondTask.await()} ")
            }
            Log.i("Coroutines", "combiningTasksSequentially() completed in $timeToCompletion milliseconds")
        }

    }

      fun doSomethingUsefulFiveSeconds(): Int {
        Log.i("Coroutines", "doSomethingUsefulFiveSeconds started")
        delay(5000) // pretend we are doing something useful here
        Log.i("Coroutines", "doSomethingUsefulFiveSeconds ended")
        return 5
    }

    suspend fun doSomethingUsefulTenSeconds(): Int {
        Log.i("Coroutines", "doSomethingUsefulTenSeconds started")
        delay(10000) // pretend we are doing something useful here
        Log.i("Coroutines", "doSomethingUsefulTenSeconds ended")
        return 10
    }


}
