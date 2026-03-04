# 本地开发配置说明

## 需要本地创建的配置文件

以下配置文件由于包含敏感信息（密码、IP 地址等），已被排除在版本控制之外。你需要根据本地环境创建这些文件：

### 1. application-mq.yml (RabbitMQ 配置)
**位置:** `stock_backend/src/main/resources/application-mq.yml` 和 `stock_job/src/main/resources/application-mq.yml`

```yaml
spring:
  rabbitmq:
    host: YOUR_LOCAL_HOST # 你的 MQ IP 地址
    port: 5672
    username: YOUR_USERNAME # 你的用户名
    password: YOUR_PASSWORD # 你的密码
    virtual-host: /
```

### 2. application-xxljob.yml (XXL-Job 配置)
**位置:** `stock_job/src/main/resources/application-xxljob.yml`

```yaml
logging:
  config: classpath:logback.xml
xxl:
  job:
    accessToken: YOUR_ACCESS_TOKEN # 你的访问令牌
    admin:
      addresses: http://YOUR_HOST:8093/xxl-job-admin # XXL-Job 管理地址
    executor:
      address: ''
      appname: ming-stock-executor
      ip: ''
      logpath: ./logs
      logretentiondays: 30
      port: 9999
```

### 3. application-shading.properties (ShardingSphere 配置)
**位置:** `stock_backend/src/main/resources/application-shading.properties` 和 `stock_job/src/main/resources/application-shading.properties`

```properties
# 配置你的 ShardingSphere 分片规则
# 包含数据库连接信息等敏感配置
```

### 4. application-cache.yml (Redis 配置)
**位置:** `stock_backend/src/main/resources/application-cache.yml`

```yaml
spring:
  redis:
    host: YOUR_REDIS_HOST # 你的 Redis 主机地址
    port: 6379
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 1
    timeout: PT10S
```

## 其他说明

### IDE 配置
项目使用 IntelliJ IDEA 开发，`.idea/` 目录已被添加到 `.gitignore`。每个开发者应该使用自己的 IDE 配置。

### 日志文件
`logs/` 目录已被忽略，运行日志会保存在本地。

### Docker 部署
如果使用 Docker 部署，请参考 `docker/` 目录下的配置文件。

## 快速开始

1. 克隆项目
2. 根据上述模板创建本地配置文件
3. 修改配置为你本地环境的值
4. 运行项目
