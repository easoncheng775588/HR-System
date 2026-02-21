# 数据库初始化脚本
# 使用方法：在PowerShell中运行此脚本

# MySQL连接参数
$MySQLHost = "localhost"
$MySQLPort = "3306"
$MySQLUser = "root"
$MySQLPassword = "p@ssw0rd"

# 获取脚本所在目录
$ScriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$SQLScript = Join-Path $ScriptPath "create_recruitment_request.sql"

Write-Host "正在初始化数据库..." -ForegroundColor Green
Write-Host "MySQL连接信息: $MySQLUser@$MySQLHost:$MySQLPort" -ForegroundColor Yellow
Write-Host ""

# 检查SQL脚本是否存在
if (-not (Test-Path $SQLScript)) {
    Write-Host "错误: 找不到SQL脚本文件: $SQLScript" -ForegroundColor Red
    exit 1
}

# 尝试连接MySQL并执行脚本
try {
    # 这里需要根据实际的MySQL安装路径调整
    # 常见路径: C:\Program Files\MySQL\MySQL Server X.X\bin\mysql.exe
    $MySQLPath = "mysql.exe"
    
    # 检查mysql命令是否可用
    $null = Get-Command $MySQLPath -ErrorAction Stop
    
    Write-Host "正在执行SQL脚本: $SQLScript" -ForegroundColor Cyan
    & $MySQLPath -h $MySQLHost -P $MySQLPort -u $MySQLUser -p$MySQLPassword < $SQLScript
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "数据库初始化成功！" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "数据库初始化失败，退出代码: $LASTEXITCODE" -ForegroundColor Red
    }
} catch {
    Write-Host "错误: 无法找到MySQL命令行工具" -ForegroundColor Red
    Write-Host "请确保MySQL已安装并添加到系统PATH中" -ForegroundColor Yellow
    Write-Host "或者手动执行以下SQL脚本: $SQLScript" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "手动执行步骤:" -ForegroundColor Cyan
    Write-Host "1. 打开MySQL命令行工具或MySQL Workbench" -ForegroundColor White
    Write-Host "2. 使用root用户登录 (密码: wlb2013#)" -ForegroundColor White
    Write-Host "3. 执行SQL脚本文件: $SQLScript" -ForegroundColor White
}

Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")