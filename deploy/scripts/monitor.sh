#!/bin/bash
# 简单的系统监控脚本

# 设置阈值
CPU_THRESHOLD=80
MEM_THRESHOLD=80
DISK_THRESHOLD=90
LOG_FILE="/opt/trae-kasa/deploy/monitor.log"

# 获取当前状态
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')
MEM_USAGE=$(free | grep Mem | awk '{print $3/$2 * 100.0}')
DISK_USAGE=$(df -h / | awk '$NF=="/"{printf "%s", $5}' | sed 's/%//')

# 记录日志
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

# 检查 CPU
if (( $(echo "$CPU_USAGE > $CPU_THRESHOLD" | bc -l) )); then
    log "警告: CPU 使用率过高 ($CPU_USAGE%)"
fi

# 检查内存
if (( $(echo "$MEM_USAGE > $MEM_THRESHOLD" | bc -l) )); then
    log "警告: 内存使用率过高 ($MEM_USAGE%)"
fi

# 检查磁盘
if [ "$DISK_USAGE" -gt "$DISK_THRESHOLD" ]; then
    log "警告: 磁盘使用率过高 ($DISK_USAGE%)"
fi

# 检查容器状态
docker ps --format "{{.Names}} {{.Status}}" | grep -v "Up" >> "$LOG_FILE"
