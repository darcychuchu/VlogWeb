package org.vlog.web.util

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * 重试工具类
 * 用于在API调用失败时进行重试
 */
object RetryUtil {
    private val logger = LoggerFactory.getLogger(RetryUtil::class.java)

    /**
     * 执行带重试的操作
     * @param maxRetries 最大重试次数
     * @param retryDelayMs 重试间隔（毫秒）
     * @param operation 要执行的操作
     * @return 操作结果，如果所有重试都失败则返回null
     */
    fun <T> executeWithRetry(
        maxRetries: Int = 3,
        retryDelayMs: Long = 1000,
        operation: () -> T?
    ): T? {
        var attempts = 0
        var lastException: Exception? = null

        while (attempts < maxRetries) {
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                attempts++
                
                if (attempts < maxRetries) {
                    logger.debug("操作失败，进行第 $attempts 次重试: ${e.message}")
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryDelayMs * attempts) // 指数退避
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw ie
                    }
                }
            }
        }

        logger.debug("操作在 $maxRetries 次重试后仍然失败: ${lastException?.message}")
        return null
    }

    /**
     * 执行带重试和降级的操作
     * @param maxRetries 最大重试次数
     * @param retryDelayMs 重试间隔（毫秒）
     * @param operation 要执行的操作
     * @param fallback 降级操作，当所有重试都失败时执行
     * @return 操作结果或降级结果
     */
    fun <T> executeWithRetryAndFallback(
        maxRetries: Int = 3,
        retryDelayMs: Long = 1000,
        operation: () -> T?,
        fallback: () -> T
    ): T {
        val result = executeWithRetry(maxRetries, retryDelayMs, operation)
        return result ?: fallback()
    }
}
