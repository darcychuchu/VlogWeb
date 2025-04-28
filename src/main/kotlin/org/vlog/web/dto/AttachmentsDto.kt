/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2025-02-07T02:34:34.323526+08:00
 */
package org.vlog.web.dto

data class AttachmentsDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var isTyped: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,
    var resourceId: String? = null,
    var quoteId: String? = null, // videoId
    var quoteTyped: Int? = null,
    var size: Long? = null
)