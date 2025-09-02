package org.vlog.web.controller

import org.vlog.web.service.ApiService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant

@Controller
class HomeController(
    private val apiService: ApiService
) {
    ///private val logger = LoggerFactory.getLogger(HomeController::class.java)

    @GetMapping("/")
    fun index(
        model: Model,
        @RequestParam(required = false, defaultValue = "1") typed: Int,
        @RequestParam(required = false, defaultValue = "2025") year: Long,
        @RequestParam(required = false, defaultValue = "2") orderBy: Int
    ): String {
        try {
//            val videoList = apiService.getVideoList(typed, 1,24,year, orderBy)
//            model.addAttribute("videoList", videoList)

            val createdTime = Instant.now().toEpochMilli().div(1000).minus( 604800 )

            model.addAttribute("movieNewList", apiService.getVideoCharts(
                typed = 1,
                state = 1,
                title = "NEW",
                valued = 1,
                createdAt = createdTime)?.items)


            model.addAttribute("movieHotList", apiService.getVideoCharts(
                typed = 1,
                state = 1,
                title = "HOT",
                valued = 2,
                createdAt = createdTime)?.items)


            model.addAttribute("tvChineseList", apiService.getVideoCharts(
                typed = 2,
                state = 1,
                cate = "74fe8681-e071-499e-9f91-45f0447a8bb2",
                createdAt = createdTime)?.items)


            model.addAttribute("tvKoreanList", apiService.getVideoCharts(
                typed = 2,
                state = 1,
                cate = "065148b9-bc4a-4c05-80e3-230728407bbb",
                createdAt = createdTime)?.items)


            model.addAttribute("tvJapaneseList", apiService.getVideoCharts(
                typed = 2,
                state = 1,
                cate = "a7ba4cf7-0ef0-4eb5-8ecd-17c752c6b7aa",
                createdAt = createdTime)?.items)


            model.addAttribute("tvAmericanList", apiService.getVideoCharts(
                typed = 2,
                state = 1,
                cate = "d1f0f23d-4913-44b2-85d0-f89d41b1baec",
                createdAt = createdTime)?.items)

            model.addAttribute("animationList", apiService.getVideoCharts(
                typed = 3,
                state = 1,
                createdAt = createdTime)?.items)

            val categories = apiService.getCategories(typed).sortedBy { it.orderSort }
            model.addAttribute("categories", categories)

            model.addAttribute("typed", typed)
            model.addAttribute("year", year)
            model.addAttribute("orderBy", orderBy)
            model.addAttribute("appVersion", apiService.getAppVersion())

            return "index"
        } catch (e: Exception) {
            ///logger.error("Error loading home page: ${e.message}", e)
            model.addAttribute("errorMessage", "加载首页数据失败，请稍后再试")
            return "error"
        }
    }
}
