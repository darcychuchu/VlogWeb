package org.vlog.web.dto

data class FavoritesDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var isTyped: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,
    var quoteId: String? = null, // videoId
    var quoteType: Int? = null
)