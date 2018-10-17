package com.github.maksimdvl.spiderhttpRR

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.*
import com.github.kittinunf.fuel.Fuel
import awaitString
import java.net.URL
import java.io.IOException
import java.nio.charset.*
import org.apache.commons.io.IOUtils

fun main(arg: Array<String>) {
    val l = listOf(URL("https://rosreestr.ru/site/"), URL("https://rosreestr.ru/wps/portal/online_request"))
    println(jobsGetContentOf(l))
}

fun getContentOfHTTPPage(pageAddress: String, codePage: Charset = Charsets.UTF_8) = IOUtils.toString(URL(pageAddress), codePage)

fun jobsGetContentOf(iterURL: Iterable<URL>): HashMap<URL, String> {
    val jobs = mutableListOf<Job>()
    val resultHM = hashMapOf<URL, String>()
    val mutex = Mutex()
    runBlocking {
        iterURL.forEach {
            val job = launch {
                var resultGetHttp = "NOT RESULT GET REQUEST"
                repeat (5) {
                    try {
                        if (resultGetHttp == "NOT RESULT GET REQUEST") resultGetHttp = Fuel.get(it.toString()).awaitString()
                    } catch (e: IOException) {
                        delay(1000)
                    }
                }
                mutex.withLock { resultHM[it] = resultGetHttp }
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }
    }
    return resultHM
}