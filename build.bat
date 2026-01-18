@echo off
echo Building 3D Model Viewer...
echo.

REM Создаем директорию для выходных файлов
if not exist "out" mkdir out
if not exist "out\classes" mkdir out\classes
if not exist "dist" mkdir dist

REM Компилируем Java файлы
echo Compiling Java sources...
javac -d out\classes -encoding UTF-8 -sourcepath src src\Main.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Создаем JAR файл
echo Creating JAR file...
cd out\classes
jar cfm ..\..\dist\ModelViewer.jar ..\..\META-INF\MANIFEST.MF .

if errorlevel 1 (
    echo JAR creation failed!
    cd ..\..
    pause
    exit /b 1
)

cd ..\..

echo.
echo Build successful!
echo JAR file created: dist\ModelViewer.jar
echo.
echo To run the application, use: java -jar dist\ModelViewer.jar
echo Or use the run.bat script.
pause
