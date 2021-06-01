package com.mytlogos.enterprise.test

import org.joda.time.DateTime
import java.util.concurrent.CompletableFuture

object SimpleTest {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
//        Client client = new Client();
//        Call<ClientUser> call = client.login("mater", "123");
//        Response<ClientUser> response = call.execute();
//        System.out.println(response.body());
        val future = CompletableFuture.completedFuture<Any?>(null).thenRun {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(DateTime.now().toString() + "future:" + Thread.currentThread())
        }
        println(DateTime.now().toString() + "after: " + Thread.currentThread())
    }

    private fun d(integers: Iterable<Int>) {}
    private open class A<T> {
        var value: List<T>? = null
        fun hello(list: List<T>) {
            for (t in list) {
                println(t)
            }
        }
    }

    private class B : A<Any?>()
}