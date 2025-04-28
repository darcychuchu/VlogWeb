# Kotlin + Gradle 实现前后端分离架构

本文档提供使用Kotlin语言和Gradle构建工具实现前后端分离架构的详细示例。

## Gradle 构建配置

### build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot 核心依赖
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Kotlin 支持
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 缓存支持
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // 可选：Redis 缓存
    // implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // 日志支持
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // 测试依赖
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### settings.gradle.kts

```kotlin
rootProject.name = "video-web-app"
```

## 应用配置

### application.yml

```yaml
# API服务器配置
api:
  base-url: http://your-api-server/api/json/v1
  health-check-interval: 60000  # 毫秒

# 缓存配置
spring:
  cache:
    type: caffeine
    cache-names: videoDetails,actors,genres
    caffeine:
      spec: expireAfterWrite=60m,maximumSize=1000

# 日志配置
logging:
  level:
    org.springframework.web.client.RestTemplate: DEBUG
    com.example.videowebapp: INFO
```

## 主应用类

```kotlin
package com.example.videowebapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class VideoWebAppApplication

fun main(args: Array<String>) {
    runApplication<VideoWebAppApplication>(*args)
}
```

## 数据模型 (DTOs)

### ApiResponse.kt

```kotlin
package com.example.videowebapp.dto

data class ApiResponse<T>(
    val code: String = "",
    val message: String = "",
    val data: T? = null
)
```

### VideoDetailDTO.kt

```kotlin
package com.example.videowebapp.dto

data class VideoDetailDTO(
    val id: String = "",
    val title: String = "",
    val alias: String = "",
    val description: String = "",
    val director: String = "",
    val actors: String = "",
    val tags: String = "",
    val score: String = "",
    val orderSort: String = "",
    val publishedAt: String = "",
    val attachmentId: String = "",
    val favoriteState: Boolean = false
)
```

### ActorDTO.kt

```kotlin
package com.example.videowebapp.dto

data class ActorDTO(
    val id: String = "",
    val title: String = ""
)
```

### GenreDTO.kt

```kotlin
package com.example.videowebapp.dto

data class GenreDTO(
    val id: String = "",
    val title: String = ""
)
```

## 配置类

### RestTemplateConfig.kt

```kotlin
package com.example.videowebapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        // 创建RestTemplate实例
        val restTemplate = RestTemplate()

        // 设置连接超时和读取超时
        val factory = HttpComponentsClientHttpRequestFactory().apply {
            setConnectTimeout(5000) // 连接超时5秒
            setReadTimeout(10000)   // 读取超时10秒
        }
        restTemplate.requestFactory = factory

        // 添加消息转换器，支持JSON
        val converter = MappingJackson2HttpMessageConverter().apply {
            supportedMediaTypes = listOf(MediaType.APPLICATION_JSON)
        }
        restTemplate.messageConverters.add(converter)

        return restTemplate
    }
}
```

### CacheConfig.kt

```kotlin
package com.example.videowebapp.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return CaffeineCacheManager().apply {
            // 配置默认的缓存规则
            setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)  // 写入后60分钟过期
                .maximumSize(1000)                       // 最多缓存1000条数据
            )

            // 添加缓存名称
            setCacheNames(listOf("videoDetails", "actors", "genres"))
        }
    }
}
```

## 服务类

### ApiService.kt

