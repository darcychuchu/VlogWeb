package org.vlog.web.controller

import org.vlog.web.service.ApiProxyService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@Controller
class ApiProxyController(
    private val apiProxyService: ApiProxyService
) {
    ////private val logger = LoggerFactory.getLogger(ApiProxyController::class.java)

    @GetMapping("/api-proxy/**")
    @ResponseBody
    fun proxyApiGet(request: HttpServletRequest): ResponseEntity<Any> {
        val apiPath = request.requestURI.replace("/api-proxy", "")
        val queryString = if (request.queryString != null) "?${request.queryString}" else ""
        println(apiPath)
        return apiProxyService.proxyGet(apiPath, queryString)
    }

    @PostMapping("/api-proxy/**")
    @ResponseBody
    fun proxyApiPost(
        request: HttpServletRequest,
        @RequestBody(required = false) body: Any?
    ): ResponseEntity<Any> {
        val apiPath = request.requestURI.replace("/api-proxy", "")
        return apiProxyService.proxyPost(apiPath, body)
    }

//    @GetMapping("/api-proxy-clear-cache")
//    @ResponseBody
//    fun clearCache(): Map<String, String> {
//        apiProxyService.clearCache()
//        return mapOf("status" to "success", "message" to "Cache cleared")
//    }
}
