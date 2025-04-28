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
        @RequestParam(required = false, defaultValue = "0") typed: Int,
        @RequestParam(required = false, defaultValue = "0") released: Long,
        @RequestParam(required = false, defaultValue = "3") orderBy: Int,
        @RequestParam(required = false, defaultValue = "") cate: String
    ): String {
        ///logger.info("Accessing home page with typed=$typed, released=$released, orderBy=$orderBy, cate=$cate")

        try {
            // 获取视频列表
            val videoList = apiService.getVideoList(typed, released, orderBy, cate)
            model.addAttribute("videoList", videoList)

            // 获取分类列表
            val categories = apiService.getCategories(typed, cate)
            model.addAttribute("categories", categories)

            // 获取地址列表
            val addresses = apiService.getAddresses()
            model.addAttribute("addresses", addresses)

            // 添加筛选参数
            model.addAttribute("typed", typed)
            model.addAttribute("released", released)
            model.addAttribute("orderBy", orderBy)
            model.addAttribute("cate", cate)

            return "index"
        } catch (e: Exception) {
            ///logger.error("Error loading home page: ${e.message}", e)
            model.addAttribute("errorMessage", "加载首页数据失败，请稍后再试")
            return "error"
        }
    }
}