```kotlin
package com.example.videowebapp.service

import com.example.videowebapp.dto.ActorDTO
import com.example.videowebapp.dto.ApiResponse
import com.example.videowebapp.dto.GenreDTO
import com.example.videowebapp.dto.VideoDetailDTO
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

private val logger = KotlinLogging.logger {}

@Service
class ApiService(
    private val restTemplate: RestTemplate,
    @Value("\${api.base-url}") private val apiBaseUrl: String
) {

    @Cacheable("videoDetails")
    fun getVideoDetail(id: String): VideoDetailDTO {
        try {
            logger.info { "Fetching video details for ID: $id" }

            val responseType = object : ParameterizedTypeReference<ApiResponse<VideoDetailDTO>>() {}
            val response = restTemplate.exchange(
                "$apiBaseUrl/videos/$id",
                HttpMethod.GET,
                null,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null && apiResponse.code == "0") {
                return apiResponse.data ?: throw RuntimeException("Video details data is null")
            } else {
                logger.error { "API returned error: ${apiResponse?.message ?: "Unknown error"}" }
                throw RuntimeException("Failed to get video details")
            }
        } catch (e: RestClientException) {
            logger.error(e) { "Error fetching video details" }
            throw e
        }
    }

    @Cacheable("actors")
    fun getActors(videoId: String): List<ActorDTO> {
        try {
            logger.info { "Fetching actors for video ID: $videoId" }

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<ActorDTO>>>() {}
            val response = restTemplate.exchange(
                "$apiBaseUrl/videos/actors/$videoId",
                HttpMethod.GET,
                null,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null && apiResponse.code == "0") {
                return apiResponse.data ?: emptyList()
            } else {
                logger.error { "API returned error: ${apiResponse?.message ?: "Unknown error"}" }
                throw RuntimeException("Failed to get actors")
            }
        } catch (e: RestClientException) {
            logger.error(e) { "Error fetching actors" }
            throw e
        }
    }

    @Cacheable("genres")
    fun getGenres(videoId: String): List<GenreDTO> {
        try {
            logger.info { "Fetching genres for video ID: $videoId" }

            val responseType = object : ParameterizedTypeReference<ApiResponse<List<GenreDTO>>>() {}
            val response = restTemplate.exchange(
                "$apiBaseUrl/videos/genres/$videoId",
                HttpMethod.GET,
                null,
                responseType
            )

            val apiResponse = response.body
            if (apiResponse != null && apiResponse.code == "0") {
                return apiResponse.data ?: emptyList()
            } else {
                logger.error { "API returned error: ${apiResponse?.message ?: "Unknown error"}" }
                throw RuntimeException("Failed to get genres")
            }
        } catch (e: RestClientException) {
            logger.error(e) { "Error fetching genres" }
            throw e
        }
    }
}
```

## 健康检查

### ApiHealthChecker.kt

```kotlin
package com.example.videowebapp.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

private val logger = KotlinLogging.logger {}

@Component
class ApiHealthChecker(
    private val restTemplate: RestTemplate,
    @Value("\${api.base-url}") apiBaseUrl: String
) {

    private val apiHealthUrl = "$apiBaseUrl/health"

    @Volatile
    private var apiHealthy = true

    @Scheduled(fixedRateString = "\${api.health-check-interval:60000}")
    fun checkApiHealth() {
        try {
            val response = restTemplate.getForEntity(apiHealthUrl, String::class.java)

            if (response.statusCode.is2xxSuccessful) {
                if (!apiHealthy) {
                    logger.info { "API服务器恢复正常" }
                }
                apiHealthy = true
            } else {
                logger.warn { "API服务器返回非成功状态码: ${response.statusCode.value()}" }
                apiHealthy = false
            }
        } catch (e: Exception) {
            logger.error(e) { "API服务器健康检查失败" }
            apiHealthy = false
            // 可以在这里添加警报通知逻辑
        }
    }

    fun isApiHealthy(): Boolean = apiHealthy
}
```

## 控制器

### VideoController.kt

```kotlin
package com.example.videowebapp.controller

import com.example.videowebapp.service.ApiHealthChecker
import com.example.videowebapp.service.ApiService
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

private val logger = KotlinLogging.logger {}

@Controller
class VideoController(
    private val apiService: ApiService,
    private val apiHealthChecker: ApiHealthChecker
) {

    @GetMapping("/videos/detail/{id}")
    fun videoDetail(@PathVariable id: String, model: Model): String {
        return try {
            // 检查API健康状态
            if (!apiHealthChecker.isApiHealthy()) {
                logger.warn { "API服务器不健康，可能会影响页面加载" }
                model.addAttribute("apiWarning", "API服务器可能不可用，某些数据可能无法加载")
            }

            // 从API获取视频详情
            val videoDetail = apiService.getVideoDetail(id)
            model.addAttribute("VideoItem", videoDetail)

            // 从API获取演员信息
            val actors = apiService.getActors(id)
            model.addAttribute("Actors", actors)

            // 从API获取类型信息
            val genres = apiService.getGenres(id)
            model.addAttribute("Genres", genres)

            // 添加其他模型属性
            model.addAttribute("AuthorState", mapOf(
                "creatorStatus" to false,
                "loginStatus" to true
            ))
            model.addAttribute("FavoriteState", videoDetail.favoriteState)

            "detail"
        } catch (e: Exception) {
            logger.error(e) { "Error rendering video detail page" }
            model.addAttribute("errorMessage", "无法获取视频详情")
            model.addAttribute("errorDetails", e.message)
            "error"
        }
    }
}
```

