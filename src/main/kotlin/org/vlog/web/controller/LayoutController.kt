package org.vlog.web.controller

import org.vlog.web.dto.CategoriesDto
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * 布局控制器，用于向所有视图添加公共数据
 */
@ControllerAdvice
class LayoutController {

    /**
     * 向所有视图添加视频类型列表
     */
    @ModelAttribute("navVideoTypes")
    fun addVideoTypes(): List<CategoriesDto> {
        // 返回视频类型列表
        return listOf(
            CategoriesDto(id = "1", title = "电影", isTyped = 1),
            CategoriesDto(id = "2", title = "电视剧", isTyped = 2),
            CategoriesDto(id = "3", title = "动漫", isTyped = 3),
            CategoriesDto(id = "4", title = "综艺", isTyped = 4)
        )
    }
}
