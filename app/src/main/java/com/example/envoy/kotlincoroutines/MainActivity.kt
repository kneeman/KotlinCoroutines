package com.example.envoy.kotlincoroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    //https://proandroiddev.com/android-coroutine-recipes-33467a4302e9
    //https://github.com/kotlin/kotlinx.coroutines
    //https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md#launch-ui-coroutine
    //https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#coroutine-basics
    //https://proandroiddev.com/android-coroutine-recipes-33467a4302e9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_simple_coroutine_noui.setOnClickListener { handleClickListener(it) }
        button_coroutine_update_ui.setOnClickListener { launchDelayWithUIActionOnCompletion(false) }
        button_combine_tasks_sequentially.setOnClickListener { combiningTasksSequentially() }
        button_combine_tasks_asynchronously.setOnClickListener { combiningTasksAsynchronously() }
        button_run_blocking.setOnClickListener({runBlockingCoroutine(true)})
        button_contexts.setOnClickListener{coroutineContexts()}
        button_disrespectul.setOnClickListener { willNotRespectCancel() }
        button_respectul.setOnClickListener { willRespectCancel() }

    }

    private fun handleClickListener(v: View) {
        when (v.id) {
            R.id.button_simple_coroutine_noui -> launchDelayNoUI(false)
            else -> println("Unhandled view click listener")
        }
    }

    private fun launchDelayNoUI(longDelay: Boolean) {
        //
        launch {
            val delayTime: Long = if (longDelay) 10000 else 5000
            Log.i("Coroutines", "starting launchDelayNoUI coroutine with $delayTime delay")
            delay(delayTime)
            Log.i("Coroutines", "launchDelayNoUI with $delayTime delay is complete")
        }
    }

    private fun launchDelayWithUIActionOnCompletion(longDelay: Boolean) {
        launch(UI) {
            val delayTime: Long = if (longDelay) 10000 else 5000
            Log.i("Coroutines", "starting launchSmallDelayWithToastOnCompletion coroutine with $delayTime delay")
            delay(delayTime)
            Log.i("Coroutines", "launchSmallDelayWithToastOnCompletion with $delayTime delay is complete")
            //Toast.makeText(this@MainActivity, "launchSmallDelayWithToastOnCompletion with $delayTime delay is complete", Toast.LENGTH_LONG).show()
            button_coroutine_update_ui.text = "My delay is finished"
            delay(delayTime)
            button_coroutine_update_ui.text = getString(R.string.coroutine_update_ui_text)
        }
    }

    private fun combiningTasksSequentially() {
        val timeToCompletion = measureTimeMillis {
            launch {
                Log.i("Coroutines", "combiningTasksSequentially() coroutine started")
                //Here we are extracting out some functionality to another method.  Note this method uses the 'suspend' keyword
                val firstTask = doSomethingUsefulFiveSeconds()
                val secondTask = doSomethingUsefulTenSeconds()
                //This method does not need any additional keyword
                logSomething()
                Log.i("Coroutines", "combiningTasksSequentially() coroutine ended with values $firstTask & $secondTask")
            }
        }
        Log.i("Coroutines", "combiningTasksSequentially() completed in $timeToCompletion milliseconds")
    }


    //Launch vs Async - Launch returns a job, while async returns a deffered.  As seen below, they can be combined.
    //Example of Deffered
    private fun combiningTasksAsynchronously() {
        launch {
            val timeToCompletion = measureTimeMillis {
                Log.i("Coroutines", "combiningTasksAsynchronously() coroutines started")
                val firstTask: Deferred<Int> = async { doSomethingUsefulFiveSeconds() }  //Could use implicit on the Deferred.
                val secondTask = async { doSomethingUsefulTenSeconds() }
                //the await function is part of the Deferred class which is returned by the async coroutine.
                Log.i("Coroutines", "combiningTasksAsynchronously() coroutines ended with values ${firstTask.await()} & ${secondTask.await()} ")
            }
            Log.i("Coroutines", "combiningTasksSequentially() completed in $timeToCompletion milliseconds")
        }

    }

    //Example of a runBlockingCoroutine.  These run on the main thread and block that thread until completion
    private fun runBlockingCoroutine(longDelay: Boolean) {
        runBlocking {
            val delayTime: Long = if (longDelay) 10000 else 5000
            Log.i("Coroutines", "starting runBlocking coroutine with $delayTime delay")
            delay(delayTime)
            Log.i("Coroutines", "runBlocking with $delayTime delay is complete")
        }
    }


    //Coroutines do run on threads based on their context, which you can supply when you create them.
    private fun coroutineContexts() {
        runBlocking<Unit> {
            val jobs = arrayListOf<Job>()
            jobs += launch { //Default coroutine context.  Default is the commonPool thread.
                Log.i("Coroutines", "Default context': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(Unconfined) { // not confined -- will work with main thread
                Log.i("Coroutines", "Unconfined': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(coroutineContext) { // context of the parent, runBlocking coroutine
                Log.i("Coroutines", "coroutineContext': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(CommonPool) { // will get dispatched to ForkJoinPool.commonPool (or equivalent)
                Log.i("Coroutines", "CommonPool': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
                Log.i("Coroutines", "new single thread context': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs.forEach { it.join() }
        }
    }

    //Note the use of the suspend keyword.  If you want to extract out a method used in a coroutine, you will most often use suspend
    //if it uses any method requiring delays or coroutine methods
    suspend fun doSomethingUsefulFiveSeconds(): Int {
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

    private fun logSomething() {
        Log.i("Coroutines", "I just a log short and stout")
    }

    //Just a fun method Jose was asking about to mimic an Async.  Could also pass in a lambda parameter to use in either
    //background processing or the 'onPostExecute'.  Jose was very impressed.
    private fun mimicAsync() {
        val job =
            launch {
                Log.i("Coroutines", "mimicAsync method started")
                delay(10000)
                Log.i("Coroutines", "mimicAsync delay completed")
                //button_temp.text = "I am going to crash" IMPORTANT - This line will compile, but create a run-time crash
                launch(UI) {
                    someOtherMethod()
                    button_respectul.text = "I am done!"
                }
            }
    }

    private fun someOtherMethod() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    //Jobs have a cancel function HOWEVER coroutines must cooperate in order to be cancellable.
    private fun willNotRespectCancel() {
        runBlocking<Unit> {
            val startTime = System.currentTimeMillis()
            val job = launch {
                var nextPrintTime = startTime
                var i = 0
                while (i < 5) {
                    // print a message twice a second
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            }
            delay(1300L) // delay a bit
            println("main: I'm tired of waiting and am invoking cancel!")
            job.cancelAndJoin() // cancels the job and waits for its completion
            println("main: Now I can quit.")
        }
    }

    //So lets make it cooperate with cancel!
    private fun willRespectCancel() {
        runBlocking<Unit> {
            val startTime = System.currentTimeMillis()
            val job = launch {
                var nextPrintTime = startTime
                var i = 0
                while (isActive) {  //isActive is a property inside CoroutineScope object so is available inside the coroutine.
                    // print a message twice a second
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            }
            delay(1300L) // delay a bit
            println("main: I'm tired of waiting and am invoking cancel!")
            job.cancelAndJoin() // cancels the job and waits for its completion
            println("main: Now I can quit.")
        }
    }

    //Another way to 'check' for cancelation is using suspending functions inside your coroutine that respects cancel and
    //will then throw a CancellationException which you can than catch.  Note that a cancelled coroutine CancellationException
    //is considered to be a normal reason for coroutine completion
    private fun usingCancelableSuspendingFunctions() {
        runBlocking<Unit> {
            withTimeout(1300L) {  //withTimeout in CoroutineScope that will throw a TimeoutCancellationException
                                        //which is a subclass of CancellationException
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            }
        }
    }

}
