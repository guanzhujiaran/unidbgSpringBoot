# unidbgSpringBoot 项目

## 项目简介
基于 Spring Boot 和 unidbg 的项目，用于模拟和分析 Android 应用的 Native 代码执行。

## 环境要求
- JDK 21
- Maven 3.6.0+
- Docker（用于容器化部署）

## 快速开始
1. 克隆项目：
   ```bash
   git clone https://github.com/your-repo/unidbgSpringBoot.git
   ```
2. 构建项目：
   ```bash
   mvn clean package
   ```
3. 运行项目：
   ```bash
   java -jar target/unidbgSpringBoot-0.0.1.jar
   ```

## 部署到 Docker
### 1. 修改端口号（可选）
- 修改 `application.properties` 中的端口号：
  ```properties
  server.port=23335
  ```
- 确保 `docker-compose.yml` 中的端口映射一致：
  ```yaml
  ports:
    - "23335:23335"
  ```

### 2. 构建镜像
使用 Spring Boot Maven 插件构建 Docker 镜像：
```bash
mvn spring-boot:build-image
```

### 3. 运行容器
直接运行：
```bash
docker run -p 23335:23335 unidbg-springboot-samsclub
```
或使用 `docker-compose`：
```bash
docker-compose up
```

## 注意事项
- 确保 Docker 已安装并运行。
- 如需修改端口号，请同步更新 `application.properties` 和 `docker-compose.yml`。