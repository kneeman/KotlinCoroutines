package com.example.envoy.kotlincoroutines

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import kotlin.concurrent.thread

class CoroutinesExamples {

    @Test
    fun lotsOfDotsThreads() {
        List(500_000) {
            thread {
                Thread.sleep(1000L)
                print(".")
            }
        }.forEach { it.join() }
        println()
        println("Done")
    }

    @Test
    fun lotsOfDotsCoroutines() = runBlocking{
        List(500_000) {
            launch {
                delay(1000L)
                print(".")
            }
        }.forEach { it.join() }
        println()
        println("Done")
    }

    //In addition if the first one had not crashed with out of memory, you would of seen that
    //coroutines are much faster than creating threads as well.

    @Test
    fun nonBlockingWaitingForJob() = runBlocking {
        val job = launch  {
            delay(3000L) //This delay is inside of coroutine, so it will suspend this coroutine
            //but not the outer one.  That will move on.
            print("Coroutine!")
        }
        print("I am a Kotlin ")
        job.join() //join on a job will wait for the job to complete.  So now I am blocking this coroutine.
        print(" And I am finished!")
    }
}