#!/bin/bash
# 数据库备份脚本
set -e

# 配置
CONTAINER_NAME="trae-mysql"
DB_NAME="trae_ams"
DB_USER="root"
DB_PASSWORD="${DB_ROOT_PASSWORD:-123456}" # 建议从环境变量获取
BACKUP_DIR="/opt/trae-kasa/deploy/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/$DB_NAME-$DATE.sql.gz"

# 确保备份目录存在
mkdir -p "$BACKUP_DIR"

# 备份
echo "开始备份数据库 $DB_NAME..."
docker exec "$CONTAINER_NAME" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" --single-transaction --routines --triggers "$DB_NAME" | gzip > "$BACKUP_FILE"

# 保留最近 7 天的备份
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +7 -delete

echo "备份完成: $BACKUP_FILE"
