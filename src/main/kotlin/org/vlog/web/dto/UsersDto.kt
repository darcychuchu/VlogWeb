package org.vlog.web.dto

data class UsersDto(
    var id: String? = null,
    var isLocked: Int? = null,
    var createdAt: Long? = null,
    var name: String? = null,
    var nickName: String? = null,
    var description: String? = null,
    var avatar: String? = null,
    var accessToken: String? = null
)