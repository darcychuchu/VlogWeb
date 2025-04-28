package org.vlog.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UsersDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var name: String? = null,
    var nickName: String? = null,
    var description: String? = null,
    var avatar: String? = null,
    var accessToken: String? = null,
    var isLocked: Int? = null  // 用于判断是否更改过昵称，1表示已锁定（不能更改），0表示未锁定（可以更改）
)