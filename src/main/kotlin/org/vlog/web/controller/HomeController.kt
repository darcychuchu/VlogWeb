package org.vlog.web.controller

import org.vlog.web.service.ApiService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController(
    private val apiService: ApiService
) {
    ///private val logger = LoggerFactory.getLogger(HomeController::class.java)

    @GetMapping("/")
    fun index(
        model: Model,
        @RequestParam(required = false, defaultValue = "1") typed: Int,
        @RequestParam(required = false, defaultValue = "2025") released: Long,
        @RequestParam(required = false, defaultValue = "2") orderBy: Int
    ): String {
        try {
            val videoList = apiService.getVideoList(typed, released, orderBy)
            model.addAttribute("videoList", videoList)

            val categories = apiService.getCategories(typed).sortedBy { it.orderSort }
            model.addAttribute("categories", categories)

            model.addAttribute("typed", typed)
            model.addAttribute("released", released)
            model.addAttribute("orderBy", orderBy)

            return "index"
        } catch (e: Exception) {
            ///logger.error("Error loading home page: ${e.message}", e)
            model.addAttribute("errorMessage", "加载首页数据失败，请稍后再试")
            return "error"
        }
    }
}
