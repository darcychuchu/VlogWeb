package org.vlog.web.service

import org.vlog.web.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class ApiService(
    private val restTemplate: RestTemplate,
    @Value("\${api.base-url}") private val apiBaseUrl: String
) {
    private val logger = LoggerFactory.getLogger(ApiService::class.java)


    /**
     * VideoListFiltered 筛选视频
     * @param typed 类型
     * @param page 分页
     * @param size 分页大小
     * @param year 年份
     * @param orderBy 排序
     * @param cate 分类
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    fun getVideoListFiltered(
        typed: Int = 0,
        page: Int = 1,
        size: Int = 24,
        year: Long = 0,
        orderBy: Int = 3,
        cate: String = "",
        token: String? = null
    ): PaginatedResponse<VideoListDto>? {
        try {
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/filter?typed=$typed&page=$page&size=$size&year=$year&order_by=$orderBy&cate=$cate"
            } else {
                "$apiBaseUrl/videos/filter?typed=$typed&page=$page&size=$size&year=$year&order_by=$orderBy&cate=$cate&token=$token"
            }

            val responseType = object : ParameterizedTypeReference<ApiResponse<PaginatedResponse<VideoListDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<PaginatedResponse<VideoListDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data
        } catch (e: RestClientException) {
            return null
        } catch (e: Exception) {
            return null
        }
    }


    fun getVideoList(
        typed: Int = 1,
        page: Int = 1,
        size: Int = 24,
        year: Long = 2025,
        orderBy: Int = 3,
        token: String? = null
    ): List<VideoListDto>? {
        try {
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/filter?typed=$typed&page=$page&size=$size&year=$year&order_by=$orderBy"
            } else {
                "$apiBaseUrl/videos/filter?typed=$typed&page=$page&size=$size&year=$year&order_by=$orderBy&token=$token"
            }

            val responseType = object : ParameterizedTypeReference<ApiResponse<PaginatedResponse<VideoListDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<PaginatedResponse<VideoListDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data?.items
        } catch (e: RestClientException) {
            return null
        } catch (e: Exception) {
            return null
        }
    }


    /**
     * VideoDetail 视频详情
     * @param id 视频ID
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    fun getVideoDetail(id: String, token: String? = null): VideoDetailDto? {
        try {
            val responseType = object : ParameterizedTypeReference<ApiResponse<VideoDetailDto>>() {}
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/detail/$id"
            } else {
                "$apiBaseUrl/videos/detail/$id?token=$token"
            }
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data
        } catch (e: RestClientException) {
            return null
        }
    }




    /**
     * SourceDetail 视频详情
     * @param id 视频ID
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    fun getSourceDetail(id: String, token: String? = null): VideoDetailDto? {
        try {
            val responseType = object : ParameterizedTypeReference<ApiResponse<VideoDetailDto>>() {}
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/sources/detail/$id"
            } else {
                "$apiBaseUrl/videos/sources/detail/$id?token=$token"
            }
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data
        } catch (e: RestClientException) {
            return null
        }
    }



    /**
     * Categories 视频分类
     * @param typed 类型
     * @param cate 分类ID
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    fun getCategories(typed: Int = 0, cate: String? = null, token: String? = null): List<CategoriesDto> {
        try {
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/categories?typed=$typed&cate=$cate"
            } else {
                "$apiBaseUrl/videos/categories?typed=$typed&cate=$cate&token=$token"
            }

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<CategoriesDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<CategoriesDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data ?: emptyList()
        } catch (e: RestClientException) {
            return emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }


    /**
     * 搜索视频
     * @param searchKey 搜索关键词
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    fun searchVideos(searchKey: String, token: String? = null): List<VideosDto> {
        try {
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/search?key=$searchKey"
            } else {
                "$apiBaseUrl/videos/search?key=$searchKey&token=$token"
            }

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data ?: emptyList()
        } catch (e: RestClientException) {
            return emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }



    /**
     * AppVersion App版本
     * @return 结果
     */
    fun getAppVersion(): AppVersionDto? {
        try {
            val url = "$apiBaseUrl/app/version"

            val responseType = object : ParameterizedTypeReference<ApiResponse<AppVersionDto>>() {}
            val response = restTemplate.exchange<ApiResponse<AppVersionDto>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            return response.body?.data
        } catch (e: RestClientException) {
            return null
        } catch (e: Exception) {
            return null
        }
    }

//    /**
//     * 发布评论
//     * @param videoId 视频ID
//     * @param token 用户token
//     * @param content 评论内容
//     * @return 是否发布成功
//     */
//    fun postComment(videoId: String, token: String, content: String): Boolean {
//        try {
//            ///logger.info("Posting comment for video ID: $videoId")
//
//            val headers = org.springframework.http.HttpHeaders()
//            headers.contentType = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
//
//            val map = org.springframework.util.LinkedMultiValueMap<String, String>()
//            map.add("content", content)
//
//            val request = HttpEntity(map, headers)
//
//            val url = "$apiBaseUrl/videos/comments-post/$videoId?token=$token"
//            ///logger.info("API URL: $url")
//
//            val responseType = object : ParameterizedTypeReference<ApiResponse<String>>() {}
//            val response = restTemplate.exchange<ApiResponse<String>>(
//                url,
//                HttpMethod.POST,
//                request,
//                responseType
//            )
//
//            val apiResponse = response.body
//            if (apiResponse != null) {
//                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
//                // 如果code为200，则认为发布成功
//                return apiResponse.code == "200"
//            } else {
//                ///logger.error("API response body is null")
//                return false
//            }
//        } catch (e: RestClientException) {
//            ///logger.error("Error posting comment: ${e.message}", e)
//            return false
//        } catch (e: Exception) {
//            ///logger.error("Unexpected error posting comment: ${e.message}", e)
//            return false
//        }
//    }
}
