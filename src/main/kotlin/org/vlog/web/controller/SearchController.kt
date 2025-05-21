package org.vlog.web.controller

import org.vlog.web.dto.UsersDto
import org.vlog.web.service.ApiService
import org.vlog.web.util.RetryUtil
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 搜索控制器
 * 处理搜索相关请求
 */
@Controller
@RequestMapping("/search")
class SearchController(
    private val apiService: ApiService
) {
    ///private val logger = LoggerFactory.getLogger(SearchController::class.java)

    /**
     * 搜索页面
     * @param keyword 搜索关键词
     * @param model 模型
     * @param session 会话
     * @return 视图名称
     */
    @GetMapping
    fun search(
        @RequestParam(required = false) keyword: String?,
        model: Model,
        session: HttpSession
    ): String {
        //logger.info("Accessing search page with keyword: $keyword")

        // 如果关键词为空，返回空结果页面
        if (keyword.isNullOrBlank()) {
            model.addAttribute("keyword", "")
            model.addAttribute("videos", emptyList<Any>())
            model.addAttribute("totalCount", 0)
            return "search"
        }

        try {

            // 获取用户token（如果已登录）
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken
            val searchResults = apiService.searchVideos(keyword, token)
//            model.addAttribute("videoList", videoList)
//
//            // 尝试从缓存获取搜索结果
//            val searchResults = RetryUtil.executeWithRetryAndFallback(
//                maxRetries = 2,
//                operation = { apiService.searchVideos(keyword, token) },
//                fallback = { emptyList() }
//            )

            // 添加到模型
            model.addAttribute("keyword", keyword)
            model.addAttribute("videos", searchResults)
            model.addAttribute("totalCount", searchResults.size)

            return "search"
        } catch (e: Exception) {
            ///logger.debug("Error during search: ${e.message}")
            model.addAttribute("errorMessage", "搜索过程中发生错误，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }
}
