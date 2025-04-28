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
    ///private val logger = LoggerFactory.getLogger(ApiService::class.java)

    ////@Cacheable("videoDetails")
    fun getVideoDetail(id: String, gatherId: String = "5eeb44fc-2fb4-4ec2-b32e-63f50bfff707", token: String? = null): VideosDto? {
        try {
            //logger.info("Fetching video details for ID: $id")

            val responseType = object : ParameterizedTypeReference<ApiResponse<VideosDto>>() {}
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/detail/$id?gather=$gatherId"
            } else {
                "$apiBaseUrl/videos/detail/$id?gather=$gatherId&token=$token"
            }

            val response = restTemplate.exchange<ApiResponse<VideosDto>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                //logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    //logger.info("Retrieved video details from API")
                    return apiResponse.data
                } else {
                    //logger.debug("API returned error code: ${apiResponse.code}, message: ${apiResponse.message ?: "Unknown error"}")
                    return null
                }
            } else {
                //logger.debug("API response body is null")
                return null
            }
//        } catch (e: RedisConnectionFailureException) {
//            // Redis连接失败，记录日志但继续执行，使用内存缓存
//            logger.debug("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                // 重新尝试获取数据，但不使用Redis缓存
//                val responseType = object : ParameterizedTypeReference<ApiResponse<VideosDto>>() {}
//                val response = restTemplate.exchange<ApiResponse<VideosDto>>(
//                    "$apiBaseUrl/videos/detail/$id?gather=$gatherId",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        logger.info("Retrieved video details from API after Redis failure")
//                        return apiResponse.data
//                    } else {
//                        logger.debug("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return null
//                    }
//                } else {
//                    logger.debug("API response body is null after Redis failure")
//                    return null
//                }
//            } catch (ex: RestClientException) {
//                logger.debug("Error fetching video details after Redis failure: ${ex.message}")
//                return null
//            }
        } catch (e: RestClientException) {
            ///logger.debug("Error fetching video details: ${e.message}")
            return null
        }
    }

    ///@Cacheable("actors")
    fun getActors(videoId: String, token: String? = null): List<TagsQuotesDto> {
        try {
            ///logger.info("Fetching actors for video ID: $videoId")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/actors/$videoId"
            } else {
                "$apiBaseUrl/videos/actors/$videoId?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<TagsQuotesDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<TagsQuotesDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} actors from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<TagsQuotesDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<TagsQuotesDto>>>(
//                    "$apiBaseUrl/videos/actors/$videoId",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        val data = apiResponse.data ?: emptyList()
//                        logger.info("Retrieved ${data.size} actors from API after Redis failure")
//                        return data
//                    } else {
//                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return emptyList()
//                    }
//                } else {
//                    logger.error("API response body is null after Redis failure")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching actors after Redis failure: ${ex.message}", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching actors: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching actors: ${e.message}", e)
            return emptyList()
        }
    }

    ///@Cacheable("genres")
    fun getGenres(videoId: String, token: String? = null): List<TagsQuotesDto> {
        try {
            ///logger.info("Fetching genres for video ID: $videoId")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/genres/$videoId"
            } else {
                "$apiBaseUrl/videos/genres/$videoId?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<TagsQuotesDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<TagsQuotesDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} genres from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<TagsQuotesDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<TagsQuotesDto>>>(
//                    "$apiBaseUrl/videos/genres/$videoId",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        val data = apiResponse.data ?: emptyList()
//                        logger.info("Retrieved ${data.size} genres from API after Redis failure")
//                        return data
//                    } else {
//                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return emptyList()
//                    }
//                } else {
//                    logger.error("API response body is null after Redis failure")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching genres after Redis failure: ${ex.message}", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching genres: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching genres: ${e.message}", e)
            return emptyList()
        }
    }

