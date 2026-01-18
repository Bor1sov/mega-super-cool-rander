#!/bin/bash

# Запуск приложения из JAR файла
# Если JAR не существует, пытаемся собрать его

if [ ! -f "dist/ModelViewer.jar" ]; then
    echo "JAR file not found. Building..."
    ./build.sh
    if [ $? -ne 0 ]; then
        echo "Build failed. Cannot run application."
        exit 1
    fi
fi

echo "Starting 3D Model Viewer..."
java -jar dist/ModelViewer.jar
