package org.vlog.web.dto

import java.math.BigDecimal

data class VideosDto(
    var id: String? = null,
    var categoryId: String? = null,
    var attachmentId: String? = null,
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
    var coverUrl: String? = null,
    var videoPlayList: List<GatherSourcesDto>? = listOf()
)