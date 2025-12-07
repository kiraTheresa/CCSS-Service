1. 编辑 docker-compose.yml 文件
2. 在 nacos 服务的 environment 部分添加 `- NACOS_AUTH_ENABLE=false` 配置
3. 保存文件
4. 重启 Nacos 服务使配置生效

当前 Nacos 服务已经配置了 `MODE=standalone`，只需添加关闭认证的环境变量即可，这是最简单、快速的方式来关闭 Nacos 的 Auth 功能。