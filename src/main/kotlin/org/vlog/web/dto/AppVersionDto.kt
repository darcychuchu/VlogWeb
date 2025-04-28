package org.vlog.web.dto

data class AppVersionDto(
    val versionCode: Int = 0,
    val versionName: String? = null,
    val forceUpdate: Boolean = false,
    val downloadUrl: String? = null,
    val description: String? = null,
    val fileSize: Long = 0,
    val md5: String? = null
)