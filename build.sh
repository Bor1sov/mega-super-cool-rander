#!/bin/bash

echo "Building 3D Model Viewer..."
echo

# Создаем директорию для выходных файлов
mkdir -p out/classes
mkdir -p dist

# Компилируем Java файлы
echo "Compiling Java sources..."
javac -d out/classes -encoding UTF-8 -sourcepath src src/Main.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Создаем JAR файл
echo "Creating JAR file..."
cd out/classes
jar cfm ../../dist/ModelViewer.jar ../../META-INF/MANIFEST.MF .

if [ $? -ne 0 ]; then
    echo "JAR creation failed!"
    cd ../..
    exit 1
fi

cd ../..

echo
echo "Build successful!"
echo "JAR file created: dist/ModelViewer.jar"
echo
echo "To run the application, use: java -jar dist/ModelViewer.jar"
echo "Or use the run.sh script."
