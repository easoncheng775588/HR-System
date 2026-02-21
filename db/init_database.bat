@echo off
echo 正在初始化数据库...
echo.

REM 设置MySQL连接参数
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=p@ssw0rd

REM 执行数据库创建脚本
echo 执行创建数据库脚本...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% < create_recruitment_request.sql

if %errorlevel% equ 0 (
    echo.
    echo 数据库初始化成功！
) else (
    echo.
    echo 数据库初始化失败，请检查MySQL连接信息。
)

pause