## 异常处理

### GlobalExceptionHandler.kt

```kotlin
package com.example.videowebapp.controller

import mu.KotlinLogging
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RestClientException::class)
    fun handleApiError(e: RestClientException, model: Model): String {
        logger.error(e) { "API communication error" }
        model.addAttribute("errorMessage", "无法连接到API服务器，请稍后再试")
        model.addAttribute("errorDetails", e.message)
        return "error"
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericError(e: Exception, model: Model): String {
        logger.error(e) { "Unexpected error" }
        model.addAttribute("errorMessage", "发生了意外错误，请稍后再试")
        model.addAttribute("errorDetails", e.message)
        return "error"
    }
}
```

## Thymeleaf 模板

### detail.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="cn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${VideoItem.title} + ' - 视频详情'">视频详情</title>
    <meta name="description" th:content="${VideoItem.description}">
    <meta name="keywords" th:content="${VideoItem.tags}">
    <!-- 样式表引用 -->
    <link rel="stylesheet" href="/css/styles.css">
</head>
<body class="bg-black text-white">

<!-- API警告消息 -->
<div th:if="${apiWarning}" class="bg-yellow-600 text-white p-4 text-center">
    <p th:text="${apiWarning}">API警告</p>
</div>

<section id="main-container" class="max-w-7xl mx-auto px-4 py-8">
    <!-- 页面内容 -->

    <!-- 将API数据注入到页面中 -->
    <script th:inline="javascript">
        // 从Controller传来的动态数据
        window.videoData = {
            videoDetail: [[${VideoItem}]],
            actors: [[${Actors}]],
            genres: [[${Genres}]]
        };

        // 不常变动的静态数据直接写在这里
        window.staticData = {
            playerOptions: {
                autoplay: true,
                controls: true,
                // 其他播放器选项...
            },
            uiLabels: {
                watchNow: "立即观看",
                addToFavorites: "收藏",
                removeFromFavorites: "取消收藏",
                // 其他UI标签...
            }
        };
    </script>

    <!-- 前端JavaScript -->
    <script src="/js/video-detail.js"></script>
</section>

</body>
</html>
```

### error.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="cn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>错误</title>
    <link rel="stylesheet" href="/css/styles.css">
</head>
<body class="bg-gray-900 text-white">
    <div class="max-w-4xl mx-auto px-4 py-16">
        <div class="bg-red-900 rounded-lg p-8">
            <h1 class="text-3xl font-bold mb-4">出错了</h1>
            <p class="text-xl mb-4" th:text="${errorMessage}">发生了错误</p>
            <p class="text-gray-300 mb-8" th:if="${errorDetails}" th:text="${errorDetails}">错误详情</p>
            <a href="/" class="bg-white text-red-900 px-6 py-2 rounded-lg font-semibold hover:bg-gray-200">
                返回首页
            </a>
        </div>
    </div>
</body>
</html>
```

## 前端JavaScript

### video-detail.js

