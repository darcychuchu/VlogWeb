package org.vlog.web.dto

data class CategoriesDto(
    var id: String? = null,
    var title: String? = null,
    var path: String? = null,
    var description: String? = null,
    var isTyped: Int? = null,
    var createdBy: String? = null,
    var modelId: String? = null,
    var parentId: String? = null,

    var categoryList: List<CategoriesDto>? = null,
)