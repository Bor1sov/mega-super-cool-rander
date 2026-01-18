@echo off
REM Запуск приложения из JAR файла
REM Если JAR не существует, пытаемся собрать его

if not exist "dist\ModelViewer.jar" (
    echo JAR file not found. Building...
    call build.bat
    if errorlevel 1 (
        echo Build failed. Cannot run application.
        pause
        exit /b 1
    )
)

echo Starting 3D Model Viewer...
java -jar dist\ModelViewer.jar
