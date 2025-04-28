# 前后端分离架构 + 服务器端渲染实现指南

## 架构概述

这个架构将系统分为两个独立部分：

1. **API服务器**：包含数据库和业务逻辑，提供JSON API
2. **Web服务器**：不连接数据库，只负责渲染页面
   - 从API服务器获取数据
   - 使用Thymeleaf渲染HTML
   - 将不常变动的数据直接嵌入HTML
   - 只保留经常变动的数据与API交互

这种架构的优势：
- Web服务器不需要数据库，可以轻松部署和扩展
- 保留了服务器端渲染的SEO优势
- 关键数据直接嵌入HTML源码，对搜索引擎友好
- 前后端职责清晰分离

## 实现步骤

### 1. API服务器

API服务器提供各种JSON端点，例如：

- `/api/json/v1/videos/{id}` - 获取视频详情
- `/api/json/v1/videos/actors/{id}` - 获取视频演员
- `/api/json/v1/videos/genres/{id}` - 获取视频类型
- `/api/json/v1/videos/players/{id}` - 获取播放列表
- `/api/json/v1/videos/comments/{id}/{type}` - 获取评论

### 2. Web服务器配置

#### 添加RestTemplate配置

```java
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

#### 添加缓存配置（可选）

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

### 3. 创建API服务类

```java
@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "http://your-api-server/api/json/v1";

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("videoDetails")
    public VideoDetailDTO getVideoDetail(String id) {
        return restTemplate.getForObject(
            apiBaseUrl + "/videos/" + id,
            VideoDetailDTO.class
        );
    }

    @Cacheable("actors")
    public List<ActorDTO> getActors(String videoId) {
        ResponseEntity<ApiResponse<List<ActorDTO>>> response = restTemplate.exchange(
            apiBaseUrl + "/videos/actors/" + videoId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<List<ActorDTO>>>() {}
        );
        return response.getBody().getData();
    }

    @Cacheable("genres")
    public List<GenreDTO> getGenres(String videoId) {
        ResponseEntity<ApiResponse<List<GenreDTO>>> response = restTemplate.exchange(
            apiBaseUrl + "/videos/genres/" + videoId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<List<GenreDTO>>>() {}
        );
        return response.getBody().getData();
    }

    // 其他API方法...
}
```

### 4. 实现Controller

```java
@Controller
public class VideoController {

    private final ApiService apiService;

    public VideoController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/videos/detail/{id}")
    public String videoDetail(@PathVariable String id, Model model) {
        try {
            // 从API获取视频详情
            VideoDetailDTO videoDetail = apiService.getVideoDetail(id);

            // 从API获取演员信息
            List<ActorDTO> actors = apiService.getActors(id);

            // 从API获取类型信息
            List<GenreDTO> genres = apiService.getGenres(id);

            // 将数据添加到模型中
            model.addAttribute("VideoItem", videoDetail);
            model.addAttribute("Actors", actors);
            model.addAttribute("Genres", genres);

            // 返回视图名称
            return "detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "无法获取视频详情: " + e.getMessage());
            return "error";
        }
    }
}
```

### 5. 修改Thymeleaf模板

在`detail.html`中，将API数据注入到页面中：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="cn">
<head>
    <!-- 头部内容 -->
</head>
<body>
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
            // 静态数据...
        };
    </script>

    <!-- 前端JavaScript -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // 使用注入的数据渲染页面
            renderVideoDetails(window.videoData.videoDetail);
            renderActors(window.videoData.actors);
            renderGenres(window.videoData.genres);

            // 对于需要实时更新的数据，仍然使用API
            loadComments();
            loadPlayList();
        });

        // 其他函数...
    </script>
</body>
</html>
```

### 6. 示例：将固定数据嵌入HTML

