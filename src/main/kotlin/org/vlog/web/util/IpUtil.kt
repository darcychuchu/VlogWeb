package org.vlog.web.util

import jakarta.servlet.http.HttpServletRequest
import java.util.Locale
import java.util.regex.Pattern
import kotlin.text.equals
import kotlin.text.indexOf
import kotlin.text.isNullOrEmpty
import kotlin.text.trim

object IpUtil {

    internal class DevicePattern(val name: String, patternInfo: String) {
        private val pattern: Pattern = Pattern.compile(patternInfo)
        fun matches(s: String?): Boolean? {
            return s?.let { pattern.matcher(it).find() }
        }
    }

    private var devicePatterns = arrayOf(
        DevicePattern("Windows", "\\Wwindows\\snt\\W"),  //
        DevicePattern("macOS", "\\Wmacintosh\\W"),  //
        DevicePattern("Android", "\\Wandroid\\W"),  //
        DevicePattern("iPhone", "\\Wiphone\\W"),  //
        DevicePattern("iPad", "\\Wipad\\W"),  //
        DevicePattern("Linux", "\\Wlinux\\W")
    )

    fun getDevice(request: HttpServletRequest): String {
        var ua = request.getHeader("USER-AGENT")
        if (ua != null) {
            ua = ua.lowercase(Locale.getDefault())
            for (device in devicePatterns) {
                if (device.matches(ua) == true) {
                    return device.name
                }
            }
        }
        return "Unknown"
    }

    fun getIpString(request: HttpServletRequest): String {
        val ip = getClientIpAddr(request)
        return if (null != ip
            && "" != ip.trim { it <= ' ' }
            && !"unknown".equals(ip, ignoreCase = true)) {
            ip
        }else{
            getIpAddr(request)
        }
    }

    private fun getClientIpAddr(request: HttpServletRequest): String? {
        var ip = request.getHeader("X-Forwarded-For")
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_FORWARDED")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_VIA")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("REMOTE_ADDR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip
    }

    private fun getIpAddr(request: HttpServletRequest): String {
        var ip = request.getHeader("X-Real-IP")
        if (null != ip && "" != ip.trim { it <= ' ' }
            && !"unknown".equals(ip, ignoreCase = true)) {
            return ip
        }
        ip = request.getHeader("X-Forwarded-For")
        if (null != ip && "" != ip.trim { it <= ' ' }
            && !"unknown".equals(ip, ignoreCase = true)) {
            // get first ip from proxy ip
            val index = ip.indexOf(',')
            return if (index != -1) {
                ip.take(index)
            } else {
                ip
            }
        }
        return request.remoteAddr
    }
}