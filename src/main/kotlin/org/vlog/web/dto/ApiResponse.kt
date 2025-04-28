package org.vlog.web.dto

data class ApiResponse<T>(
    val code: String = "",
    val message: String = "",
    val data: T? = null
)
