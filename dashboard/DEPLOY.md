# Dashboard 部署指南

## 两种运行模式

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| **local** | 前后端本地运行 + PostgreSQL Docker | **日常开发** |
| **docker** | 前后端数据库全部 Docker | **测试/生产部署** |

---

## 日常开发（local 模式）

### 1. 启动 PostgreSQL（只需一次）

```bash
docker run -d \
  --name dashboard-db \
  -e POSTGRES_DB=dashboard \
  -e POSTGRES_USER=dashboard \
  -e POSTGRES_PASSWORD=dashboard123 \
  -p 5432:5432 \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16-alpine
```

后续开关：
```bash
docker start dashboard-db   # 启动
docker stop dashboard-db    # 停止
```

### 2. 启动后端

```bash
cd dashboard
./mvnw spring-boot:run
```

默认使用 `local` profile，连接 localhost:5432 的 PostgreSQL。

### 3. 启动前端

```bash
cd dashboard-web
pnpm dev
```

访问：http://localhost:3000

---

## 生产部署（docker 模式）

### 方式一：Docker Compose（单服务器全栈）

```bash
# 1. 上传代码到服务器
scp -r dashboard dashboard-web user@server:/opt/

# 2. 服务器上执行
cd /opt/dashboard
docker-compose up -d

# 3. 查看状态
docker-compose ps
docker-compose logs -f
```

访问：http://服务器IP

### 方式二：已有外部数据库

如果服务器已有 PostgreSQL，只部署后端：

```bash
cd dashboard

# 构建镜像
docker build -t dashboard-app .

# 运行（连接外部数据库，使用 prod profile）
docker run -d \
  --name dashboard-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://你的数据库IP:5432/dashboard \
  -e SPRING_DATASOURCE_USERNAME=dashboard \
  -e SPRING_DATASOURCE_PASSWORD=你的密码 \
  dashboard-app
```

---

## 服务架构

```
日常开发 (local)
├─ 前端: pnpm dev (localhost:3000)
├─ 后端: ./mvnw spring-boot:run (localhost:8080)
└─ 数据库: Docker PostgreSQL (localhost:5432)

生产部署 (docker)
├─ Nginx (80) ← 入口
│   ├─ /api/* → 后端
│   └─ /* → 前端静态资源
├─ 后端 (8080)
└─ PostgreSQL (5432)
```

---

## 常用命令

```bash
# 查看容器
docker ps

# 查看日志
docker logs dashboard-db
docker logs dashboard-app
docker logs dashboard-web

# 进入数据库
docker exec -it dashboard-db psql -U dashboard

# Docker Compose
docker-compose up -d          # 启动
docker-compose down           # 停止
docker-compose down -v        # 停止并清空数据
docker-compose logs -f        # 实时日志
docker-compose build          # 重新构建
docker-compose up -d --build  # 重新构建并启动
```

---

## 数据备份

```bash
# 备份
docker exec dashboard-db pg_dump -U dashboard dashboard > backup.sql

# 恢复
docker exec -i dashboard-db psql -U dashboard dashboard < backup.sql
```
