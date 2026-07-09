@echo off
chcp 65001 >nul
echo ============================================
echo   新疆大学招生宣传报名平台 — 一键启动
echo ============================================
echo.
echo [1/2] 启动后端 (端口 8080)...
start "Backend" cmd /c "cd enrollment-backend && mvn spring-boot:run"
echo 等待后端启动中 (约 25 秒)...
timeout /t 25 /nobreak >nul
echo.
echo [2/2] 启动前端 (端口 3000)...
start "Frontend" cmd /c "cd enrollment-frontend && npx vite --port 3000"
timeout /t 5 /nobreak >nul
echo.
echo ============================================
echo   启动完成！浏览器即将打开...
echo   如果没打开，请访问 http://localhost:3000
echo   测试账号: school1 / 123456
echo ============================================
start http://localhost:3000
pause