```html
<script th:inline="javascript">
    // 视频详情固定数据示例
    window.videoStaticData = {
        videoDetail: {
            id: [[${VideoItem.id}]], // 使用动态ID
            title: '流浪地球2',
            alias: 'The Wandering Earth 2',
            description: '在不远的将来，太阳即将毁灭，人类在地球表面建造了巨大的推进器，希望将地球推离太阳系，寻找新家园。然而，在这个过程中，人类面临着各种挑战和困难。刘培强和他的团队必须克服重重困难，拯救人类的未来。',
            director: '郭帆',
            actors: '吴京, 刘德华, 李雪健, 沙溢, 宁理',
            tags: '科幻, 冒险, 灾难',
            score: '9.2',
            orderSort: '12345',
            publishedAt: '2023-01-22',
            attachmentId: 'cover123',
            favoriteState: false
        },
        actors: [
            { id: '1', title: '吴京' },
            { id: '2', title: '刘德华' },
            { id: '3', title: '李雪健' },
            { id: '4', title: '沙溢' },
            { id: '5', title: '宁理' }
        ],
        genres: [
            { id: '1', title: '科幻' },
            { id: '2', title: '冒险' },
            { id: '3', title: '灾难' }
        ]
    };
</script>
```

### 7. 前端JavaScript使用注入的数据

```javascript
document.addEventListener('DOMContentLoaded', function() {
    // 如果有静态数据，优先使用静态数据
    if (window.videoStaticData) {
        // 渲染演员数据
        if (window.videoStaticData.actors) {
            const actorsContainer = document.getElementById('actors-container');
            actorsContainer.innerHTML += window.videoStaticData.actors.map(actor =>
                `<span>${actor.title} / </span>`
            ).join('');
        }

        // 渲染类型数据
        if (window.videoStaticData.genres) {
            const genresContainer = document.getElementById('genres-container');
            genresContainer.innerHTML += window.videoStaticData.genres.map(genre =>
                `<a href="/videos/genre/${genre.id}"><span>${genre.title} / </span></a>`
            ).join('');
        }
    } else {
        // 如果没有静态数据，再从API获取
        // 原有的API调用代码...
    }
});
```

## 优化建议

### 1. 缓存策略

- 在Web服务器上缓存API响应，减少API调用
- 使用Spring的`@Cacheable`注解简化缓存实现
- 为不同类型的数据设置不同的缓存过期时间

### 2. 错误处理

- 添加全局异常处理器
- 实现优雅的降级策略，当API不可用时显示缓存数据或友好错误页面

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientException.class)
    public String handleApiError(RestClientException e, Model model) {
        model.addAttribute("errorMessage", "API服务暂时不可用，请稍后再试");
        return "error";
    }
}
```

### 3. 健康检查

- 定期检查API服务器的健康状态
- 当API服务器不可用时发送警报

```java
@Component
public class ApiHealthChecker {

    private final RestTemplate restTemplate;
    private final String apiHealthUrl = "http://your-api-server/health";
    private final Logger log = LoggerFactory.getLogger(ApiHealthChecker.class);

    // 构造函数...

    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkApiHealth() {
        try {
            restTemplate.getForObject(apiHealthUrl, String.class);
            log.info("API服务器正常运行");
        } catch (Exception e) {
            log.error("API服务器不可用", e);
            // 可以发送警报通知
        }
    }
}
```

### 4. 性能优化

- 使用压缩减少传输大小
- 考虑使用HTTP/2提高性能
- 实现条件请求（使用ETag或Last-Modified）

## 部署考虑

### 1. Web服务器部署

- 可以部署多个Web服务器实例，通过负载均衡分发流量
- 由于不需要数据库连接，部署更加简单
- 可以使用Docker容器化部署

### 2. API服务器部署

- 单独部署API服务器，可以根据需要进行扩展
- 考虑使用API网关管理API请求

### 3. CDN集成

- 静态资源（JS、CSS、图片）可以通过CDN分发
- 考虑对某些页面进行CDN缓存

## 选集对话框实现

对于选集列表，使用对话框/模态窗口显示，避免长页面滚动：

```html
<!-- 选集按钮 -->
<div class="text-center mb-4">
    <button id="show-episodes-btn" class="bg-gray-700 text-white px-6 py-2 rounded-lg hover:bg-gray-600">
        <span>查看全部选集</span>
    </button>
</div>

<!-- 当前选集信息 -->
<div id="current-episode-info" class="text-center mb-4 text-gray-300">
    <span>当前播放: </span><span id="current-episode-title">未选择</span>
</div>

