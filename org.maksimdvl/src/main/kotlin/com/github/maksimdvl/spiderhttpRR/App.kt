package com.github.maksimdvl.spiderhttpRR

import java.io.PrintWriter
import kotlin.concurrent.withLock
import java.util.concurrent.locks.*
import kotlinx.coroutines.experimental.*
import com.github.kittinunf.fuel.Fuel
import awaitString
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.math.roundToInt


fun main(vararg arg:String) {
    var path1url = "https://rosreestr.ru/wps/portal/p/cc_ib_portal_services/online_request/!ut/p/z1/pVE9TsMwFD4LA7PtNKV0tNqqoCJKf4AmS-S4VuoqcSLHBrEhwYEqZuAM6Y2w40BYWgYsL-_5-3vPIAQrEArywBOieC5IauogPIvGM3-EBj6ajGeoB_ElnqAOGkO47IL7o4ApAuF_-AZg-fDAwdDww6MWA-8PgI34l0lgQvYiiC4wwr43mQ6XfYjnHXh3de15ECKwsBo0F0rmacokCE7hghFJN5jaRdrXsq4j9VQwEODhcD5a1KxY83TNRWJMrAZZR0JnrpAsseTgF504vUBJzeq2jreMKofZ5Lo04qgeuVREqqjIS-4Y3b7rSsYMfP-6f9k_V-_VrvqoPqu3atcaRnxtRfo-7LoNOH-lUpYx0Xjl8bYNygTVWSyJoCwyGg0irUeJ7Tpa62YBpqhlJU82quU043wn-PmDhq-p0pI5KCnMfE2eIrtdQX6TZeedw_cRn3wBQc_CpQ!!/p0/IZ7_01HA1A42KODT90AR30VLN22001=CZ6_GQ4E1C41KGQ170AIAK131G00T5=MEcontroller!QCPObjectDataController==/" +
            "?object_data_id=140_"
    val path2url = "&dbName=fir&region_key=140"

    val count = 1000
    val startId = arg[1].toInt()
    val endAppendix = if( arg.size > 2 ) arg[2] else ""

    var l1 = 1
    for (i in 1..endAppendix.length) l1 *= 10

    println( (startId * count).toString() + endAppendix)

    val resultHM = hashMapOf<Int, String>()
    val l: Lock = ReentrantLock()
    runBlocking {
        val jobs: List<Job> = List( count ) {
            launch {
                val id = (startId * count + it) * l1  + ( if( endAppendix.isEmpty() ) 0 else endAppendix.toInt() )
                lateinit var aJsoup: Document
                val maxRequestCount = 5
                loop@ for(i in 1..maxRequestCount){
                    try {
                        aJsoup = Jsoup.parse(Fuel.get(path1url + id + path2url).awaitString())
                        break@loop
                    }
                    catch (e: Exception){
                        if (i == maxRequestCount ) return@launch
                        println("Повторный запрос${id}")
                        delay(1000)
                    }
                }
                val brdw = mutableListOf<Element>()

                with(brdw) {
                    add(aJsoup.select("td.brdw0111").first()?:Element("table"))
                    addAll(aJsoup.select("td.brdw1010")?:listOf(Element("table")))
                    add(aJsoup.getElementById("r_enc")?:Element("table"))
                }

                val html = """
                            |<!DOCTYPE html>
                            |<html>
                            |   <head><title>$id</title></head>
                            |   <body></body>
                            |</html>""".trimMargin()

                val pageJsoup = Jsoup.parse(html)
                val p = pageJsoup.selectFirst("body")!!
                with(p) {
                        brdw.forEach { append(it.toString()) }
                }

                l.withLock { resultHM.set(id, pageJsoup.html()) }
            }
        }

        jobs.forEach { it.join() } // wait for all jobs to complete
    }
    var xxx = "xxx"
    val pathN = if (startId < 1) "" else "${startId}"
    val path = "${arg[0]}/${pathN}${xxx}${endAppendix}.json"

    PrintWriter(path).use {
        it.println(Gson().toJson(resultHM))
    }
}
