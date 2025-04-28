# 依赖包和参数详细说明

## 导入包信息

### RestTemplate 相关

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
```

### 缓存相关

```java
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
```

### Spring 配置相关

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
```

### Controller 相关

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
```

### 服务相关

```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
```

### 日志相关

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

### 其他工具类

```java
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
```

## Maven 依赖

在`pom.xml`中添加以下依赖：

```xml
<!-- Spring Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Spring Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- 可选：使用Caffeine作为缓存实现 -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- 可选：使用Redis作为缓存实现 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 参数详细说明

### RestTemplate 配置

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        // 创建RestTemplate实例
        RestTemplate restTemplate = new RestTemplate();
        
        // 设置连接超时和读取超时
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时5秒
        factory.setReadTimeout(10000);   // 读取超时10秒
        restTemplate.setRequestFactory(factory);
        
        // 添加消息转换器，支持JSON
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        restTemplate.getMessageConverters().add(converter);
        
        return restTemplate;
    }
}
```

需要额外导入：

```java
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
```

### CacheManager 配置

#### 使用SimpleCacheManager（内存缓存）

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("videoDetails"),
            new ConcurrentMapCache("actors"),
            new ConcurrentMapCache("genres")
        ));
        return cacheManager;
    }
}
```

#### 使用Caffeine缓存（更高性能的内存缓存）

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置默认的缓存规则
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)  // 写入后60分钟过期
            .maximumSize(1000)                       // 最多缓存1000条数据
        );
        
        // 添加缓存名称
        cacheManager.setCacheNames(Arrays.asList(
            "videoDetails", "actors", "genres"
        ));
        
        return cacheManager;
    }
}
```

需要额外导入：

```java
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
```

#### 使用Redis缓存（分布式缓存）

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置序列化方式
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))  // 设置缓存过期时间为60分钟
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 创建Redis缓存管理器
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("videoDetails", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)))  // 视频详情缓存2小时
            .withCacheConfiguration("actors", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(4)))        // 演员信息缓存4小时
            .withCacheConfiguration("genres", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(12)))       // 类型信息缓存12小时
            .build();
    }
}
```

需要额外导入：

```java
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
```

## API响应类型定义

为了正确处理API响应，需要定义相应的DTO类：

### API通用响应格式

```java
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    
    // 构造函数、getter和setter
    
    public ApiResponse() {}
    
    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
}
```

### 视频详情DTO

```java
public class VideoDetailDTO {
    private String id;
    private String title;
    private String alias;
    private String description;
    private String director;
    private String actors;
    private String tags;
    private String score;
    private String orderSort;
    private String publishedAt;
    private String attachmentId;
    private boolean favoriteState;
    
    // 构造函数、getter和setter
}
```

### 演员DTO

```java
public class ActorDTO {
    private String id;
    private String title;
    
    // 构造函数、getter和setter
}
```

### 类型DTO

```java
public class GenreDTO {
    private String id;
    private String title;
    
    // 构造函数、getter和setter
}
```

## 完整的ApiService示例

```java
@Service
public class ApiService {
    
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final Logger log = LoggerFactory.getLogger(ApiService.class);
    
    public ApiService(RestTemplate restTemplate, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }
    
    @Cacheable("videoDetails")
    public VideoDetailDTO getVideoDetail(String id) {
        try {
            log.info("Fetching video details for ID: {}", id);
            ResponseEntity<ApiResponse<VideoDetailDTO>> response = restTemplate.exchange(
                apiBaseUrl + "/videos/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<VideoDetailDTO>>() {}
            );
            
            if (response.getBody() != null && "0".equals(response.getBody().getCode())) {
                return response.getBody().getData();
            } else {
                log.error("API returned error: {}", response.getBody() != null ? response.getBody().getMessage() : "Unknown error");
                throw new RuntimeException("Failed to get video details");
            }
        } catch (RestClientException e) {
            log.error("Error fetching video details", e);
            throw e;
        }
    }
    
    @Cacheable("actors")
    public List<ActorDTO> getActors(String videoId) {
        try {
            log.info("Fetching actors for video ID: {}", videoId);
            ResponseEntity<ApiResponse<List<ActorDTO>>> response = restTemplate.exchange(
                apiBaseUrl + "/videos/actors/" + videoId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<ActorDTO>>>() {}
            );
            
            if (response.getBody() != null && "0".equals(response.getBody().getCode())) {
                return response.getBody().getData();
            } else {
                log.error("API returned error: {}", response.getBody() != null ? response.getBody().getMessage() : "Unknown error");
                throw new RuntimeException("Failed to get actors");
            }
        } catch (RestClientException e) {
            log.error("Error fetching actors", e);
            throw e;
        }
    }
    
    @Cacheable("genres")
    public List<GenreDTO> getGenres(String videoId) {
        try {
            log.info("Fetching genres for video ID: {}", videoId);
            ResponseEntity<ApiResponse<List<GenreDTO>>> response = restTemplate.exchange(
                apiBaseUrl + "/videos/genres/" + videoId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<GenreDTO>>>() {}
            );
            
            if (response.getBody() != null && "0".equals(response.getBody().getCode())) {
                return response.getBody().getData();
            } else {
                log.error("API returned error: {}", response.getBody() != null ? response.getBody().getMessage() : "Unknown error");
                throw new RuntimeException("Failed to get genres");
            }
        } catch (RestClientException e) {
            log.error("Error fetching genres", e);
            throw e;
        }
    }
}
```

