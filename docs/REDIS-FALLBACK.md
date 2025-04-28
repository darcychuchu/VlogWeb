# Redis 故障处理方案

## 问题描述

应用程序在启动时尝试连接 Redis 服务器，但如果 Redis 服务器不可用，应用程序会抛出以下异常：

```
org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
```

这会导致应用程序无法正常运行，即使 Redis 只是用于缓存功能。

## 解决方案

我们实现了一个故障转移机制，当 Redis 不可用时，应用程序会自动切换到内存缓存，确保应用程序可以继续运行。

### 主要修改

1. **CacheConfig 类**
   - 添加了故障转移机制，当 Redis 连接失败时，自动切换到内存缓存
   - 使用 `ConcurrentMapCacheManager` 作为备选缓存管理器
   - 添加了异常处理和日志记录

2. **application.yml**
   - 添加了 `spring.main.allow-bean-definition-overriding: true` 配置，允许 Bean 定义覆盖
   - 保留了原有的 Redis 配置，以便在 Redis 可用时使用

3. **ApiService 类**
   - 添加了 `RedisConnectionFailureException` 异常处理
   - 当 Redis 连接失败时，继续尝试获取数据，但不使用 Redis 缓存
   - 改进了错误处理，确保在 Redis 或 API 调用失败时返回合理的默认值

## 使用方法

无需特殊配置，应用程序会自动检测 Redis 是否可用：

1. 如果 Redis 可用，应用程序会使用 Redis 进行缓存
2. 如果 Redis 不可用，应用程序会自动切换到内存缓存
3. 日志中会记录相关信息，方便排查问题

## 注意事项

1. 内存缓存不会在应用程序重启后保留，而 Redis 缓存会持久化
2. 内存缓存可能会占用更多的应用程序内存
3. 在生产环境中，建议确保 Redis 服务器的可用性，以获得更好的性能和可靠性

## 后续改进

1. 添加 Redis 连接健康检查，定期尝试重新连接 Redis
2. 添加缓存统计信息，监控缓存命中率和使用情况
3. 考虑使用更复杂的缓存策略，如多级缓存或分布式缓存
