package org.vlog.web.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
///@CacheConfig(cacheNames = ["apiProxy"])
class ApiProxyService(
    private val restTemplate: RestTemplate,
    private val apiBaseUrl: String = "https://66log.com/api/json/v3"
) {
    //private val logger = LoggerFactory.getLogger(ApiProxyService::class.java)

    ///@Cacheable(key = "#path + #queryString", unless = "#result.statusCode.is2xxSuccessful == false")
    fun proxyGet(path: String, queryString: String): ResponseEntity<Any> {
        // 处理图片路径特殊情况
//        if (path.contains("/file/attachments/image/")) {
//            // 图片URL直接访问基础URL，不需要api/json/v1/videos前缀
//            // 根据用户提供的正确图片URL格式
//            val imageUrl = "${apiImageUrl}$path$queryString"
//            //logger.info("Proxying image request to: $imageUrl")
//
//            try {
//                // 使用RestTemplate的getForEntity方法获取字节数组响应
//                val response = restTemplate.getForEntity(imageUrl, ByteArray::class.java)
//                ///logger.info("Image response received: ${response.statusCode}, content type: ${response.headers.contentType}")
//                return ResponseEntity.status(response.statusCode)
//                    .headers(response.headers)
//                    .body(response.body)
//            } catch (e: Exception) {
//                //logger.error("Error proxying image request to $imageUrl: ${e.message}")
//                // 如果图片请求出错，返回一个空的图片响应
//                val headers = HttpHeaders()
//                headers.contentType = MediaType.IMAGE_JPEG
//                return ResponseEntity.ok().headers(headers).body(ByteArray(0))
//            }
//        }
//
//        // 处理其他API请求
        val apiPath = if (path.startsWith("/file/")) {
            // 如果是文件路径，则不需要添加videos前缀
            path
        } else {
            // 其他API路径需要添加videos前缀
            "/videos$path"
        }

        ///logger.debug("Proxying GET request to: $apiBaseUrl$apiPath$queryString")

        try {
            return restTemplate.exchange<Any>(
                "$apiBaseUrl$apiPath$queryString",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                object : ParameterizedTypeReference<Any>() {}
            )
        } catch (e: HttpClientErrorException) {
            ///logger.error("Error proxying request to $apiBaseUrl$apiPath$queryString: ${e.message}")
            throw e
        }
    }

    // 不缓存POST请求
    fun proxyPost(path: String, body: Any?): ResponseEntity<Any> {
        ///logger.debug("Proxying POST request to: $apiBaseUrl$path")

        val httpEntity = if (body != null) HttpEntity(body) else HttpEntity.EMPTY

        return restTemplate.exchange<Any>(
            "$apiBaseUrl$path",
            HttpMethod.POST,
            httpEntity,
            object : ParameterizedTypeReference<Any>() {}
        )
    }

    // 手动清除缓存
    //@CacheEvict(allEntries = true)
    fun clearCache() {
        ///logger.info("Clearing API proxy cache")
    }
}
