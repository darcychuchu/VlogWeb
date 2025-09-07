package org.vlog.web.dto

import java.math.BigDecimal

data class VideoListDto(
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
    var tags: String? = null,
    var remarks: String? = null,
    var coverUrl: String? = null,
    var gatherVideoId: String? = null,
)