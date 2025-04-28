# Redis 配置更新说明

## 更新内容

在 Spring Boot 3.4 版本中，Redis 连接池配置方式发生了变化。我们已经更新了 `application.yml` 文件中的 Redis 配置，以符合最新的推荐做法。

### 旧配置（已废弃）

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
    host: localhost
    timeout: 2000
    port: 6379
```

### 新配置（推荐）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000
      client-type: lettuce
      connect-timeout: 5000
      # Lettuce specific configuration
      lettuce:
        shutdown-timeout: 100ms
```

## 主要变更

1. Redis 配置已从 `spring.redis.*` 移至 `spring.data.redis.*`
2. 连接池配置已被移除，因为 Lettuce 现在默认使用内置连接池
   - Lettuce 默认使用内置连接池（最小 8 个连接，最大 64 个连接）
   - 如果需要自定义连接池，请参考最新的 Spring Boot 文档

## 参考文档

- [Spring Data Redis 文档](https://docs.spring.io/spring-data/redis/reference/redis/drivers.html)
- [Spring Boot 应用属性](https://docs.spring.io/spring-boot/appendix/application-properties/index.html)

## 注意事项

如果应用需要特定的连接池配置，请参考最新的 Spring Boot 和 Spring Data Redis 文档，了解如何在 Java 配置类中自定义 LettuceConnectionFactory。
