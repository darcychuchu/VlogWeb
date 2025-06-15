package org.vlog.web.dto

data class GatherSourcesDto(
    var id: String? = null,
    var gatherId: String,
    var gatherTitle: String,
    var gatherTips: String,
    var remarks: String? = null,
    val playList: List<GatherSourcesPlayersDto>? = null
)

data class GatherSourcesPlayersDto(
    var title: String? = null,
    var playUrl: String? = null
)

data class GatherListDto(
    var videoId: String? = null,
    var version: Int? = 0,
    var gatherList: List<GatherSourcesDto>? = mutableListOf()
)