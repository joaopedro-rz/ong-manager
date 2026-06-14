#!/bin/sh
# Script de backup do PostgreSQL. Execute periodicamente via cron.
DATE=$(date +%Y%m%d_%H%M%S)
docker exec ong_db pg_dump -U "${POSTGRES_USER:-ong_user}" "${POSTGRES_DB:-ong_manager}" \
  > "./scripts/backup/backup_$DATE.sql"
echo "Backup criado: backup_$DATE.sql"
