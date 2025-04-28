# 视频API文档

本文档详细描述了视频网站的API接口。API基础URL为：`http://127.0.0.1:8083/api/json/v1/videos`

## 目录

1. [视频详情](#1-视频详情)
2. [演员列表](#2-演员列表)
3. [类型列表](#3-类型列表)
4. [评论相关](#4-评论相关)
   - [评论列表](#41-评论列表)
   - [发布评论](#42-发布评论)
5. [播放相关](#5-播放相关)
   - [播放服务商和选集列表](#51-播放服务商和选集列表)
6. [视频列表](#6-视频列表)
   - [首页推荐列表](#61-首页推荐列表)
   - [筛选列表查询](#62-筛选列表查询)
   - [相似影片推荐](#63-相似影片推荐)
7. [分类和地址](#7-分类和地址)
   - [地址列表](#71-地址列表)
   - [分类列表](#72-分类列表)

## 1. 视频详情

获取视频的详细信息。

**请求方式**：GET

**URL**：`/detail/{id}`

**URL参数**：
- `id`：视频ID
- `gather`（可选）：默认值为"5eeb44fc-2fb4-4ec2-b32e-63f50bfff707"

**响应数据**：`ApiResponse<VideosDto>`

**示例**：
```
GET /api/json/v1/videos/detail/123456?gather=5eeb44fc-2fb4-4ec2-b32e-63f50bfff707
```

## 2. 演员列表

获取视频的演员列表。

**请求方式**：GET

**URL**：`/actors/{id}`

**URL参数**：
- `id`：视频ID

**响应数据**：`ApiResponse<List<TagsQuotesDto>>`

**示例**：
```
GET /api/json/v1/videos/actors/123456
```

## 3. 类型列表

获取视频的类型列表。

**请求方式**：GET

**URL**：`/genres/{id}`

**URL参数**：
- `id`：视频ID

**响应数据**：`ApiResponse<List<TagsQuotesDto>>`

**示例**：
```
GET /api/json/v1/videos/genres/123456
```

## 4. 评论相关

### 4.1 评论列表

获取视频的评论列表。

**请求方式**：GET

**URL**：`/comments/{id}/{typed}`

**URL参数**：
- `id`：视频ID
- `typed`：评论类型

**响应数据**：`ApiResponse<List<CommentsDto>>`

**示例**：
```
GET /api/json/v1/videos/comments/123456/0
```

### 4.2 发布评论

发布评论。

**请求方式**：POST

**URL**：`/comments-post/{id}/{typed}`

**URL参数**：
- `id`：视频ID
- `typed`：评论类型

**请求体**：评论内容

**示例**：
```
POST /api/json/v1/videos/comments-post/123456/0
```

## 5. 播放相关

### 5.1 播放服务商和选集列表

获取视频的播放服务商和选集列表。

**请求方式**：GET

**URL**：`/players/{id}`

**URL参数**：
- `id`：视频ID

**响应数据**：`ApiResponse<List<VideoPlayersDto>>`

**示例**：
```
GET /api/json/v1/videos/players/123456
```

## 6. 视频列表

### 6.1 首页推荐列表

获取首页推荐视频列表。

**请求方式**：GET

**URL**：`/list/{typed}/{released}/{orderBy}`

**URL参数**：
- `typed`：视频类型
- `released`：发布时间
- `orderBy`：排序方式

**查询参数**：
- `cate`（可选）：分类ID，默认为空

**响应数据**：`ApiResponse<List<VideosDto>>`

**示例**：
```
GET /api/json/v1/videos/list/0/0/3?cate=
```

### 6.2 筛选列表查询

根据条件筛选视频列表。

**请求方式**：GET

**URL**：`/list`

**查询参数**：
- `typed`（可选）：视频类型，默认为0
- `page`（可选）：页码，默认为1
- `size`（可选）：每页大小，默认为24
- `code`（可选）：地区代码，默认为0
- `year`（可选）：年份，默认为0
- `order_by`（可选）：排序方式，默认为3
- `cate`（可选）：分类ID，默认为空
- `tag`（可选）：标签ID，默认为空

**响应数据**：`ApiResponse<PaginatedResponse<VideosDto>>`

**示例**：
```
GET /api/json/v1/videos/list?typed=0&page=1&size=24&code=0&year=0&order_by=3&cate=&tag=
```

### 6.3 相似影片推荐

获取相似影片推荐列表。

**请求方式**：GET

**URL**：`/more-liked/{id}/{orderBy}`

**URL参数**：
- `id`：视频ID
- `orderBy`：排序方式

**响应数据**：`ApiResponse<List<VideosDto>>`

**示例**：
```
GET /api/json/v1/videos/more-liked/123456/2
```

## 7. 分类和地址

### 7.1 地址列表

获取地址列表，主要用于筛选的code。

**请求方式**：GET

**URL**：`/address`

**响应数据**：`ApiResponse<List<AddressDto>>`

**示例**：
```
GET /api/json/v1/videos/address
```

### 7.2 分类列表

获取分类列表。

**请求方式**：GET

**URL**：`/categories`

**查询参数**：
- `typed`（可选）：视频类型，默认为0
- `cate`（可选）：分类ID，默认为空

**响应数据**：`ApiResponse<List<CategoriesDto>>`

**示例**：
```
GET /api/json/v1/videos/categories?typed=0&cate=
```

## 数据模型

### ApiResponse<T>

API通用响应格式。

```kotlin
data class ApiResponse<T>(
    val code: String = "",
    val message: String = "",
    val data: T? = null
)
```

### VideosDto

视频详情数据模型。

```kotlin
data class VideosDto(
    var id: String? = null,
    var categoryId: String? = null,
    var attachmentId: String? = null,
    var title: String? = null,
    var score: BigDecimal? = null,
    var alias: String? = null,
    var director: String? = null,
    var actors: String? = null,
    var region: String? = null,
    var language: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var author: String? = null,
    val videoPlayList: List<VideoPlayersDto>? = null
)
```

### VideoPlayersDto

视频播放器数据模型。

```kotlin
data class VideoPlayersDto(
    var videoId: String,
    var gatherId: String,
    var gatherTitle: String,
    val playList: List<GatherPlayersDto>? = null
)
```

### GatherPlayersDto

播放集数据模型。

```kotlin
data class GatherPlayersDto(
    var title: String? = null,
    var playUrl: String? = null
)
```

### TagsQuotesDto

标签引用数据模型，用于演员和类型。

```kotlin
data class TagsQuotesDto(
    var id: String? = null,
    var title: String? = null
)
```

### CommentsDto

评论数据模型。

```kotlin
data class CommentsDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,
    var isTyped: Int? = null,
    var createdBy: String? = null,
    var attachmentId: String? = null,
    var quoteId: String? = null, // videoId || Comments id
    var parentId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var createdByItem: UsersDto? = null
)
```

### AddressDto

地址数据模型。

```kotlin
data class AddressDto(
    var code: Long? = null,
    var name: String? = null
)
```

### CategoriesDto

分类数据模型。

```kotlin
data class CategoriesDto(
    var id: String? = null,
    var title: String? = null,
    var path: String? = null,
    var description: String? = null,
    var isTyped: Int? = null,
    var createdBy: String? = null,
    var modelId: String? = null,
    var parentId: String? = null,
    var categoryList: List<CategoriesDto>? = null
)
```

### PaginatedResponse<T>

分页响应数据模型。

```kotlin
data class PaginatedResponse<T>(
    val items: List<T> = emptyList(),
    val total: String = "0",
    val page: String = "1",
    val pageSize: String = "24",
    
    // 兼容Thymeleaf模板中使用的字段
    val content: List<T> = items,
    val totalElements: Long = total.toLongOrNull() ?: 0,
    val totalPages: Int = if (pageSize.toIntOrNull() ?: 0 > 0) ((total.toLongOrNull() ?: 0) / (pageSize.toLongOrNull() ?: 1)).toInt() + 1 else 0,
    val size: Int = pageSize.toIntOrNull() ?: 0,
    val number: Int = (page.toIntOrNull() ?: 1) - 1,
    val numberOfElements: Int = items.size,
    val first: Boolean = page == "1",
    val last: Boolean = (page.toIntOrNull() ?: 1) >= totalPages,
    val empty: Boolean = items.isEmpty()
)
```
