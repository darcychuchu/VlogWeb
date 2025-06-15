package org.vlog.web.dto


data class FavoritesDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var orderSort: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,
    var quoteId: String? = null,
    var quoteType: Int? = null
)