```javascript
document.addEventListener('DOMContentLoaded', function() {
    // 使用注入的数据渲染页面
    if (window.videoData) {
        renderVideoDetails(window.videoData.videoDetail);
        renderActors(window.videoData.actors);
        renderGenres(window.videoData.genres);
    }

    // 对于需要实时更新的数据，仍然使用API
    loadComments();
    loadPlayList();
});

// 渲染视频详情
function renderVideoDetails(videoDetail) {
    // 设置页面标题
    document.title = `${videoDetail.title} - 视频详情`;

    // 渲染视频详情...
    const headerContainer = document.getElementById('video-header');
    if (headerContainer) {
        headerContainer.innerHTML = `
            <div class="relative">
                <img class="w-full h-64 object-cover" src="/file/attachments/image/l/${videoDetail.attachmentId}" alt="${videoDetail.title}">
                <div class="absolute inset-0 bg-gradient-to-t from-black via-black/70 to-transparent"></div>
                <div class="absolute bottom-0 left-0 p-6">
                    <h1 class="text-4xl font-bold text-white">${videoDetail.title}</h1>
                    <p class="text-gray-300 mt-2">${videoDetail.alias}</p>
                </div>
            </div>
        `;
    }

    // 渲染其他详情...
}

// 渲染演员信息
function renderActors(actors) {
    const actorsContainer = document.getElementById('actors-container');
    if (actorsContainer && actors && actors.length > 0) {
        actorsContainer.innerHTML = `
            <h3 class="text-xl font-semibold mb-2">演员</h3>
            <div class="flex flex-wrap gap-2">
                ${actors.map(actor => `
                    <span class="bg-gray-800 px-3 py-1 rounded-full">${actor.title}</span>
                `).join('')}
            </div>
        `;
    }
}

// 渲染类型信息
function renderGenres(genres) {
    const genresContainer = document.getElementById('genres-container');
    if (genresContainer && genres && genres.length > 0) {
        genresContainer.innerHTML = `
            <h3 class="text-xl font-semibold mb-2">类型</h3>
            <div class="flex flex-wrap gap-2">
                ${genres.map(genre => `
                    <a href="/videos/genre/${genre.id}" class="bg-blue-900 px-3 py-1 rounded-full hover:bg-blue-800">${genre.title}</a>
                `).join('')}
            </div>
        `;
    }
}

// 加载评论
function loadComments() {
    if (!window.videoData || !window.videoData.videoDetail) return;

    const videoId = window.videoData.videoDetail.id;
    const commentContainer = document.getElementById('comments-container');

    if (commentContainer) {
        // 显示加载中状态
        commentContainer.innerHTML = '<p class="text-center py-4">加载评论中...</p>';

        // 从API获取评论
        fetch(`/api/json/v1/videos/comments/${videoId}/0`)
            .then(response => response.json())
            .then(data => {
                if (data && data.data && data.data.length > 0) {
                    renderComments(data.data);
                } else {
                    commentContainer.innerHTML = '<p class="text-center py-4">暂无评论</p>';
                }
            })
            .catch(error => {
                console.error('Error loading comments:', error);
                commentContainer.innerHTML = '<p class="text-center py-4 text-red-500">加载评论失败</p>';
            });
    }
}

// 渲染评论
function renderComments(comments) {
    const commentContainer = document.getElementById('comments-container');
    if (commentContainer) {
        commentContainer.innerHTML = comments.map(comment => `
            <div class="bg-gray-800 rounded-lg p-4 mb-4">
                <div class="flex items-center mb-2">
                    <img class="w-10 h-10 rounded-full" src="/images/avatar.svg" alt="User">
                    <div class="ml-3">
                        <h4 class="font-semibold">${comment.createdBy || 'Anonymous'}</h4>
                        <p class="text-gray-400 text-sm">${new Date(parseInt(comment.createdAt)).toLocaleString()}</p>
                    </div>
                </div>
                <p class="text-gray-300">${comment.description}</p>
            </div>
        `).join('');
    }
}

// 加载播放列表
function loadPlayList() {
    // 类似于loadComments的实现...
}
```

## 项目结构

```
video-web-app/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── videowebapp/
│   │   │               ├── VideoWebAppApplication.kt
│   │   │               ├── config/
│   │   │               │   ├── CacheConfig.kt
│   │   │               │   └── RestTemplateConfig.kt
│   │   │               ├── controller/
│   │   │               │   ├── ApiProxyController.kt
│   │   │               │   ├── GlobalExceptionHandler.kt
│   │   │               │   └── VideoController.kt
│   │   │               ├── dto/
│   │   │               │   ├── ActorDTO.kt
│   │   │               │   ├── ApiResponse.kt
│   │   │               │   ├── GenreDTO.kt
│   │   │               │   └── VideoDetailDTO.kt
│   │   │               └── service/
│   │   │                   ├── ApiHealthChecker.kt
│   │   │                   └── ApiService.kt
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── styles.css
│   │   │   │   ├── js/
│   │   │   │   │   └── video-detail.js
│   │   │   │   └── images/
│   │   │   │       └── avatar.svg
│   │   │   └── templates/
│   │   │       ├── detail.html
│   │   │       └── error.html
│   │   └── kotlin/
│   │       └── com/
│   │           └── example/
│   │               └── videowebapp/
│   │                   └── VideoWebAppApplicationTests.kt
```

