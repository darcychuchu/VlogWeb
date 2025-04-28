package org.vlog.web.controller

import org.vlog.web.dto.CategoriesDto
import org.vlog.web.service.ApiService
//import org.vlog.web.service.EnhancedCacheService
//import org.vlog.web.service.HealthCheckService
import org.vlog.web.util.RetryUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.vlog.web.dto.UsersDto
import jakarta.servlet.http.HttpSession

@Controller
class VideoController(
    private val apiService: ApiService,
//    private val enhancedCacheService: EnhancedCacheService,
//    private val healthCheckService: HealthCheckService
) {
    //private val logger = LoggerFactory.getLogger(VideoController::class.java)

    @GetMapping("/videos/detail/{id}", "/detail/{id}")
    fun detail(
        @PathVariable id: String,
        model: Model,
        session: HttpSession
    ): String {
        //logger.info("Accessing video detail page for ID: $id")

        try {
            // 获取用户token（如果已登录）
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken

            // 使用重试机制获取视频基本信息
            val videoDetail = apiService.getVideoDetail(id=id, token=token) ?: return "error"
//            RetryUtil.executeWithRetry(maxRetries = 3) {
////                enhancedCacheService.getVideoBasicInfo(id)
//                apiService.getVideoDetail(id=id, token=token)
//            }

            //println("Video not found for ID: $videoDetail")
            //val videoDetail = apiService.getVideoDetail(id=id, token=token)

//            if (videoDetail == null) {
//                logger.debug("Video not found for ID: $id")
//                model.addAttribute("errorMessage", "视频不存在或已被删除")
//                model.addAttribute("errorDetails", "无法找到ID为 $id 的视频")
//                model.addAttribute("errorCode", "404")
//                return "error"
//            }

            // 使用重试机制获取演员列表，并提供降级策略
            val actors = apiService.getActors(id, token)
//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.getActors(id, token) },
//                fallback = { emptyList() }
//            )

            // 使用重试机制获取类型列表，并提供降级策略
            val genres = apiService.getGenres(id, token)
//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.getGenres(id, token) },
//                fallback = { emptyList() }
//            )

            // 使用重试机制获取播放列表，并提供降级策略
            val players = videoDetail.videoPlayList

//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 3,
//                operation = {
//                    videoDetail.videoPlayList
//                    //apiService.getPlayers(id, token)
//                            },
//                fallback = { emptyList() }
//            )

            // 使用重试机制获取评论列表，并提供降级策略
            val comments = apiService.getComments(id, 0, token)
//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.getComments(id, 0, token) },
//                fallback = { emptyList() }
//            )

            // 使用重试机制获取相似推荐，并提供降级策略
            val moreLiked = apiService.getMoreLiked(id, 2, token)
//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.getMoreLiked(id, 2, token) },
//                fallback = { emptyList() }
//            )

            // 添加到模型
            model.addAttribute("VideoItem", videoDetail)
            model.addAttribute("Actors", actors)
            model.addAttribute("Genres", genres)
            model.addAttribute("Players", players)
            model.addAttribute("Comments", comments)
            model.addAttribute("MoreLiked", moreLiked)

            return "detail"
        } catch (e: Exception) {
            //logger.debug("Error loading video detail page: ${e.message}")
            model.addAttribute("errorMessage", "加载视频详情失败，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }

    @GetMapping("/list")
    fun list(
        model: Model,
        @RequestParam(required = false, defaultValue = "0") typed: Int,
        @RequestParam(required = false, defaultValue = "1") page: Int = 1,
        @RequestParam(required = false, defaultValue = "24") size: Int = 24,
        @RequestParam(required = false, defaultValue = "0") code: Long = 0,
        @RequestParam(required = false, defaultValue = "0") year: Long = 0,
        @RequestParam(required = false, defaultValue = "3") orderBy: Int = 3,
        @RequestParam(required = false, defaultValue = "") cate: String = "",
        @RequestParam(required = false, defaultValue = "") tag: String = "",
        session: HttpSession
    ): String {
        //logger.info("Accessing list page with typed=$typed, page=$page, size=$size, code=$code, year=$year, orderBy=$orderBy, cate=$cate, tag=$tag")

        try {
            // 获取用户token（如果已登录）
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken

//            // 检查API服务是否可用
//            if (!healthCheckService.isApiAvailable()) {
//                logger.warn("API服务不可用，返回错误页面")
//                model.addAttribute("errorMessage", "服务暂时不可用，请稍后再试")
//                model.addAttribute("errorDetails", "API服务当前不可用")
//                model.addAttribute("errorCode", "503")
//                return "error"
//            }

            // 如果不是电影类型，则忽略地区筛选参数
            val actualCode = if (typed == 1) code else 0L

            // 使用重试机制获取分页视频列表
            val paginatedVideos = apiService.getVideoListFiltered(typed, page, size, actualCode, year, orderBy, cate, tag, token)
//                RetryUtil.executeWithRetry(maxRetries = 3) {
//                apiService.getVideoListFiltered(typed, page, size, actualCode, year, orderBy, cate, tag, token)
//            }

            if (paginatedVideos == null) {
                //logger.debug("无法获取视频列表")
                model.addAttribute("errorMessage", "加载视频列表失败，请稍后再试")
                model.addAttribute("errorDetails", "无法从服务器获取视频列表")
                model.addAttribute("errorCode", "500")
                return "error"
            }

            // 获取类型列表（作为类型使用）
            val videoTypes = listOf(
                CategoriesDto(id = "1", title = "电影", isTyped = 1),
                CategoriesDto(id = "2", title = "电视剧", isTyped = 2),
                CategoriesDto(id = "3", title = "动漫", isTyped = 3),
                CategoriesDto(id = "4", title = "综艺", isTyped = 4)
            )

            // 使用重试机制获取分类
            val categories = if (typed > 0) {
                apiService.getCategories(typed, cate, token)
                // 如果选择了类型，获取该类型下的分类
//                RetryUtil.executeWithRetryAndFallback(
//                    maxRetries = 2,
//                    operation = { apiService.getCategories(typed, cate, token) },
//                    fallback = { emptyList() }
//                )
            } else {
                // 如果没有选择类型，显示空列表
                emptyList()
            }

            // 获取当前选中的类型
            val currentType = if (typed > 0) {
                videoTypes.find { it.isTyped?.toInt() == typed }
            } else {
                null
            }

            // 获取当前选中的分类
            val currentCategory = if (cate.isNotEmpty()) {
                categories.find { it.id == cate }
            } else {
                null
            }

            // 使用重试机制获取地址列表
            val addresses = apiService.getAddresses(token)
//            RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.getAddresses(token) },
//                fallback = { emptyList() }
//            )

//            // 如果是列表查询，缓存结果（1小时）
//            if (healthCheckService.isRedisAvailable()) {
//                val listKey = "list_${typed}_${page}_${size}_${code}_${year}_${orderBy}_${cate}_${tag}"
//                enhancedCacheService.cacheListResults(listKey, paginatedVideos)
//            }

            // 添加到模型
            model.addAttribute("paginatedVideos", paginatedVideos)
            model.addAttribute("videoTypes", videoTypes)  // 添加视频类型列表
            model.addAttribute("categories", categories)  // 当前类型的分类列表
            model.addAttribute("currentType", currentType)  // 当前选中的类型
            model.addAttribute("currentCategory", currentCategory)  // 当前选中的分类
            model.addAttribute("addresses", addresses)

            // 添加筛选参数
            model.addAttribute("typed", typed)
            model.addAttribute("page", page)
            model.addAttribute("size", size)
            model.addAttribute("code", code)
            model.addAttribute("year", year)
            model.addAttribute("orderBy", orderBy)
            model.addAttribute("cate", cate)
            model.addAttribute("tag", tag)

            return "list"
        } catch (e: Exception) {
            //logger.debug("Error loading list page: ${e.message}")
            model.addAttribute("errorMessage", "加载视频列表失败，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }
}
