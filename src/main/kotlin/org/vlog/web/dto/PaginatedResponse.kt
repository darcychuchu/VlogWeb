package org.vlog.web.dto

data class PaginatedResponse<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 24,
)
