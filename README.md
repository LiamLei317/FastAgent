# Fast Agent

基于 Spring Boot + LangChain4j 的智能对话系统

## 快速开始

### 1. 环境准备

- Java 17+
- Maven 3.6+

### 2. 配置环境变量

1. 复制环境变量模板文件：
   ```bash
   cp .env.example .env
   ```

2. 编辑 `.env` 文件，填入你的智谱AI API密钥：
   ```env
   ZHIPU_API_KEY=your_zhipu_api_key_here
   ```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 访问接口

- 项目地址：http://localhost:8080
- 接口文档：http://localhost:8080/doc.html

## API 接口

### 创建会话
```bash
curl -X POST "http://localhost:8080/api/chat/session/create?userId=123"
```

### 普通对话
```bash
curl -X POST "http://localhost:8080/api/chat/send" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好",
    "userId": "123"
  }'
```

### 流式对话（SSE）
```bash
curl -X POST "http://localhost:8080/api/chat/stream" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "message": "你好",
    "userId": "123"
  }'
```

## 环境变量配置

项目使用环境变量管理敏感配置，确保安全性：

| 变量名 | 说明 | 必填 | 示例 |
|--------|------|------|------|
| ZHIPU_API_KEY | 智谱AI API密钥 | 是 | 4be64cd116b745caa9a33a9c0792f225.xvRXoLi3aOU2VOth |

## 安全说明

- `.env` 文件包含敏感信息，已加入 `.gitignore` 不会被提交到版本控制
- 生产环境建议通过系统环境变量或配置中心管理密钥
- `.env.example` 文件可作为配置模板参考

## 项目结构

```
fast-agent/
├── fast-agent-model/          # 数据模型层
├── fast-agent-dao/            # 数据访问层
├── fast-agent-service/        # 服务层
├── fast-agent-web/            # Web层
├── fast-agent-core/           # 核心配置
├── fast-agent-common/         # 公共组件
├── fast-agent-knowledge/      # 知识库
└── .env.example               # 环境变量模板
```