## 运行应用

使用Gradle包装器运行应用：

```bash
./gradlew bootRun
```

或者构建并运行JAR文件：

```bash
./gradlew build
java -jar build/libs/video-web-app-0.0.1-SNAPSHOT.jar
```

## 总结

这个Kotlin + Gradle示例实现了与Java + Maven版本相同的功能，但使用了Kotlin的语言特性，如：

1. 数据类（data class）简化了DTO的定义
2. 扩展函数和属性委托简化了代码
3. 空安全特性提高了代码的健壮性
4. 函数式编程风格使代码更简洁

Kotlin与Spring Boot完美集成，可以充分利用Spring的注解和功能，同时享受Kotlin语言的简洁和安全特性。

Gradle构建系统使用Kotlin DSL提供了类型安全的构建脚本，比XML更灵活和强大。

这个示例展示了如何使用现代化的Kotlin + Gradle技术栈实现前后端分离架构，同时保留服务器端渲染的SEO优势。

## API代理实现

除了在服务器端渲染页面时获取API数据外，还可以将所有前端JavaScript的API请求也统一到Controller中处理，特别是当Web服务器和API服务器通过内网连接时，这种方法有很多优势。

### 优势

1. **网络拓扑优化**：
   - Web服务器可以通过内网高速连接访问API服务器
   - 避免了客户端浏览器直接访问API服务器的网络延迟
   - 可以隐藏API服务器，只暴露Web服务器到公网

2. **安全性提升**：
   - API服务器可以完全部署在内网，不暴露到公网
   - 所有请求通过Web服务器进行过滤和验证
   - 可以实现更严格的访问控制和认证

3. **跨域问题解决**：
   - 避免了浏览器端的跨域请求问题
   - 不需要在API服务器上配置CORS

4. **缓存优化**：
   - 可以在Web服务器上统一实现缓存策略
   - 减少对API服务器的重复请求

5. **请求合并**：
   - 可以将多个API请求合并为一个响应
   - 减少客户端的请求次数

### 前端JavaScript修改

将API请求指向代理端点：

```javascript
// 之前的直接API请求
fetch('/api/json/v1/videos/comments/123/0')
    .then(response => response.json())
    .then(data => {
        // 处理数据
    });

// 修改为通过代理请求
fetch('/api-proxy/json/v1/videos/comments/123/0')
    .then(response => response.json())
    .then(data => {
        // 处理数据
    });
```

### 性能影响分析

**性能优化方面**：
- 内网连接通常比公网连接快10-100倍
- Web服务器可以实现更高效的连接复用
- 集中式缓存可以提高缓存命中率

**可能的性能劣势**：
- Web服务器需要处理更多的请求
- 请求需要经过额外的处理层（通常延迟在毫秒级）

### 并行请求优化

Kotlin的协程支持使得并行请求非常简单，可以显著提高性能：

```kotlin
@GetMapping("/api-combined/video-details/{id}")
@ResponseBody
fun getCombinedVideoDetails(@PathVariable id: String): Map<String, Any> {
    return runBlocking {
        // 并行发起多个请求
        val videoDetailDeferred = async(Dispatchers.IO) { /* API请求 */ }
        val actorsDeferred = async(Dispatchers.IO) { /* API请求 */ }
        val genresDeferred = async(Dispatchers.IO) { /* API请求 */ }

        // 等待所有请求完成并返回结果
        mapOf(
            "videoDetail" to videoDetailDeferred.await(),
            "actors" to actorsDeferred.await(),
            "genres" to genresDeferred.await()
        )
    }
}
```
