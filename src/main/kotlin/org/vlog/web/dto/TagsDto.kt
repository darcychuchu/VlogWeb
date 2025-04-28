package org.vlog.web.dto

data class TagsDto(
    var videoId: String? = null,
    var id: String? = null,
    var title: String? = null,
    var orderSort: Long? = null,
)