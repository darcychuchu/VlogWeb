package org.vlog.web.utils

import org.lionsoul.ip2region.xdb.Searcher
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.text.isBlank
import kotlin.to

object AddressUtil {

    @Suppress("UNREACHABLE_CODE")
    fun getAddressInfo(ip: String): Map<String, Any> {
        val searcher: Searcher = try {
            Searcher.newWithFileOnly("/data/ip2region.xdb")
        } catch (e: IOException) {
            return mapOf("region" to "")
        }
        try {
            val sTime = System.nanoTime()
            val region = searcher.search(ip)
            val cost = TimeUnit.NANOSECONDS.toMicros((System.nanoTime() - sTime))
            return if (region.isBlank() && region == null){
                mapOf("region" to "")
            }else{
                mapOf("region" to region, "ioCount" to searcher.ioCount, "took" to cost)
            }
        } catch (e: Exception) {
            return mapOf("region" to "")
        }
        searcher.close()
    }
}