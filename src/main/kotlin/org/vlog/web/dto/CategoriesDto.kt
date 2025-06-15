package org.vlog.web.dto

data class CategoriesDto(
    var id: String? = null,
    var orderSort: Int? = null,
    var version: Int? = null,
    var parentId: String? = null,
    var modelId: String? = null,
    var modelTyped: Int? = null,
    var title: String? = null,

    var categoryList: List<CategoriesDto>? = null
)