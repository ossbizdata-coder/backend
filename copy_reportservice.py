import shutil
import os

source = r"D:\dev\repository\myproject\backend\src\main\java\com\oss\service\ReportService_NEW.java"
target = r"D:\dev\repository\myproject\backend\src\main\java\com\oss\service\ReportService.java"

if os.path.exists(source):
    shutil.copy(source, target)
    os.remove(source)
    print("✓ ReportService.java has been fixed!")
else:
    print("✗ Source file not found!")