<!-- 选集列表对话框 -->
<div id="episodes-dialog" class="fixed inset-0 bg-black bg-opacity-75 z-50 flex items-center justify-center hidden">
    <div class="bg-gray-800 rounded-lg p-6 max-w-4xl w-full max-h-[80vh] overflow-hidden flex flex-col">
        <div class="flex justify-between items-center mb-4">
            <h3 class="text-xl font-bold">选集列表</h3>
            <button id="close-dialog-btn" class="text-gray-400 hover:text-white">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <!-- 对话框内的选项卡导航 -->
        <div id="dialog-gather-tabs" class="flex overflow-x-auto border-b border-gray-700 mb-4">
            <!-- 动态生成选项卡 -->
        </div>

        <!-- 对话框内的选集列表 -->
        <div class="overflow-y-auto flex-grow">
            <div id="gather-container" class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-4 p-2">
                <!-- 动态加载 -->
            </div>
        </div>
    </div>
</div>
```

## 将所有API请求统一到Controller

除了在服务器端渲染页面时获取API数据外，还可以将所有前端JavaScript的API请求也统一到Controller中处理，特别是当Web服务器和API服务器通过内网连接时，这种方法有很多优势。

### 实现API代理控制器

```java
@Controller
public class ApiProxyController {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public ApiProxyController(
        RestTemplate restTemplate,
        @Value("${api.base-url}") String apiBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    // 代理所有API GET请求
    @GetMapping("/api-proxy/**")
    @ResponseBody
    public ResponseEntity<Object> proxyApiGet(HttpServletRequest request) {
        String apiPath = request.getRequestURI().replace("/api-proxy", "");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        return restTemplate.exchange(
            apiBaseUrl + apiPath + queryString,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Object>() {}
        );
    }

    // 代理所有API POST请求
    @PostMapping("/api-proxy/**")
    @ResponseBody
    public ResponseEntity<Object> proxyApiPost(
        HttpServletRequest request,
        @RequestBody(required = false) Object body
    ) {
        String apiPath = request.getRequestURI().replace("/api-proxy", "");

        HttpEntity<Object> httpEntity = body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY;

        return restTemplate.exchange(
            apiBaseUrl + apiPath,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<Object>() {}
        );
    }
}
```

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

### 性能优化

1. **使用连接池**：

```java
@Bean
public RestTemplate restTemplate() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setMaxTotal(100);

    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .build();

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);

    return new RestTemplate(factory);
}
```

2. **实现智能缓存**：

```java
@CacheConfig(cacheNames = ["apiProxy"])
@Service
public class ApiProxyService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    // 构造函数...

    @Cacheable(key = "#path + #queryString")
    public ResponseEntity<Object> proxyGet(String path, String queryString) {
        return restTemplate.exchange(
            apiBaseUrl + path + queryString,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Object>() {}
        );
    }
}
```

3. **请求合并**：

```java
@GetMapping("/api-combined/video-details/{id}")
@ResponseBody
public Map<String, Object> getCombinedVideoDetails(@PathVariable String id) {
    VideoDetailDTO videoDetail = apiService.getVideoDetail(id);
    List<ActorDTO> actors = apiService.getActors(id);
    List<GenreDTO> genres = apiService.getGenres(id);

    Map<String, Object> result = new HashMap<>();
    result.put("videoDetail", videoDetail);
    result.put("actors", actors);
    result.put("genres", genres);

    return result;
}
```

### 性能影响分析

**性能优化方面**：
- 内网连接通常比公网连接快10-100倍
- Web服务器可以实现更高效的连接复用
- 集中式缓存可以提高缓存命中率

**可能的性能劣势**：
- Web服务器需要处理更多的请求
- 请求需要经过额外的处理层（通常延迟在毫秒级）

## 结论

这种架构将前后端分离，同时保留了服务器端渲染的SEO优势。Web服务器不需要连接数据库，可以轻松部署和扩展。通过将不常变动的数据直接嵌入HTML，减少了API调用，提高了性能和SEO友好性。

将所有API请求统一到Controller中处理是一个很好的优化，特别是在Web服务器和API服务器通过内网连接的情况下。这种方法可以提高安全性、简化网络拓扑、解决跨域问题，并且在大多数情况下会提高性能。

对于需要实时更新的数据，仍然通过API获取，保持了动态交互的能力。这种方式结合了前后端分离和服务器端渲染的优点，是一种很好的架构选择。
