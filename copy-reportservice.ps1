$sourcePath = "D:\dev\repository\myproject\backend\src\main\java\com\oss\service\ReportService_NEW.java"
$targetPath = "D:\dev\repository\myproject\backend\src\main\java\com\oss\service\ReportService.java"

if (Test-Path $sourcePath) {
    Copy-Item -Path $sourcePath -Destination $targetPath -Force
    Remove-Item -Path $sourcePath -Force
    Write-Host "ReportService.java has been fixed!" -ForegroundColor Green
} else {
    Write-Host "Source file not found!" -ForegroundColor Red
}

