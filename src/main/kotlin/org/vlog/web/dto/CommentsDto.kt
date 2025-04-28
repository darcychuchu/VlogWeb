package org.vlog.web.dto

data class CommentsDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,
    var isTyped: Int? = null,
    var createdBy: String? = null,
    var attachmentId: String? = null,
    var quoteId: String? = null, // videoId || Comments id
    var parentId: String? = null,
    var title: String? = null,
    var description: String? = null,

    var createdByItem: UsersDto? = null
)