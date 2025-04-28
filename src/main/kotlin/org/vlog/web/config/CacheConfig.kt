package org.vlog.web.config

//import org.slf4j.LoggerFactory
//import org.springframework.cache.CacheManager
//import org.springframework.cache.annotation.EnableCaching
//import org.springframework.cache.concurrent.ConcurrentMapCacheManager
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import java.time.Duration

//@Configuration
//@EnableCaching
//class CacheConfig {
//    private val logger = LoggerFactory.getLogger(CacheConfig::class.java)
//
//    // 缓存TTL配置
//    private val NEVER_EXPIRE = Duration.ofDays(3650)  // 实际上相当于永不过期（10年）
//    private val LONG_TTL = Duration.ofDays(365)       // 365天
//    private val MEDIUM_TTL = Duration.ofDays(1)       // 1天
//    private val SHORT_TTL = Duration.ofHours(1)       // 1小时
//    private val VERY_SHORT_TTL = Duration.ofMinutes(30) // 30分钟
//
//    @Bean
//    @Primary
//    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
//
//        return createFallbackCacheManager()
////        return try {
////            // 尝试创建Redis缓存管理器
////            createRedisCacheManager(connectionFactory)
////        } catch (e: Exception) {
////            // 如果Redis连接失败，使用内存缓存作为备选
////            logger.debug("Redis连接失败，将使用内存缓存作为备选: ${e.message}")
////            createFallbackCacheManager()
////        }
//    }
//
////    @Bean
////    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
////        val template = RedisTemplate<String, Any>()
////        template.connectionFactory = connectionFactory
////        template.keySerializer = StringRedisSerializer()
////        template.valueSerializer = GenericJackson2JsonRedisSerializer()
////        template.hashKeySerializer = StringRedisSerializer()
////        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
////        // 对于二进制数据（如图片），需要使用JDK序列化
////        template.afterPropertiesSet()
////        return template
////    }
////
////    private fun createRedisCacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
////        // 默认缓存配置
////        val defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
////            .entryTtl(Duration.ofMinutes(60))  // 默认过期时间60分钟
////            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
////            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
////            .disableCachingNullValues()  // 不缓存空值
////
////        // 自定义特定缓存的配置，根据数据变化频率设置不同的TTL
////        val cacheConfigurations = mapOf(
////            // 视频详情缓存365天
////            "videoDetails" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // 演员信息缓存365天
////            "actors" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // 类型信息缓存365天
////            "genres" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // 播放列表缓存30分钟
////            "players" to defaultCacheConfig.entryTtl(VERY_SHORT_TTL),
////
////            // 评论缓存30分钟
////            "comments" to defaultCacheConfig.entryTtl(VERY_SHORT_TTL),
////
////            // 视频列表缓存1小时
////            "videoList" to defaultCacheConfig.entryTtl(SHORT_TTL),
////
////            // 筛选后的视频列表缓存1小时
////            "videoListFiltered" to defaultCacheConfig.entryTtl(SHORT_TTL),
////
////            // 相似推荐缓存1天
////            "moreLiked" to defaultCacheConfig.entryTtl(MEDIUM_TTL),
////
////            // 分类信息缓存365天
////            "categories" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // 视频类型缓存365天
////            "videoTypes" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // 地址信息缓存365天
////            "addresses" to defaultCacheConfig.entryTtl(LONG_TTL),
////
////            // API代理缓存10分钟
////            "apiProxy" to defaultCacheConfig.entryTtl(Duration.ofMinutes(10))
////        )
////
////        // 测试Redis连接
////        try {
////            connectionFactory.connection.close()
////        } catch (e: Exception) {
////            // 忽略连接测试异常
////            throw RedisConnectionFailureException("Redis connection test failed", e)
////        }
////
////        return RedisCacheManager.builder(connectionFactory)
////            .cacheDefaults(defaultCacheConfig)
////            .withInitialCacheConfigurations(cacheConfigurations)
////            .build()
////    }
//
//    private fun createFallbackCacheManager(): ConcurrentMapCacheManager {
//        logger.debug("创建内存缓存管理器作为备选")
//        return ConcurrentMapCacheManager().apply {
//            // 设置所有缓存名称，确保与ApiService中的@Cacheable注解一致
//            setCacheNames(listOf(
//                "videoDetails",    // 视频详情
//                "actors",         // 演员信息
//                "genres",         // 类型信息
//                "players",        // 播放器信息
//                "comments",       // 评论信息
//                "videoList",      // 视频列表
//                "videoListFiltered", // 筛选后的视频列表
//                "moreLiked",      // 相似推荐
//                "categories",     // 分类信息
//                "videoTypes",     // 视频类型
//                "addresses",      // 地址信息
//                "apiProxy"        // API代理
//            ))
//        }
//    }
//}
