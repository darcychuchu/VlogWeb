package org.vlog.web.controller

//import org.vlog.web.dto.UsersDto
//import org.vlog.web.service.ApiService
//import jakarta.servlet.http.HttpSession
//import org.slf4j.LoggerFactory
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*

///**
// * 评论控制器
// * 处理评论相关请求
// */
//@RestController
//@RequestMapping("/api/comment")
//class CommentController(
//    private val apiService: ApiService
//) {
//    ///private val logger = LoggerFactory.getLogger(CommentController::class.java)
//
//    /**
//     * 发布评论
//     * @param videoId 视频ID
//     * @param content 评论内容
//     * @param session 会话
//     * @return 响应结果
//     */
//    @PostMapping("/post/{videoId}")
//    fun postComment(
//        @PathVariable videoId: String,
//        @RequestParam content: String,
//        session: HttpSession
//    ): ResponseEntity<Map<String, Any>> {
//        //logger.info("Posting comment for video ID: $videoId")
//
//        // 检查用户是否已登录
//        val user = session.getAttribute("user") as? UsersDto
//        if (user == null) {
//            ///logger.debug("User not logged in")
//            return ResponseEntity.ok(mapOf(
//                "success" to false,
//                "message" to "请先登录后再发表评论"
//            ))
//        }
//
//        // 检查评论内容是否为空
//        if (content.isBlank()) {
//            ///logger.debug("Comment content is empty")
//            return ResponseEntity.ok(mapOf(
//                "success" to false,
//                "message" to "评论内容不能为空"
//            ))
//        }
//
//        try {
//            // 调用API发布评论
//            val success = apiService.postComment(videoId, user.accessToken ?: "", content)
//
//            return if (success) {
//                //logger.info("Comment posted successfully")
//                ResponseEntity.ok(mapOf(
//                    "success" to true,
//                    "message" to "评论发布成功"
//                ))
//            } else {
//                ///logger.debug("Failed to post comment")
//                ResponseEntity.ok(mapOf(
//                    "success" to false,
//                    "message" to "评论发布失败，请稍后再试"
//                ))
//            }
//        } catch (e: Exception) {
//            ///logger.error("Error posting comment: ${e.message}", e)
//            return ResponseEntity.ok(mapOf(
//                "success" to false,
//                "message" to "评论发布失败，请稍后再试"
//            ))
//        }
//    }
//}
