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

/**
 * API服务类
 *
 * 这个服务类负责与后端API服务器进行通信，获取各种数据。
 * 采用前后端分离架构，本服务作为中间层，将前端请求转发到API服务器，
 * 并将API服务器的响应转换为前端需要的格式。
 *
 * 主要功能：
 * 1. 获取视频详情、演员、类型等信息
 * 2. 获取视频列表、筛选列表
 * 3. 获取分类、地址等基础数据
 * 4. 处理用户评论等交互操作
 *
 * 注意：缓存相关代码已被注释掉，因为在服务器上可能导致性能问题
 */
@Service
class ApiService(
    // RestTemplate用于发送HTTP请求
    private val restTemplate: RestTemplate,
    // 从配置文件中获取API基础URL
    @Value("\${api.base-url}") private val apiBaseUrl: String
) {
    // 日志记录器已被注释，避免产生过多日志
    ///private val logger = LoggerFactory.getLogger(ApiService::class.java)

    /**
     * 获取视频详情
     *
     * 根据视频ID和采集源ID获取视频的详细信息，包括标题、描述、封面、评分等。
     * 如果用户已登录（提供了token），则可能返回额外的用户相关信息，如是否收藏。
     *
     * @param id 视频ID，必需参数
     * @param gatherId 采集源ID，默认值为"5eeb44fc-2fb4-4ec2-b32e-63f50bfff707"
     * @param token 用户令牌，可选参数，用于获取用户相关的视频信息
     * @return 返回视频详情对象，如果获取失败则返回null
     */
    ////@Cacheable("videoDetails") // 缓存注解已被注释，避免服务器性能问题
    fun getVideoDetail(id: String, gatherId: String = "5eeb44fc-2fb4-4ec2-b32e-63f50bfff707", token: String? = null): VideosDto? {
        try {
            //logger.info("Fetching video details for ID: $id")

            // 创建一个参数化类型引用，用于处理API响应
            val responseType = object : ParameterizedTypeReference<ApiResponse<VideosDto>>() {}

            // 构建URL，如果有token则添加到请求中
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/detail/$id?gather=$gatherId"
            } else {
                "$apiBaseUrl/videos/detail/$id?gather=$gatherId&token=$token"
            }

            // 发送HTTP GET请求到API服务器
            val response = restTemplate.exchange<ApiResponse<VideosDto>>(
                url,                // 请求URL
                HttpMethod.GET,     // HTTP方法：GET
                HttpEntity.EMPTY,   // 请求体为空
                responseType        // 响应类型
            )

            // 获取响应体
            val apiResponse = response.body
            if (apiResponse != null) {
                //logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")

                // 检查API响应状态
                // 如果code为"0"或空字符串，且data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    //logger.info("Retrieved video details from API")
                    return apiResponse.data  // 返回视频详情数据
                } else {
                    //logger.debug("API returned error code: ${apiResponse.code}, message: ${apiResponse.message ?: "Unknown error"}")
                    return null  // API返回错误，返回null
                }
            } else {
                //logger.debug("API response body is null")
                return null  // 响应体为null，返回null
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
            // 捕获REST客户端异常，通常是网络问题或API服务器不可用
            ///logger.debug("Error fetching video details: ${e.message}")
            return null  // 发生异常时返回null
        }
    }

    /**
     * 获取视频演员信息
     *
     * 根据视频ID获取该视频的演员列表。
     *
     * @param videoId 视频ID，必需参数
     * @param token 用户令牌，可选参数
     * @return 返回演员列表，如果获取失败则返回空列表
     */
    ///@Cacheable("actors") // 缓存注解已被注释，避免服务器性能问题
    fun getActors(videoId: String, token: String? = null): List<TagsQuotesDto> {
        try {
            ///logger.info("Fetching actors for video ID: $videoId")

            // 构建URL，如果有token则添加到请求中
            val url = if (token.isNullOrEmpty()) {
                "$apiBaseUrl/videos/actors/$videoId"
            } else {
                "$apiBaseUrl/videos/actors/$videoId?token=$token"
            }
            ///logger.info("API URL: $url")

            // 创建一个参数化类型引用，用于处理API响应
            val responseType = object : ParameterizedTypeReference<ApiResponse<List<TagsQuotesDto>>>() {}

            // 发送HTTP GET请求到API服务器
            val response = restTemplate.exchange<ApiResponse<List<TagsQuotesDto>>>(
                url,                // 请求URL
                HttpMethod.GET,     // HTTP方法：GET
                HttpEntity.EMPTY,   // 请求体为空
                responseType        // 响应类型
            )

            // 获取响应体
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")

                // 检查API响应状态
                // 如果code为"0"或空字符串，且data不为null，则认为API返回成功
                if ((apiResponse.code == "0" || apiResponse.code.isEmpty()) && apiResponse.data != null) {
                    val data = apiResponse.data
                    ///logger.info("Retrieved ${data.size} actors from API")
                    return data  // 返回演员列表
                } else {
                    ///logger.error("API returned error code: ${apiResponse.code}, message: ${apiResponse.message}")
                    return emptyList()  // API返回错误，返回空列表
                }
            } else {
                ///logger.error("API response body is null")
                return emptyList()  // 响应体为null，返回空列表
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
            // 捕获REST客户端异常，通常是网络问题或API服务器不可用
            ///logger.error("Error fetching actors: ${e.message}", e)
            return emptyList()  // 发生REST异常时返回空列表
        } catch (e: Exception) {
            // 捕获其他所有异常
            ///logger.error("Unexpected error fetching actors: ${e.message}", e)
            return emptyList()  // 发生其他异常时返回空列表
        }
    }

    /**
     * 获取视频类型信息
     *
     * 根据视频ID获取该视频的类型/标签列表，如动作片、喜剧片等。
     *
     * @param videoId 视频ID，必需参数
     * @param token 用户令牌，可选参数
     * @return 返回类型列表，如果获取失败则返回空列表
     */
    ///@Cacheable("genres") // 缓存注解已被注释，避免服务器性能问题
    fun getGenres(videoId: String, token: String? = null): List<TagsQuotesDto> {
        try {
            ///logger.info("Fetching genres for video ID: $videoId")

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取视频评论
     *
     * 根据视频ID和评论类型获取该视频的评论列表。
     *
     * @param videoId 视频ID，必需参数
     * @param typed 评论类型，默认为0（所有评论）
     * @param token 用户令牌，可选参数
     * @return 返回评论列表，如果获取失败则返回空列表
     */
    ///@Cacheable("comments") // 缓存注解已被注释，避免服务器性能问题
    fun getComments(videoId: String, typed: Int = 0, token: String? = null): List<CommentsDto> {
        try {
            ///logger.info("Fetching comments for video ID: $videoId, typed: $typed")

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取相似推荐视频
     *
     * 根据视频ID获取与该视频相似或相关的视频列表，用于"猜你喜欢"等推荐功能。
     *
     * @param videoId 视频ID，必需参数
     * @param orderBy 排序方式，默认为2（按热度排序）
     * @param token 用户令牌，可选参数
     * @return 返回相似视频列表，如果获取失败则返回空列表
     */
    ///@Cacheable("moreLiked") // 缓存注解已被注释，避免服务器性能问题
    fun getMoreLiked(videoId: String, orderBy: Int = 2, token: String? = null): List<VideosDto> {
        try {
            ///logger.info("Fetching more liked videos for ID: $videoId, orderBy: $orderBy")

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取视频列表
     *
     * 根据类型、发布时间、排序方式和分类获取视频列表，主要用于首页和分类页面。
     *
     * @param typed 视频类型，默认为0（全部类型）
     * @param released 发布时间，默认为0（全部时间）
     * @param orderBy 排序方式，默认为3（按热度排序）
     * @param cate 分类ID，默认为空字符串（全部分类）
     * @param token 用户令牌，可选参数
     * @return 返回视频列表，如果获取失败则返回空列表
     */
    ///@Cacheable("videoList") // 缓存注解已被注释，避免服务器性能问题
    fun getVideoList(typed: Int = 0, released: Long = 0, orderBy: Int = 3, cate: String = "", token: String? = null): List<VideosDto> {
        try {
            ///logger.info("Fetching video list: typed=$typed, released=$released, orderBy=$orderBy, cate=$cate")

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取分页筛选视频列表
     *
     * 根据多种条件筛选视频，并支持分页，主要用于列表页面的高级筛选。
     *
     * @param typed 视频类型，默认为0（全部类型）
     * @param page 页码，默认为1（第一页）
     * @param size 每页数量，默认为24
     * @param code 地区代码，默认为0（全部地区）
     * @param year 年份，默认为0（全部年份）
     * @param orderBy 排序方式，默认为3（按热度排序）
     * @param cate 分类ID，默认为空字符串（全部分类）
     * @param tag 标签，默认为空字符串（无标签筛选）
     * @param token 用户令牌，可选参数
     * @return 返回分页视频列表，如果获取失败则返回null
     */
    ///@Cacheable("videoListFiltered") // 缓存注解已被注释，避免服务器性能问题
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

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取地区列表
     *
     * 获取所有可用的地区信息，用于视频筛选。
     *
     * @param token 用户令牌，可选参数
     * @return 返回地区列表，如果获取失败则返回空列表
     */
    ///@Cacheable("addresses") // 缓存注解已被注释，避免服务器性能问题
    fun getAddresses(token: String? = null): List<AddressDto> {
        try {
            ///logger.info("Fetching addresses")

            // 构建URL，如果有token则添加到请求中
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

    /**
     * 获取分类列表
     *
     * 根据视频类型获取分类列表，用于视频筛选。
     *
     * @param typed 视频类型，默认为0（全部类型）
     * @param cate 父分类ID，默认为空字符串（获取顶级分类）
     * @param token 用户令牌，可选参数
     * @return 返回分类列表，如果获取失败则返回空列表
     */
    ////@Cacheable("categories") // 缓存注解已被注释，避免服务器性能问题
    fun getCategories(typed: Int = 0, cate: String = "", token: String? = null): List<CategoriesDto> {
        try {
            ///logger.info("Fetching categories: typed=$typed, cate=$cate")

            // 构建URL，如果有token则添加到请求中
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
     *
     * 从/videos/categories端点获取完整的类型和分类数据，包括所有层级的分类。
     * 与getCategories方法不同，此方法会将所有分类（包括子分类）展平为一个列表。
     *
     * @param token 用户令牌，可选参数
     * @return 返回所有分类的展平列表，如果获取失败则返回空列表
     */
    ///@Cacheable("videoTypes") // 缓存注解已被注释，避免服务器性能问题
    fun getAllVideoTypesAndCategories(token: String? = null): List<CategoriesDto> {
        try {
            //logger.info("Fetching all video types and categories")

            // 构建URL，如果有token则添加到请求中
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

                    // 将所有分类展平为一个列表，包括一级分类和二级分类
                    val allCategories = mutableListOf<CategoriesDto>()

                    // 添加一级分类（顶级分类）
                    allCategories.addAll(data)

                    // 添加二级分类（子分类）
                    // 遍历每个顶级分类，将其子分类添加到结果列表中
                    data.forEach { category ->
                        if (category.categoryList != null && category.categoryList!!.isNotEmpty()) {
                            allCategories.addAll(category.categoryList!!)
                        }
                    }

                    // 返回展平后的完整分类列表
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
     *
     * 根据关键词搜索视频，返回匹配的视频列表。
     *
     * @param searchKey 搜索关键词，必需参数
     * @param token 用户令牌，可选参数
     * @return 返回搜索结果列表，如果搜索失败则返回空列表
     */
    ///@Cacheable("search") // 缓存注解已被注释，避免服务器性能问题
    fun searchVideos(searchKey: String, token: String? = null): List<VideosDto> {
        try {
            //logger.info("Searching videos with key: $searchKey")

            // 构建URL，如果有token则添加到请求中
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
     *
     * 向指定视频发布用户评论。需要用户已登录（提供有效token）。
     *
     * @param videoId 视频ID，必需参数
     * @param token 用户令牌，必需参数，用于验证用户身份
     * @param content 评论内容，必需参数
     * @return 返回布尔值，表示评论是否发布成功
     */
    fun postComment(videoId: String, token: String, content: String): Boolean {
        try {
            ///logger.info("Posting comment for video ID: $videoId")

            // 创建HTTP头，设置内容类型为表单数据
            val headers = org.springframework.http.HttpHeaders()
            headers.contentType = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED

            // 创建表单数据
            val map = org.springframework.util.LinkedMultiValueMap<String, String>()
            map.add("content", content)  // 添加评论内容

            // 创建HTTP请求实体，包含表单数据和头信息
            val request = HttpEntity(map, headers)

            // 构建API URL，包含视频ID和用户token
            val url = "$apiBaseUrl/videos/comments-post/$videoId?token=$token"
            ///logger.info("API URL: $url")

            // 创建参数化类型引用，用于处理API响应
            val responseType = object : ParameterizedTypeReference<ApiResponse<String>>() {}

            // 发送HTTP POST请求到API服务器
            val response = restTemplate.exchange<ApiResponse<String>>(
                url,                // 请求URL
                HttpMethod.POST,    // HTTP方法：POST
                request,            // 请求实体（包含表单数据）
                responseType        // 响应类型
            )

            // 获取响应体
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}")
                // 如果code为200，则认为评论发布成功
                return apiResponse.code == "200"
            } else {
                ///logger.error("API response body is null")
                return false  // 响应体为null，发布失败
            }
        } catch (e: RestClientException) {
            // 捕获REST客户端异常，通常是网络问题或API服务器不可用
            ///logger.error("Error posting comment: ${e.message}", e)
            return false  // 发生REST异常，发布失败
        } catch (e: Exception) {
            // 捕获其他所有异常
            ///logger.error("Unexpected error posting comment: ${e.message}", e)
            return false  // 发生其他异常，发布失败
        }
    }
}
