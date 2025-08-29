package org.vlog.web.controller

import org.vlog.web.service.ApiService
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
) {

    @GetMapping("/videos/detail/{id}", "/detail/{id}")
    fun detail(
        @PathVariable id: String,
        @RequestParam(defaultValue = "1") typed: Int,
        model: Model,
        session: HttpSession
    ): String {
        try {
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken
            val videoDetail = apiService.getVideoDetail(id=id,typed=typed,token=token) ?: return "error"
            model.addAttribute("VideoItem", videoDetail)
            model.addAttribute("appVersion", apiService.getAppVersion())
            return "detail"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "加载视频详情失败，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }

    @GetMapping("/list","/filter")
    fun list(
        model: Model,
        @RequestParam(required = false, defaultValue = "0") typed: Int,
        @RequestParam(required = false, defaultValue = "1") page: Int = 1,
        @RequestParam(required = false, defaultValue = "24") size: Int = 24,
        @RequestParam(required = false, defaultValue = "0") year: Long = 0,
        @RequestParam(required = false, defaultValue = "5") orderBy: Int = 5,
        @RequestParam(required = false, defaultValue = "") cate: String = "",
        session: HttpSession
    ): String {
        try {
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken

            val paginatedVideos = apiService.getVideoListFiltered(typed, page, size, year, orderBy, cate, token)

            if (paginatedVideos == null) {
                model.addAttribute("errorMessage", "加载视频列表失败，请稍后再试")
                model.addAttribute("errorDetails", "无法从服务器获取视频列表")
                model.addAttribute("errorCode", "500")
                return "error"
            }
            val categories = apiService.getCategories(typed).sortedByDescending { it.orderSort }

            // 获取当前选中的类型
            val currentType = if (typed > 0) {
                categories.find { it.modelTyped == typed }
            } else {
                null
            }

            // 获取当前选中的分类
            val currentCategory = if (cate.isNotEmpty()) {
                categories.find { it.id == cate }
            } else {
                null
            }
            currentCategory?.categoryList = if (currentCategory?.categoryList != null)currentCategory.categoryList?.sortedByDescending { it.orderSort } else null

            model.addAttribute("paginatedVideos", paginatedVideos)
            model.addAttribute("categories", categories)  // 当前类型的分类列表
            model.addAttribute("currentType", currentType)  // 当前选中的类型
            model.addAttribute("currentCategory", currentCategory)  // 当前选中的分类
            // 添加筛选参数
            model.addAttribute("typed", typed)
            model.addAttribute("page", page)
            model.addAttribute("size", size)
            model.addAttribute("year", year)
            model.addAttribute("orderBy", orderBy)
            model.addAttribute("cate", cate)
            model.addAttribute("appVersion", apiService.getAppVersion())
            return "list"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "加载视频列表失败，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }


    @GetMapping("/charts")
    fun charts(
        model: Model,
        @RequestParam(required = false, defaultValue = "0") typed: Int,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "12") size: Int = 12,
        @RequestParam(required = false, defaultValue = "0") createdAt: Long = 0,
        @RequestParam(required = false, defaultValue = "0") orderBy: Int = 0,
        @RequestParam(required = false, defaultValue = "0") state: Int = 0,
        @RequestParam(required = false, defaultValue = "0") valued: Int = 0,
        @RequestParam(required = false, defaultValue = "") cate: String = "",
        @RequestParam(required = false, defaultValue = "") title: String = "",
        session: HttpSession
    ): String {

        try {
            val user = session.getAttribute("user") as? UsersDto
            val token = user?.accessToken

            val listVideos = apiService.getVideoCharts(typed, page, size, state,
                valued,cate,title, orderBy,createdAt,token)

            model.addAttribute("listVideos", listVideos)

            // 添加筛选参数
            model.addAttribute("typed", typed)
            model.addAttribute("page", page)
            model.addAttribute("size", size)
            model.addAttribute("createdAt", createdAt)
            model.addAttribute("state", state)
            model.addAttribute("valued", valued)
            model.addAttribute("orderBy", orderBy)
            model.addAttribute("cate", cate)
            model.addAttribute("title", title)
            return "charts"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "加载视频列表失败，请稍后再试")
            model.addAttribute("errorDetails", "系统内部错误")
            model.addAttribute("errorCode", "500")
            return "error"
        }
    }
}
