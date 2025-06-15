package org.vlog.web.dto

import java.math.BigDecimal

data class VideoDetailDto(
    var id: String? = null,
    var version: Int? = null,
    var isTyped: Int? = null,
    var releasedAt: Long? = null,
    var isRecommend: Int? = null,
    var publishedAt: String? = null,
    var orderSort: Int? = null,
    var categoryId: String? = null,
    var title: String? = null,
    var score: BigDecimal? = null,
    var alias: String? = null,
    var director: String? = null,
    var actors: String? = null,
    var region: String? = null,
    var language: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var author: String? = null,
    var remarks: String? = null,
    var coverUrl: String? = null,

    var gatherListVersion: Int? = null,
    var gatherList: MutableList<GatherSourcesDto>? = mutableListOf()
)