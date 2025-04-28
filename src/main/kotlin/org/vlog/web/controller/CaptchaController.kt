package org.vlog.web.controller

import org.vlog.web.service.CaptchaService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/captcha")
class CaptchaController(private val captchaService: CaptchaService) {
    
    /**
     * 生成验证码
     * @return 包含验证码图片和会话ID的JSON对象
     */
    @GetMapping("/generate", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun generateCaptcha(): ResponseEntity<Map<String, String>> {
        val captchaResult = captchaService.generateCaptcha()
        
        val response = mapOf(
            "imageBase64" to captchaResult.imageBase64,
            "sessionId" to captchaResult.sessionId
        )
        
        return ResponseEntity.ok(response)
    }
}