//    @Cacheable("players")
//    fun getPlayers(videoId: String, token: String? = null): List<VideoPlayersDto> {
//        try {
//            logger.info("Fetching players for video ID: $videoId")
//            // 构建URL，添加token参数
//            val url = if (token.isNullOrEmpty()) {
//                "$apiBaseUrl/videos/players/$videoId"
//            } else {
//                "$apiBaseUrl/videos/players/$videoId?token=$token"
//            }
//            logger.info("API URL: $url")
//
//            val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideoPlayersDto>>>() {}
//            val response = restTemplate.exchange<ApiResponse<List<VideoPlayersDto>>>(
//                url,
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                responseType
//            )
//
//            val apiResponse = response.body
//            if (apiResponse != null) {
//                logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
//                // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} players from API")
//                    return data
//                } else {
//                    logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } else {
//                logger.error("API response body is null")
//                return emptyList()
//            }
////        } catch (e: RedisConnectionFailureException) {
////            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
////            try {
////                logger.info("Retrying API call without Redis cache")
////                val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideoPlayersDto>>>() {}
////                val response = restTemplate.exchange<ApiResponse<List<VideoPlayersDto>>>(
////                    "$apiBaseUrl/videos/players/$videoId",
////                    HttpMethod.GET,
////                    HttpEntity.EMPTY,
////                    responseType
////                )
////
////                val apiResponse = response.body
////                if (apiResponse != null) {
////                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
////                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
////                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
////                        val data = apiResponse.data ?: emptyList()
////                        logger.info("Retrieved ${data.size} players from API after Redis failure")
////                        return data
////                    } else {
////                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
////                        return emptyList()
////                    }
////                } else {
////                    logger.error("API response body is null after Redis failure")
////                    return emptyList()
////                }
////            } catch (ex: RestClientException) {
////                logger.error("Error fetching players after Redis failure: ${ex.message}", ex)
////                return emptyList()
////            }
//        } catch (e: RestClientException) {
//            logger.error("Error fetching players: ${e.message}", e)
//            return emptyList()
//        } catch (e: Exception) {
//            logger.error("Unexpected error fetching players: ${e.message}", e)
//            return emptyList()
//        }
//    }

    ///@Cacheable("comments")
    fun getComments(videoId: String, typed: Int = 0, token: String? = null): List<CommentsDto> {
        try {
            ///logger.info("Fetching comments for video ID: $videoId, typed: $typed")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/comments/$videoId/$typed"
            } else {
                "$apiBaseUrl/videos/comments/$videoId/$typed?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<CommentsDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<CommentsDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} comments from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ////logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<CommentsDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<CommentsDto>>>(
//                    "$apiBaseUrl/videos/comments/$videoId/$typed",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        val data = apiResponse.data ?: emptyList()
//                        logger.info("Retrieved ${data.size} comments from API after Redis failure")
//                        return data
//                    } else {
//                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return emptyList()
//                    }
//                } else {
//                    logger.error("API response body is null after Redis failure")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching comments after Redis failure: ${ex.message}", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching comments: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching comments: ${e.message}", e)
            return emptyList()
        }
    }

    ///@Cacheable("moreLiked")
    fun getMoreLiked(videoId: String, orderBy: Int = 2, token: String? = null): List<VideosDto> {
        try {
            ///logger.info("Fetching more liked videos for ID: $videoId, orderBy: $orderBy")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/more-liked/$videoId/$orderBy"
            } else {
                "$apiBaseUrl/videos/more-liked/$videoId/$orderBy?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} more liked videos from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ////logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
//                    "$apiBaseUrl/more-liked/$videoId/$orderBy",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        val data = apiResponse.data ?: emptyList()
//                        logger.info("Retrieved ${data.size} more liked videos from API after Redis failure")
//                        return data
//                    } else {
//                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return emptyList()
//                    }
//                } else {
//                    logger.error("API response body is null after Redis failure")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching more liked videos after Redis failure: ${ex.message}", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching more liked videos: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching more liked videos: ${e.message}", e)
            return emptyList()
        }
    }

    ///@Cacheable("videoList")
    fun getVideoList(typed: Int = 0, released: Long = 0, orderBy: Int = 3, cate: String = "", token: String? = null): List<VideosDto> {
        try {
            ///logger.info("Fetching video list: typed=$typed, released=$released, orderBy=$orderBy, cate=$cate")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/list/$typed/$released/$orderBy?cate=$cate"
            } else {
                "$apiBaseUrl/videos/list/$typed/$released/$orderBy?cate=$cate&token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                //logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    //logger.info("Retrieved ${data.size} videos from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                //logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
//                    "$apiBaseUrl/videos/list/$typed/$released/$orderBy?cate=$cate",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null && apiResponse.code == "0") {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} videos from API after Redis failure")
//                    return data
//                } else {
//                    logger.error("API returned error after Redis failure: ${apiResponse?.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching video list after Redis failure", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching video list: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching video list: ${e.message}", e)
            return emptyList()
        }
    }

    ///@Cacheable("videoListFiltered")
    fun getVideoListFiltered(
        typed: Int = 0,
        page: Int = 1,
        size: Int = 24,
        code: Long = 0,
        year: Long = 0,
        orderBy: Int = 3,
        cate: String = "",
        tag: String = "",
        token: String? = null
    ): PaginatedResponse<VideosDto>? {
        try {
            ///logger.info("Fetching filtered video list: typed=$typed, page=$page, size=$size, code=$code, year=$year, orderBy=$orderBy, cate=$cate, tag=$tag")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/list?typed=$typed&page=$page&size=$size&code=$code&year=$year&order_by=$orderBy&cate=$cate&tag=$tag"
            } else {
                "$apiBaseUrl/videos/list?typed=$typed&page=$page&size=$size&code=$code&year=$year&order_by=$orderBy&cate=$cate&tag=$tag&token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<PaginatedResponse<VideosDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<PaginatedResponse<VideosDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    //logger.info("Retrieved paginated video list from API")
                    return apiResponse.data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return null
                }
            } else {
                ///logger.error("API response body is null")
                return null
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<PaginatedResponse<VideosDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<PaginatedResponse<VideosDto>>>(
//                    "$apiBaseUrl/videos/list?typed=$typed&page=$page&size=$size&code=$code&year=$year&order_by=$orderBy&cate=$cate&tag=$tag",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null) {
//                    logger.info("API response received after Redis failure: code=${apiResponse.code}, message=${apiResponse.message}")
//                    // 如果code为null或空字符串，但data不为null，则认为API返回成功
//                    if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
//                        logger.info("Retrieved paginated video list from API after Redis failure")
//                        return apiResponse.data
//                    } else {
//                        logger.error("API returned error after Redis failure: code=${apiResponse.code}, message=${apiResponse.message ?: "Unknown error"}")
//                        return null
//                    }
//                } else {
//                    logger.error("API response body is null after Redis failure")
//                    return null
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching filtered video list after Redis failure: ${ex.message}", ex)
//                return null
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching filtered video list: ${e.message}", e)
            return null
        } catch (e: Exception) {
            ////logger.error("Unexpected error fetching filtered video list: ${e.message}", e)
            return null
        }
    }

    ///@Cacheable("addresses")
    fun getAddresses(token: String? = null): List<AddressDto> {
        try {
            ///logger.info("Fetching addresses")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/address"
            } else {
                "$apiBaseUrl/videos/address?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<AddressDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<AddressDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} addresses from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<AddressDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<AddressDto>>>(
//                    "$apiBaseUrl/videos/address",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null && apiResponse.code == "0") {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} addresses from API after Redis failure")
//                    return data
//                } else {
//                    logger.error("API returned error after Redis failure: ${apiResponse?.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching addresses after Redis failure", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching addresses: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching addresses: ${e.message}", e)
            return emptyList()
        }
    }

    ////@Cacheable("categories")
    fun getCategories(typed: Int = 0, cate: String = "", token: String? = null): List<CategoriesDto> {
        try {
            ///logger.info("Fetching categories: typed=$typed, cate=$cate")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/categories?typed=$typed&cate=$cate"
            } else {
                "$apiBaseUrl/videos/categories?typed=$typed&cate=$cate&token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<CategoriesDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<CategoriesDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isNullOrEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} categories from API")
                    return data
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<CategoriesDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<CategoriesDto>>>(
//                    "$apiBaseUrl/videos/categories?typed=$typed&cate=$cate",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null && apiResponse.code == "0") {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} categories from API after Redis failure")
//                    return data
//                } else {
//                    logger.error("API returned error after Redis failure: ${apiResponse?.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching categories after Redis failure", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching categories: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            //logger.error("Unexpected error fetching categories: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * 获取所有视频类型和分类
     * 从/videos/categories端点获取完整的类型和分类数据
     */
    ///@Cacheable("videoTypes")
    fun getAllVideoTypesAndCategories(token: String? = null): List<CategoriesDto> {
        try {
            //logger.info("Fetching all video types and categories")
            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/categories"
            } else {
                "$apiBaseUrl/videos/categories?token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<CategoriesDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<CategoriesDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} top-level categories from API")

                    // 将所有分类展平为一个列表
                    val allCategories = mutableListOf<CategoriesDto>()

                    // 添加一级分类
                    allCategories.addAll(data)

                    // 添加二级分类
                    data.forEach { category ->
                        if (category.categoryList != null && category.categoryList!!.isNotEmpty()) {
                            allCategories.addAll(category.categoryList!!)
                        }
                    }

                    return allCategories
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<CategoriesDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<CategoriesDto>>>(
//                    "$apiBaseUrl/videos/categories",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null && apiResponse.code == "0") {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} top-level categories from API after Redis failure")
//
//                    // 将所有分类展平为一个列表
//                    val allCategories = mutableListOf<CategoriesDto>()
//
//                    // 添加一级分类
//                    allCategories.addAll(data)
//
//                    // 添加二级分类
//                    data.forEach { category ->
//                        if (category.categoryList != null && category.categoryList!!.isNotEmpty()) {
//                            allCategories.addAll(category.categoryList!!)
//                        }
//                    }
//
//                    return allCategories
//                } else {
//                    logger.error("API returned error after Redis failure: ${apiResponse?.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error fetching all video types and categories after Redis failure", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
            ///logger.error("Error fetching all video types and categories: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error fetching all video types and categories: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * 搜索视频
     * @param searchKey 搜索关键词
     * @param token 用户token（可选）
     * @return 搜索结果列表
     */
    ///@Cacheable("search")
    fun searchVideos(searchKey: String, token: String? = null): List<VideosDto> {
        try {
            //logger.info("Searching videos with key: $searchKey")

            // 构建URL，添加token参数
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/search?key=$searchKey"
            } else {
                "$apiBaseUrl/videos/search?key=$searchKey&token=$token"
            }
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
            val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为null或空字符串，但data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} search results from API")
                    return data
                } else {
                    ////logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()
            }
//        } catch (e: RedisConnectionFailureException) {
//            logger.warn("Redis connection failed, continuing with in-memory cache: ${e.message}")
//            try {
//                logger.info("Retrying API call without Redis cache")
//                val responseType = object : ParameterizedTypeReference<ApiResponse<List<VideosDto>>>() {}
//                val response = restTemplate.exchange<ApiResponse<List<VideosDto>>>(
//                    "$apiBaseUrl/videos/search?key=$searchKey" + if (!token.isNullOrEmpty()) "&token=$token" else "",
//                    HttpMethod.GET,
//                    HttpEntity.EMPTY,
//                    responseType
//                )
//
//                val apiResponse = response.body
//                if (apiResponse != null && apiResponse.code == "0") {
//                    val data = apiResponse.data ?: emptyList()
//                    logger.info("Retrieved ${data.size} search results from API after Redis failure")
//                    return data
//                } else {
//                    logger.error("API returned error after Redis failure: ${apiResponse?.message ?: "Unknown error"}")
//                    return emptyList()
//                }
//            } catch (ex: RestClientException) {
//                logger.error("Error searching videos after Redis failure", ex)
//                return emptyList()
//            }
        } catch (e: RestClientException) {
           ///logger.error("Error searching videos: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            ///logger.error("Unexpected error searching videos: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * 发布评论
     * @param videoId 视频ID
     * @param token 用户token
     * @param content 评论内容
     * @return 是否发布成功
     */
    fun postComment(videoId: String, token: String, content: String): Boolean {
        try {
            ///logger.info("Posting comment for video ID: $videoId")

            val headers = org.springframework.http.HttpHeaders()
            headers.contentType = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED

            val map = org.springframework.util.LinkedMultiValueMap<String, String>()
            map.add("content", content)

            val request = HttpEntity(map, headers)

            val url = "$apiBaseUrl/videos/comments-post/$videoId?token=$token"
            ///logger.info("API URL: $url")

            val responseType = object : ParameterizedTypeReference<ApiResponse<String>>() {}
            val response = restTemplate.exchange<ApiResponse<String>>(
                url,
                HttpMethod.POST,
                request,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为200，则认为发布成功
                return apiResponse.code == "200"
            } else {
                ///logger.error("API response body is null")
                return false
            }
        } catch (e: RestClientException) {
            ///logger.error("Error posting comment: ${e.message}", e)
            return false
        } catch (e: Exception) {
            ///logger.error("Unexpected error posting comment: ${e.message}", e)
            return false
        }
    }
}
