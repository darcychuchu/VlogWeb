package org.vlog.web.dto

data class GatherSourcesDto(
    var gatherId: String,
    var gatherTitle: String,
    var playerHost: String? = null,
    var playerPort: Int? = null,
    var remarks: String? = null,
    val playList: List<GatherSourcesPlayersDto>? = null
)

data class GatherSourcesPlayersDto(
    var title: String? = null,
    var path: String? = null,
    var playUrl: String? = null
)