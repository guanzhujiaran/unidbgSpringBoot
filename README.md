# unidbgSpringBoot

B 站 Android 端爬虫的**签名计算服务**。基于开源项目 [unidbg](https://github.com/zhkl0228/unidbg) 改写，在 Spring Boot 中加载 B 站 App 的 `libbili.so` 原生库，用 unidbg 模拟执行其中的签名算法，计算出 B 站请求所需的签名参数（如 `w_*` / `wbi` 等），供 `be-bilibili-crawler` 附上合法签名。

## 功能

- 加载 `libbili.so`，模拟执行 B 站签名算法
- 提供 HTTP 接口计算签名参数
- 仅负责签名计算，不发起任何网络抓取

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot |
| 核心 | unidbg（Unicorn 指令模拟执行 Android so） |
| 构建 | Maven（含 `mvnw` 包装器） |
| 资源 | `libbili.so` / `app.apk` |

## 目录结构

```
unidbgSpringBoot/
├── pom.xml                # Maven 构建配置
├── mvnw / mvnw.cmd        # Maven 包装器
├── docker-compose.yml     # 独立编排（生产由根 docker-compose 的 unidbg 服务引用镜像）
└── src/
    ├── main/
    │   ├── java/          # Spring Boot 启动类与签名 Controller / Service
    │   ├── resources/     # 配置文件（application.properties）
    │   └── ...            # libbili.so / app.apk 等资源
    └── test/
```

## 安装与启动

### 本地（Maven）

```bash
cd unidbgSpringBoot
./mvnw clean package -DskipTests
java -jar target/*.jar
```

### Docker（推荐，生产）

镜像 `unidbg-springboot-samsclub` 由根 `docker-compose.yml` 的 `unidbg` 服务使用：

```bash
cd /home/minato/BilibiliExplosion
docker compose up -d unidbg
```

服务监听 `23335`，由 `UNIDBG_PORT` 映射；调用方（`be-bilibili-crawler`）通过 `UNIDBG_HOST=unidbg`、`UNIDBG_PORT` 访问。

## 配置

- `src/main/resources/application.properties`：端口（`server.port`，默认 `23335`）等
- `docker-compose.yml`：端口映射需与 `application.properties` 一致
- 修改端口时同步更新 `application.properties` 与 `docker-compose.yml`

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/bilibiliSign` | 提交待签名参数，返回 B 站签名结果 |

## 与其它服务的关系

```
be-bilibili-crawler ──HTTP──▶ unidbgSpringBoot (:23335)
                                  │ 加载 libbili.so 模拟执行
                                  ▼
                              签名参数（w_*/wbi）
```

`be-bilibili-crawler` 在抓取请求中附带本服务算出的签名，绕过 B 站签名校验。

## 维护说明

- B 站 App 升级导致签名算法变化时，需替换 `src/main` 下的 `libbili.so` / `app.apk` 并重新适配调用逻辑。
- 该服务**仅负责签名计算**，依赖它的爬虫后端负责构造请求与发送。