## 配置文件

在`application.properties`或`application.yml`中添加以下配置：

```properties
# API服务器配置
api.base-url=http://your-api-server/api/json/v1

# 缓存配置
spring.cache.type=caffeine
spring.cache.cache-names=videoDetails,actors,genres
spring.cache.caffeine.spec=expireAfterWrite=60m,maximumSize=1000

# 日志配置
logging.level.org.springframework.web.client.RestTemplate=DEBUG
```

或者使用YAML格式：

```yaml
# API服务器配置
api:
  base-url: http://your-api-server/api/json/v1

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
```

## 错误处理

完整的全局异常处理器：

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(RestClientException.class)
    public String handleApiError(RestClientException e, Model model) {
        log.error("API communication error", e);
        model.addAttribute("errorMessage", "无法连接到API服务器，请稍后再试");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception e, Model model) {
        log.error("Unexpected error", e);
        model.addAttribute("errorMessage", "发生了意外错误，请稍后再试");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }
}
```

## 健康检查完整实现

```java
@Component
@EnableScheduling
public class ApiHealthChecker {
    
    private final RestTemplate restTemplate;
    private final String apiHealthUrl;
    private final Logger log = LoggerFactory.getLogger(ApiHealthChecker.class);
    
    private boolean apiHealthy = true;
    
    public ApiHealthChecker(
            RestTemplate restTemplate,
            @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiHealthUrl = apiBaseUrl + "/health";
    }
    
    @Scheduled(fixedRateString = "${api.health-check-interval:60000}")
    public void checkApiHealth() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiHealthUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                if (!apiHealthy) {
                    log.info("API服务器恢复正常");
                }
                apiHealthy = true;
            } else {
                log.warn("API服务器返回非成功状态码: {}", response.getStatusCodeValue());
                apiHealthy = false;
            }
        } catch (Exception e) {
            log.error("API服务器健康检查失败", e);
            apiHealthy = false;
            // 可以在这里添加警报通知逻辑
        }
    }
    
    public boolean isApiHealthy() {
        return apiHealthy;
    }
}
```

## 完整的Controller示例

```java
@Controller
public class VideoController {

    private final ApiService apiService;
    private final ApiHealthChecker apiHealthChecker;
    private final Logger log = LoggerFactory.getLogger(VideoController.class);
    
    public VideoController(ApiService apiService, ApiHealthChecker apiHealthChecker) {
        this.apiService = apiService;
        this.apiHealthChecker = apiHealthChecker;
    }
    
    @GetMapping("/videos/detail/{id}")
    public String videoDetail(@PathVariable String id, Model model) {
        try {
            // 检查API健康状态
            if (!apiHealthChecker.isApiHealthy()) {
                log.warn("API服务器不健康，可能会影响页面加载");
                model.addAttribute("apiWarning", "API服务器可能不可用，某些数据可能无法加载");
            }
            
            // 从API获取视频详情
            VideoDetailDTO videoDetail = apiService.getVideoDetail(id);
            model.addAttribute("VideoItem", videoDetail);
            
            // 从API获取演员信息
            List<ActorDTO> actors = apiService.getActors(id);
            model.addAttribute("Actors", actors);
            
            // 从API获取类型信息
            List<GenreDTO> genres = apiService.getGenres(id);
            model.addAttribute("Genres", genres);
            
            // 添加其他模型属性
            model.addAttribute("AuthorState", Map.of(
                "creatorStatus", false,
                "loginStatus", true
            ));
            model.addAttribute("FavoriteState", videoDetail.isFavoriteState());
            
            return "detail";
        } catch (Exception e) {
            log.error("Error rendering video detail page", e);
            model.addAttribute("errorMessage", "无法获取视频详情");
            model.addAttribute("errorDetails", e.getMessage());
            return "error";
        }
    }
}
```

需要额外导入：

```java
import java.util.Map;
```

## 总结

以上是实现前后端分离架构+服务器端渲染所需的详细依赖包和参数说明。根据您的具体需求，可以选择适合的缓存实现（内存缓存、Caffeine或Redis）。

关键点：
1. 使用`RestTemplate`调用API
2. 使用`CacheManager`缓存API响应
3. 使用`@Cacheable`注解简化缓存实现
4. 使用`@ControllerAdvice`处理全局异常
5. 使用`@Scheduled`定期检查API健康状态

这些组件共同工作，可以实现一个高效、可靠的前后端分离架构，同时保留服务器端渲染的SEO优势。
