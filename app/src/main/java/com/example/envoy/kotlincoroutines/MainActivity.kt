package com.example.envoy.kotlincoroutines

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.selects.select
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.experimental.CoroutineContext
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
        button_run_blocking.setOnClickListener({ runBlockingCoroutine(true) })
        button_contexts.setOnClickListener { coroutineContexts() }
        button_disrespectul.setOnClickListener { willNotRespectCancel() }
        button_respectul.setOnClickListener { willRespectCancel() }
        button_first_channel.setOnClickListener { ourFirstChannel() }
        button_close_channel.setOnClickListener { channelCloseIterationStyle() }
        button_producer_consumer.setOnClickListener { producerCoroutine() }
        button_pipelines.setOnClickListener { runMyPipelines() }
        button_pingpong.setOnClickListener { playPingPong() }
        button_dispatches.setOnClickListener { dispatchTips() }
        button_room.setOnClickListener { goToRoomActivity() }
        button_no_synchronization.setOnClickListener { startMassiveRun() }
        button_volatile.setOnClickListener { startMassiveRunWithVolatile() }
        button_threadsafe_structure.setOnClickListener { startMassiveRunWithAtomicData() }
        button_thread_confinement_fine.setOnClickListener { startMassiveRunWithThreadConfinementFine() }
        button_thread_confinement_coarse.setOnClickListener { startMassiveRunWithThreadConfinementCoarse() }
        button_mutex.setOnClickListener { startMassiveRunWithMutex() }
        button_actor.setOnClickListener { startMassiveRunWithActor() }
        button_select.setOnClickListener { runFizzBuzz() }
        button_select_close.setOnClickListener { runSelectHandlingClosedChannel() }
        button_select_side_channel.setOnClickListener { runSideChannel() }


    }

    private fun goToRoomActivity() {
        intent = Intent(this, RoomActivity::class.java)
        startActivity(intent)
    }


    private fun handleClickListener(v: View) {
        when (v.id) {
            R.id.button_simple_coroutine_noui -> launchDelayNoUI(false)
            else -> println("Unhandled view click listener!")
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
            jobs += launch {
                //Default coroutine context.  Default is the commonPool thread.
                Log.i("Coroutines", "Default context': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(Unconfined) {
                // not confined -- will work with main thread
                Log.i("Coroutines", "Unconfined': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(coroutineContext) {
                // context of the parent, runBlocking coroutine
                Log.i("Coroutines", "coroutineContext': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(CommonPool) {
                // will get dispatched to ForkJoinPool.commonPool (or equivalent)
                Log.i("Coroutines", "CommonPool': I'm working in thread ${Thread.currentThread().name}")
            }
            jobs += launch(newSingleThreadContext("MyOwnThread")) {
                // will get its own new thread
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
            withTimeout(1300L) {
                //withTimeout in CoroutineScope that will throw a TimeoutCancellationException
                //which is a subclass of CancellationException
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            }
        }
    }


    /**
     * Coroutines continued code
     */


    // Coroutine contexts can be combined with + operator.  Right side replaces relevant entries of context on the left
    //side
    private fun combineContexts() {
        val button = Button(this)
        val job = launch(UI) {
            //This will be the parent task for
            //offload the real work to CommonPool
            val result = async(coroutineContext + CommonPool) {
                //While this is running the UI thread is NOT suspended
                delay(10000)
                "I am done"
            }.await()
            button.text = result
        }
    }

    private fun anotherWay() {  //There is one difference here.  Can you spot it?  How do we remove that difference?
        val button = Button(this)
        val job = launch {
            val result = async {
                delay(10000)
                "I am done"
            }
            launch(UI) {
                button.text = result.await()
            }
        }
    }


    /**
     *
     *
     * Channels
     *
     */


    private fun ourFirstChannel() {
        launch {
            val channel = Channel<Int>()
            launch {
                // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
                for (x in 1..5) {
                    Log.i("Coroutines", "Channel sending data from index: $x")
                    channel.send(x * x)
                }
            }
            // here we print five received integers
            //Receive blocks waiting on each iteration until it has something sent from the channel.
            repeat(5) { Log.i("Coroutines", "Channel received data : ${channel.receive()}") }
            Log.i("Coroutines", "Done!")
        }
    }


    //Doing the same thing as ourFirstChannel() but the receiver will be handled a bit differently
    //there is a BIG issue here, can you spot it?
    private fun channelCloseIterationStyle() {
        launch {
            val channel = Channel<Int>()
            launch {
                for (x in 1..5) {
                    Log.i("Coroutines", "Channel sending data from index: $x")
                    channel.send(x * x)
                }
                channel.close() //We are informing receiver that we are done sending
            }

            //Now we replace our previous repeat with the channel itself that then knows (little magic box here) when
            //it is done
            for (y in channel) {
                //Log.i("Coroutines", "Channel received data : $y")
                Log.i("Coroutines", "Channel received data : ${channel.receive()}")
            }
            Log.i("Coroutines", "Done!")
        }
    }


    /**
     * Demonstrate convenience builder for coroutine that makes producer-consumer pattern easier
     */
    private fun producerCoroutine() {
        val producer = produce<Int> {
            for (x in 1..5) {
                Log.i("Coroutines", "Channel sending data from index: $x")
                send(x * x)
            }
        }

        launch {
            //No for loop required!
            producer.consumeEach {
                Log.i("Coroutines", "Channel received data : $it")
                delay(10000)
            }
        }
    }

    //Pipeline - pattern where coroutine produces a possible infinite stream of values.

    private fun numbersPipeline(context: CoroutineContext, start: Int) = produce<Int>(context) {
        var x = start
        while (true) { //Please note that Bill did not write this
            Log.i("Coroutines", "numbersPipeline sending: $x")
            send(x++)
        }
    }

    //ReceiveChannel is the receivers interface to channel
    private fun filterPipeline(context: CoroutineContext, numbers: ReceiveChannel<Int>, prime: Int) = produce<Int>(context) {
        for (x in numbers) {
            if (x % prime != 0) {
                Log.i("Coroutines", "filterPipeline sending: $x")
                send(x)
            }

        }
    }

    private fun runMyPipelines() = runBlocking<Unit> {
        var currentNumberFromPipeline = numbersPipeline(coroutineContext, 2)
        for (i in 1..10) {
            val prime = currentNumberFromPipeline.receive()
            Log.i("Coroutines", "Found a prime!: $prime")
            currentNumberFromPipeline = filterPipeline(coroutineContext, currentNumberFromPipeline, prime)
        }
        coroutineContext.cancelChildren()
    }


    /**
     * Fan Out - Channel can send to multiple coroutines
     */

    fun produceNumbers() = produce<Int> {
        var x = 1 // start from 1
        while (true) {
            send(x++) // produce next
            delay(100) // wait 0.1s
        }
    }

    fun launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
        channel.consumeEach {
            println("Processor #$id received $it")
        }
    }

    fun runFanOut() {
        runBlocking<Unit> {
            val producer = produceNumbers()
            repeat(5) { launchProcessor(it, producer) }
            delay(950)
            producer.cancel() // cancel producer coroutine and thus kill them all
        }
    }


    /**
     * Fan In - Multiple co-routines can send to the same channel
     */

    suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
        while (true) {
            delay(time)
            channel.send(s)
        }
    }

    fun fanIn() {
        runBlocking<Unit> {
            val channel = Channel<String>()
            launch(coroutineContext) { sendString(channel, "foo", 200L) }
            launch(coroutineContext) { sendString(channel, "BAR!", 500L) }
            repeat(6) {
                // receive first six
                println(channel.receive())
            }
            coroutineContext.cancelChildren() // cancel all children to let main finish
        }
    }

    //Go over concept of Unbuffered (default) and Buffered channels


    /**
     * Channels are fair.  The first receiver to invoke receive gets the element.  Also this is a really cool example
     * because doing a ping pong exercise was the very first multi-threaded exercise I did in school with java in 1954.
     */

    data class Ball(var hits: Int)

    fun playPingPong() {
        runBlocking<Unit> {
            val tableChannel = Channel<Ball>() // a shared table
            launch(coroutineContext) { player("ping", tableChannel) }
            launch(coroutineContext) { player("pong", tableChannel) }
            tableChannel.send(Ball(0)) // serve the ball
            delay(1000) // delay 1 second
            coroutineContext.cancelChildren() // game over, cancel them
        }
    }

    suspend fun player(name: String, table: Channel<Ball>) {
        for (ball in table) { // receive the ball in a loop.  At this point the player is ready to receive from the channel
            ball.hits++
            Log.i("Coroutines", "$name $ball")
            delay(300) // wait a bit
            table.send(ball) // send the ball back
        }
    }


    /**
     *  Bonus tip of the day.  Dispatched vs undispatched coroutines
     */


    //So read the code below and tell me what you think the results will be?
    fun dispatchTips() {
        Log.i("Coroutines", "Before launch")
        launch(CommonPool) {
            //reminder - CommonPool is redundant because it is the default
            Log.i("Coroutines", "Inside Coroutine")
            delay(100)
            Log.i("Coroutines", "After delay")
        }
        Log.i("Coroutines", "After launch")
    }
    //Async actions are always postponed to be executed later in the event dispatch thread.  To change this behavior,
    //you would change the code to the following - launch(CommonPool, CoroutineStart.UNDISPATCHED)


    /**
     * Stay tuned for the final episode of Kotlin Coroutines (maybe) where we go over "Shared mutable state and concurrency"
     * plus the Actors who define them!
     */

    //First go over discovered advantage of Producer/Consumer!


    suspend fun massiveRun(context: CoroutineContext, action: suspend () -> Unit) {
        val numberOfCoroutines = 1000 // number of coroutines to launch
        val numberActionsPerCoroutine = 1000 // times an action is repeated by each coroutine
        val time = measureTimeMillis {
            val jobs = List(numberOfCoroutines) {
                launch(context) {
                    repeat(numberActionsPerCoroutine) { action() }
                }
            }
            jobs.forEach {
                it.join() //FYI - Join will also start a corresponding coroutine if the job is in a new state.
            }
        }
        Log.i("Coroutines", "Completed ${numberOfCoroutines * numberActionsPerCoroutine} actions in $time ms")
    }

    var counter = 0

    fun startMassiveRun() = runBlocking<Unit> {
        massiveRun(CommonPool) {
            counter++
        }
        Log.i("Coroutines", "Counter = $counter")
        counter = 0
    }

    //Volatile to the rescue?  Lets try it...

    @Volatile // in Kotlin `volatile` is an annotation
    var volatileCounter = 0

    //PopQuiz - Why is <Unit> greyed out in this and all the other examples?
    fun startMassiveRunWithVolatile() = runBlocking<Unit> {
        massiveRun(CommonPool) {
            volatileCounter++
        }
        Log.i("Coroutines", "Volatile Counter = $volatileCounter")
        volatileCounter = 0

        //So volatile does not work
    }


    /**
     * Thread safe state solutions!
     */


    /**
     *  #1 - Thread-safe Data Structures
     */


    var counterAtomic = AtomicInteger()

    fun startMassiveRunWithAtomicData() = runBlocking<Unit> {
        massiveRun(CommonPool) {
            counterAtomic.addAndGet(1)
            //really for a counter use - counterAtomic.incrementAndGet()

        }
        Log.i("Coroutines", "Atomic Counter = $counterAtomic")
        counterAtomic.set(0)
    }


    /**
     *  #2 - Thread confinement
     */
    val counterContext = newSingleThreadContext("CounterContext") //Note the use of naming our context
    var counterThreadContextFine = 0

    //Fine thread confinement means you limit all access to the shared state to a single thread, but coroutine is started
    //on a different thread
    fun startMassiveRunWithThreadConfinementFine() = runBlocking<Unit> {
        massiveRun(CommonPool) {
            // run each coroutine in CommonPool
            withContext(counterContext) {
                // but confine each increment to the single-threaded context
                counterThreadContextFine++
            }
        }
        Log.i("Coroutines", "Counter using fine thread confinement = $counterThreadContextFine")
        //This will run relatively slowly because you are switching from CommonPool to the other context each time
        counterThreadContextFine = 0
    }


    var counterThreadContextCoarse = 0
    fun startMassiveRunWithThreadConfinementCoarse() = runBlocking {
        massiveRun(counterContext) {
            // run each coroutine in the single-threaded context
            counterThreadContextCoarse++
        }
        Log.i("Coroutines", "Counter using coarse thread confi nement = $counterThreadContextCoarse")
        //No longer switching threads to access the shared state, so should run faster
        counterThreadContextCoarse = 0
    }

    //Keep in mind you dice these up as you see fit.  Fine and course are just words to show a concept. Fine to you might
    // mean each individual data is on its own thread and course might mean you update all your data on one specific thread.

    /**
     *  #3 - Mutual Exclusion - think synchronized block!
     *  Creates a section of code that is never executed concurrently.
     *  Mutex class has lock (suspending function) and unlock functions to mark a critical section
     */

    val mutex = Mutex()
    var counterMutex = 0

    fun startMassiveRunWithMutex() = runBlocking {
        massiveRun(CommonPool) {
            mutex.withLock {
                //Even easier the withLock extension function which has mutex.lock, try{...}, finally{...},
                //and mutex.unlock all implemented for you.
                counterMutex++
            }
        }
        Log.i("Coroutines", "Counter using a mutex object = $counterMutex")
        counterMutex = 0
    }
    //Mutex uses fine grain locking, so it will take longer.


    /**
     * Actors - a combination of coroutine, confined and encapsulated state, and channel to communicate with other
     *  coroutines.  Simple actor can be written as a function, actor with complex state are better in their own class.
     */

    //First go over the data structures used in this example

    //actor coroutine builder. Incoming channel is in its scope, send channel is in its resulting job
    fun counterActor() = actor<CounterMessage> {
        var counter = 0 // actor state

        // Note that msg is inferred by the channel in the Actor Scope(CounterMessage in this case). I find this something
        // that can be hard to read/follow as you have to know about this particular channel by its actor coroutine builder
        for (msg in channel) {
            when (msg) {
                is IncomingCounter -> counter++
                is GetCounter -> msg.response.complete(counter)
            }
        }
    }

    fun startMassiveRunWithActor() = runBlocking {
        val counterJob = counterActor() //Setup job.  Why do we need the job besides to close it?
        massiveRun(CommonPool) {
            counterJob.send(IncomingCounter) //Sending the message to the actor to change state
        }

        val response = CompletableDeferred<Int>()
        counterJob.send(GetCounter(response)) //Sending the message to the actor to get the counter value
        Log.i("Coroutines", "Counter using an Actor = ${response.await()}")
    }

    /**
     * Select Expressions - way to await multiple suspending functions and select the first one that becomes available
     *
     */

    fun fizz(context: CoroutineContext) = produce<String>(context) {
        while (true) { // sends "Fizz" every 300 ms
            delay(300)
            send("Fizz")
        }
    }

    fun buzz(context: CoroutineContext) = produce<String>(context) {
        while (true) { // sends "Buzz!" every 500 ms
            delay(500)
            send("Buzz!")
        }
    }


    //If we were just to use receive, we would only receive from either one channel or another.  Select allows us to
    //receive from both simultaneously
    suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
        select<Unit> {
            fizz.onReceive { value ->
                Log.i("Coroutines", "fizz -> '$value'")

            }
            buzz.onReceive { value ->
                Log.i("Coroutines", "buzz -> '$value'")
            }
        }
    }

    fun runFizzBuzz() = runBlocking{
        val fizz = fizz(coroutineContext)
        val buzz = buzz(coroutineContext)
        repeat(7) {
            selectFizzBuzz(fizz, buzz)
        }
        coroutineContext.cancelChildren() // cancel fizz & buzz coroutines
    }

    //What happens when the channel closes but we are still running select?


    suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
            select<String> {
                a.onReceiveOrNull { value ->
                    if (value == null)
                        "Channel 'a' is closed"
                    else
                        "a -> '$value'"
                }
                b.onReceiveOrNull { value ->
                    if (value == null)
                        "Channel 'b' is closed"
                    else
                        "b -> '$value'"
                }
            }


    fun runSelectHandlingClosedChannel() = runBlocking{
        val a = produce<String>(coroutineContext) {
            repeat(4) { send("Hello $it") }
        }
        val b = produce<String>(coroutineContext) {
            repeat(4) { send("World $it") }
        }
        repeat(8) { // print first eight results
            Log.i("Coroutines", selectAorB(a, b))
        }
        coroutineContext.cancelChildren()
    }

    //Use the onSend clause to determine if a consumer is ready or not.
    fun produceNumbers(context: CoroutineContext, side: SendChannel<Int>) = produce<Int>(context) {
        for (num in 1..10) { // produce 10 numbers from 1 to 10
            delay(100) // every 100 ms
            select<Unit> {
                onSend(num) {} // Send to the primary channel
                side.onSend(num) {} // or to the side channel
            }
        }
    }

    fun runSideChannel() {
        runBlocking{
            // Setup side channel. We are using producer/consumer so main channel is implied in the consumeEach below
            val side = Channel<Int>()

            launch(coroutineContext) { // this is a very fast consumer for the side channel
                side.consumeEach {
                    Log.i("Coroutines", "Side channel has $it")
                }
            }

            //Here we set up our main consumer
            produceNumbers(coroutineContext, side).consumeEach {
                Log.i("Coroutines", "Main channel has $it")
                delay(250) // let us digest the consumed number properly, do not hurry
            }
            Log.i("Coroutines", "Done Consuming")
            coroutineContext.cancelChildren()
        }
    }

}

sealed class CounterMessage

//Note - As of 1.1 you no longer have to the subclasses inside of the sealed class itself
object IncomingCounter : CounterMessage() //One way message to increment the counter

//message to get value, deferred needed to send response
class GetCounter(val response: CompletableDeferred<Int>) : CounterMessage()



