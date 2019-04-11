package com.example.envoy.kotlincoroutines

import kotlinx.coroutines.experimental.*
import org.junit.Test

class EasyAsyncConversion {

    /*****************************************************************************************/
    /**************        Synchronous on Main Thread   **************************************/
    /*****************************************************************************************/

    //This is our API call
    fun someApiCall(inputThatWillAlsoBeOutput : String) : String {
        Thread.sleep(1000)
        return "$inputThatWillAlsoBeOutput has returned!"
    }

    fun measureTime(block: () -> Unit) {
        val start = System.nanoTime()
        block()
        val end = System.nanoTime()
        println("${(end - start)/1.0e9} seconds")
    }

    val inputs = listOf("Chandra", "Ajani", "Gideon", "Liliana")

    @Test
    fun howLongWillItTakeSychronously(){
        measureTime {
            val returnValues = mutableListOf<String>()
            for (input in inputs) {
                returnValues += someApiCall(input)
            }

            for (returnValue in returnValues) {
                println(returnValue)
            }
        }
    }

    /*****************************************************************************************/
    /**************        Async Version off main thread    **********************************/
    /*****************************************************************************************/

    //first method someApiCall exactly the same - Not even going to bother copying it

    suspend fun measureTimeAsync(block: suspend () -> Unit) { //Added keyword suspend x 2.  Rest is the same
        val start = System.nanoTime()
        block()
        val end = System.nanoTime()
        println("${(end - start)/1.0e9} seconds")
    }

    //Bonus question. In line with exploring different ways to use all of our Kotlin goodies, can anyone
    //think of another way this would work without the suspend keywords being used?

    @Test
    fun howLongWillItTakeAsynchronously() {
        launch { //This line is added, wrapping what we already had
            measureTimeAsync {
                val returnValues = mutableListOf< Deferred<String>>() //Added the Deferred wrapper only
                for (input in inputs) {
                    returnValues += async { someApiCall(input) } //Just wrapped existing call in async {}
                }

                for (returnValue in returnValues) {
                    println(returnValue.await()) //Added .await() now that it is deferred
                }
            }
        }
        Thread.sleep(2000) //This is not really a change, its so test does not terminate prior to
                                    //Deferred returns
    }

    //Where is the logic and flow change?????

}