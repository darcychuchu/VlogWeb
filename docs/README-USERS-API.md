# 用户API文档

本文档详细描述了视频网站的用户相关API接口。API基础URL为：`http://127.0.0.1:8083/api/json/v1/users`

## 目录

1. [用户名验证](#1-用户名验证)
2. [昵称验证](#2-昵称验证)
3. [用户登录](#3-用户登录)
4. [用户注册](#4-用户注册)
5. [更新用户信息](#5-更新用户信息)
6. [数据模型](#数据模型)

## 1. 用户名验证

检查用户名是否已存在。

**请求方式**：GET

**URL**：`/stated-name`

**查询参数**：
- `username`：要检查的用户名

**响应数据**：`ApiResponse<Boolean>`
- 返回`true`表示用户名已存在
- 返回`false`表示用户名不存在（可用）

**示例**：
```
GET /api/json/v1/users/stated-name?username=testuser
```

**响应示例**：
```json
{
  "data": true  // 用户名已存在
}
```

## 2. 昵称验证

检查昵称是否已存在。

**请求方式**：GET

**URL**：`/stated-nickname`

**查询参数**：
- `nickname`：要检查的昵称

**响应数据**：`ApiResponse<Boolean>`
- 返回`true`表示昵称已存在
- 返回`false`表示昵称不存在（可用）

**示例**：
```
GET /api/json/v1/users/stated-nickname?nickname=testnickname
```

**响应示例**：
```json
{
  "data": false  // 昵称不存在，可用
}
```

## 3. 用户登录

用户登录接口。

**请求方式**：POST

**URL**：`/login`

**表单参数**：
- `username`：用户名
- `password`：密码
- `android_id`（可选）：安卓设备ID

**响应数据**：`ApiResponse<UsersDto>`

**示例**：
```
POST /api/json/v1/users/login
Content-Type: application/x-www-form-urlencoded

username=testuser&password=123456
```

**响应示例**：
```json
{
  "code": "0",
  "message": "登录成功",
  "data": {
    "id": "user-uuid",
    "name": "testuser",
    "nickName": "测试用户",
    "description": "这是一个测试用户",
    "avatar": "http://example.com/avatar.jpg",
    "accessToken": "access-token-value",
    "isLocked": 0
  }
}
```

## 4. 用户注册

用户注册接口。

**请求方式**：POST

**URL**：`/register`

**表单参数**：
- `username`：用户名（只能包含字母和数字）
- `password`：密码（至少6位）
- `nickname`：昵称
- `description`（可选）：个人简介

**响应数据**：`ApiResponse<UsersDto>`

**示例**：
```
POST /api/json/v1/users/register
Content-Type: application/x-www-form-urlencoded

username=newuser&password=123456&nickname=新用户&description=这是一个新用户
```

**响应示例**：
```json
{
  "code": "0",
  "message": "注册成功",
  "data": {
    "id": "new-user-uuid",
    "name": "newuser",
    "nickName": "新用户",
    "description": "这是一个新用户",
    "avatar": null,
    "accessToken": "access-token-value",
    "isLocked": 0
  }
}
```

## 5. 更新用户信息

更新用户头像和昵称。注意：昵称只能更改一次。

**请求方式**：POST

**URL**：`/updated/{name}/{token}`

**URL参数**：
- `name`：用户名
- `token`：访问令牌

**表单参数**：
- `nickname`（可选）：新昵称
- `avatar_file`（可选）：新头像文件（multipart/form-data）

**响应数据**：`ApiResponse<UsersDto>`

**示例**：
```
POST /api/json/v1/users/updated/testuser/access-token-value
Content-Type: multipart/form-data

nickname=新昵称
[avatar_file二进制数据]
```

**响应示例**：
```json
{
  "code": "0",
  "message": "更新成功",
  "data": {
    "id": "user-uuid",
    "name": "testuser",
    "nickName": "新昵称",
    "description": "这是一个测试用户",
    "avatar": "http://example.com/new-avatar.jpg",
    "accessToken": "access-token-value",
    "isLocked": 1  // 昵称已锁定，不能再次修改
  }
}
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

### UsersDto

用户数据模型。

```kotlin
data class UsersDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var name: String? = null,
    var nickName: String? = null,
    var description: String? = null,
    var avatar: String? = null,
    var accessToken: String? = null,
    var isLocked: Int? = null  // 0表示未锁定（可以修改昵称），1表示已锁定（不能修改昵称）
)
```
