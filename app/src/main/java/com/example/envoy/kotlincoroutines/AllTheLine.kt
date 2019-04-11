package com.example.envoy.kotlincoroutines

/*
    Inline, noinline, crossinline, and overtheline.  One of these do not belong!  The others should be understood.
 */

class AllTheLines{

    private var memberString = "I am going to get lost later."

    fun main(args: Array<String>) {
        functionA()
        functionLambdaNoInline { println("Lambda printline") }
        functionLambdaWithInline { println("Lambda printline") }
        inlineMultipleParameters({ println("First Lambda printline")}, {println("Second Lambda printline")})  //Question, do you understand the IDE suggestion?  Do you agree with it?

    }

    //The inline keyword basically means that the compiler is going to copy this code and place it directly into the calling
    //method. Its as if the called function never existed.
    inline fun functionA() {
        println("function A printline !")
    }

    //But check out the compiler warning

    /*
        Warning: Expected performance impact of inlining ‘public inline fun functionA() can be insignificant.
        Inlining works best for functions with lambda parameters.  Turns out inlining a standard method is pretty
        much pointless because of JVM optimization.
     */

    inline fun functionLambdaWithInline (lambda: () -> Unit) {
        println("functionLambdaNoInLine printline before lambda invoke")
        lambda.invoke() //Remember that lambda's have the invoke method to invoke the code passed in for the lambda.
    }


    //Look at the decompiled code.  Note how the lambda call without inline creates a class of Function0?
    fun functionLambdaNoInline (lambda: () -> Unit) {
        println("functionLambdaNoInLine printline before lambda invoke")
        lambda.invoke()
    }

    /*
        Kotlin actually has a bunch of classes for handling lambdas passed in as parameters in the function package
        You will see classes for Function0, Function1, Function2, etc... based on number of parameters.

        public interface Function0<out R> : Function<R> {
        /** Invokes the function. */
        public operator fun invoke(): R

        Its this creation of new instances that creates an "expense" which the inline keyword removes.

        So why not make all lambda parameterized methods inline????
        1) Because inline loses access to the enclosing classes private methods and members.
        2) If you use return inside of an inlined function, not only do you return from the inline function, but you
            also return from the CALLING function.
     */
    fun regularFunction() {
        println(memberString)
    }

    inline fun inlineFunction(lambda: () -> Unit) {
//        println(memberString)
    }

    /*
        noinline - modifies parameters to nullify the inline keyword at the function level, so it will not inline that
        specific lambda.
     */

    inline fun inlineMultipleParameters(noinline lambda: () -> Unit, lambda2: () -> Unit){
        lambda.invoke()
        lambda2.invoke()
    }

    /*
        crossinline - also modifies parameter only. Nullifies the 'non-local control flow' aspect of inline lambda,
        making return behave as you are used to when used in an inline function
     */

    inline fun inlineMultipleParameters2(crossinline lambda: () -> Unit, lambda2: () -> Unit){
        lambda.invoke() //Without keyword of crossinline, what would happen to lambda2 if a return is encountered in lambda?
        lambda2.invoke()
    }


}