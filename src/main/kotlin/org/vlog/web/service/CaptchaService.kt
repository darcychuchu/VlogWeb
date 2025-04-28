package org.vlog.web.service

import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.random.Random

@Service
class CaptchaService {
    
    companion object {
        private const val CAPTCHA_LENGTH = 4
        private const val CAPTCHA_WIDTH = 120
        private const val CAPTCHA_HEIGHT = 40
        private const val CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000 // 5分钟过期
        private val CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()
    }
    
    // 存储验证码和过期时间
    private val captchaMap = mutableMapOf<String, CaptchaInfo>()
    
    /**
     * 生成验证码
     * @return 包含验证码图片和会话ID的对象
     */
    fun generateCaptcha(): CaptchaResult {
        // 生成随机验证码
        val captchaText = generateRandomText()
        
        // 生成验证码图片
        val captchaImage = generateCaptchaImage(captchaText)
        
        // 生成会话ID
        val sessionId = UUID.randomUUID().toString()
        
        // 存储验证码信息
        captchaMap[sessionId] = CaptchaInfo(
            captchaText,
            System.currentTimeMillis() + CAPTCHA_EXPIRE_TIME
        )
        
        // 清理过期的验证码
        cleanExpiredCaptchas()
        
        return CaptchaResult(captchaImage, sessionId)
    }
    
    /**
     * 验证验证码
     * @param sessionId 会话ID
     * @param captchaText 用户输入的验证码
     * @return 验证结果
     */
    fun validateCaptcha(sessionId: String, captchaText: String): Boolean {
        val captchaInfo = captchaMap[sessionId] ?: return false
        
        // 检查是否过期
        if (captchaInfo.expireTime < System.currentTimeMillis()) {
            captchaMap.remove(sessionId)
            return false
        }
        
        // 验证码验证成功后，立即删除，防止重复使用
        val isValid = captchaInfo.text.equals(captchaText, ignoreCase = true)
        if (isValid) {
            captchaMap.remove(sessionId)
        }
        
        return isValid
    }
    
    /**
     * 生成随机验证码文本
     */
    private fun generateRandomText(): String {
        val sb = StringBuilder()
        for (i in 0 until CAPTCHA_LENGTH) {
            val randomIndex = Random.nextInt(CAPTCHA_CHARS.size)
            sb.append(CAPTCHA_CHARS[randomIndex])
        }
        return sb.toString()
    }
    
    /**
     * 生成验证码图片
     * @param text 验证码文本
     * @return 验证码图片的Base64编码
     */
    private fun generateCaptchaImage(text: String): String {
        // 创建图片
        val image = BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // 设置背景
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
        
        // 绘制干扰线
        drawInterferenceLines(g2d)
        
        // 绘制干扰点
        drawInterferencePoints(g2d)
        
        // 绘制验证码文本
        drawCaptchaText(g2d, text)
        
        g2d.dispose()
        
        // 将图片转换为Base64编码
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val imageBytes = outputStream.toByteArray()
        
        return Base64.getEncoder().encodeToString(imageBytes)
    }
    
    /**
     * 绘制干扰线
     */
    private fun drawInterferenceLines(g2d: Graphics2D) {
        g2d.stroke = java.awt.BasicStroke(1.5f)
        
        for (i in 0 until 5) {
            g2d.color = getRandomColor()
            val x1 = Random.nextInt(CAPTCHA_WIDTH)
            val y1 = Random.nextInt(CAPTCHA_HEIGHT)
            val x2 = Random.nextInt(CAPTCHA_WIDTH)
            val y2 = Random.nextInt(CAPTCHA_HEIGHT)
            g2d.drawLine(x1, y1, x2, y2)
        }
    }
    
    /**
     * 绘制干扰点
     */
    private fun drawInterferencePoints(g2d: Graphics2D) {
        for (i in 0 until 80) {
            g2d.color = getRandomColor()
            val x = Random.nextInt(CAPTCHA_WIDTH)
            val y = Random.nextInt(CAPTCHA_HEIGHT)
            g2d.fillRect(x, y, 2, 2)
        }
    }
    
    /**
     * 绘制验证码文本
     */
    private fun drawCaptchaText(g2d: Graphics2D, text: String) {
        g2d.font = Font("Arial", Font.BOLD, 28)
        
        // 计算文本宽度，以便居中显示
        val fm = g2d.fontMetrics
        val totalWidth = fm.stringWidth(text)
        val startX = (CAPTCHA_WIDTH - totalWidth) / 2
        
        // 逐个字符绘制，并应用随机旋转和颜色
        for (i in text.indices) {
            val char = text[i].toString()
            val charWidth = fm.stringWidth(char)
            
            // 随机旋转角度（-15到15度）
            val theta = Math.toRadians(Random.nextDouble(-15.0, 15.0))
            
            // 保存当前变换
            val oldTransform = g2d.transform
            
            // 应用旋转变换
            g2d.translate(startX + i * (totalWidth / text.length) + charWidth / 2, CAPTCHA_HEIGHT / 2 + 8)
            g2d.rotate(theta)
            g2d.translate(-(startX + i * (totalWidth / text.length) + charWidth / 2), -(CAPTCHA_HEIGHT / 2 + 8))
            
            // 设置随机颜色并绘制字符
            g2d.color = getRandomDarkColor()
            g2d.drawString(char, startX + i * (totalWidth / text.length), CAPTCHA_HEIGHT / 2 + 8)
            
            // 恢复变换
            g2d.transform = oldTransform
        }
    }
    
    /**
     * 获取随机颜色
     */
    private fun getRandomColor(): Color {
        return Color(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
    }
    
    /**
     * 获取随机深色（确保文字可见）
     */
    private fun getRandomDarkColor(): Color {
        return Color(
            Random.nextInt(100),
            Random.nextInt(100),
            Random.nextInt(100)
        )
    }
    
    /**
     * 清理过期的验证码
     */
    private fun cleanExpiredCaptchas() {
        val currentTime = System.currentTimeMillis()
        val iterator = captchaMap.entries.iterator()
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.expireTime < currentTime) {
                iterator.remove()
            }
        }
    }
    
    /**
     * 验证码信息类
     */
    data class CaptchaInfo(
        val text: String,
        val expireTime: Long
    )
    
    /**
     * 验证码结果类
     */
    data class CaptchaResult(
        val imageBase64: String,
        val sessionId: String
    )
}
