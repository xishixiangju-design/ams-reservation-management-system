#!/bin/bash
set -e

# 配置变量
PROJECT_DIR="/opt/trae-kasa"
DEPLOY_DIR="$PROJECT_DIR/deploy"
LOG_FILE="$PROJECT_DIR/deploy/deploy.log"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "开始部署流程..."

# 1. 更新代码
log "正在拉取最新代码..."
cd "$PROJECT_DIR"
git pull origin main

# 2. 构建并启动容器
log "正在构建并启动容器..."
cd "$DEPLOY_DIR"
docker-compose down
docker-compose up -d --build

# 3. 清理无用镜像
log "清理无用镜像..."
docker image prune -f

log "部署完成！"
docker-compose ps
