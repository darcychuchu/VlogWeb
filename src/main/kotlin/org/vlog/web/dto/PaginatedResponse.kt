package org.vlog.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaginatedResponse<T>(
    // API返回的字段
    val items: List<T> = emptyList(),
    val total: String = "0",
    val page: String = "1",
    val pageSize: String = "24",

    // 兼容Thymeleaf模板中使用的字段
    val content: List<T> = items,
    val totalElements: Long = total.toLongOrNull() ?: 0,
    val totalPages: Int = if ((pageSize.toIntOrNull() ?: 0) > 0) ((total.toLongOrNull() ?: 0) / (pageSize.toLongOrNull() ?: 1)).toInt() + 1 else 0,
    val size: Int = pageSize.toIntOrNull() ?: 0,
    val number: Int = (page.toIntOrNull() ?: 1) - 1,  // 页码从0开始
    val numberOfElements: Int = items.size,
    val first: Boolean = page == "1",
    val last: Boolean = (page.toIntOrNull() ?: 1) >= totalPages,
    val empty: Boolean = items.isEmpty()
) {
    // 添加辅助构造函数，确保content和items保持一致
    constructor(
        items: List<T>,
        total: String,
        page: String,
        pageSize: String
    ) : this(
        items = items,
        total = total,
        page = page,
        pageSize = pageSize,
        content = items
    )
}
