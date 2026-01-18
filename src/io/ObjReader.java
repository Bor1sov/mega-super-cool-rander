package io;

import model.Model;
import model.Vertex;
import model.Polygon;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Класс для чтения 3D моделей из OBJ файлов
 */
public class ObjReader {
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_FACE_TOKEN = "f";
    private static final String OBJ_COMMENT_TOKEN = "#";

    /**
     * Читает модель из файла
     * @param filePath путь к файлу
     * @return объект Model
     * @throws ObjReaderException если произошла ошибка при чтении файла
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static Model read(String filePath) throws ObjReaderException, IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ObjReaderException("File not found: " + filePath);
        }
        if (!file.canRead()) {
            throw new ObjReaderException("Cannot read file: " + filePath);
        }

        String fileName = file.getName();
        if (fileName.lastIndexOf('.') > 0) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        Model model = new Model(fileName);

        // Пытаемся определить кодировку файла
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Пропускаем пустые строки
                if (line.isEmpty()) {
                    continue;
                }

                // Удаляем комментарии в конце строки
                int commentIndex = line.indexOf(OBJ_COMMENT_TOKEN);
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex).trim();
                    // Если после удаления комментария строка пустая, пропускаем
                    if (line.isEmpty()) {
                        continue;
                    }
                }

                // Пропускаем строки, которые начинаются с комментария
                if (line.startsWith(OBJ_COMMENT_TOKEN)) {
                    continue;
                }

                try {
                    parseLine(line, model, lineNumber);
                } catch (ObjReaderException e) {
                    throw new ObjReaderException(
                        String.format("Line %d: %s\nLine content: %s", lineNumber, e.getMessage(), line), 
                        lineNumber);
                }
            }
        }

        return model;
    }

    /**
     * Парсит одну строку файла
     */
    private static void parseLine(String line, Model model, int lineNumber) throws ObjReaderException {
        // Нормализуем пробелы: заменяем табы и множественные пробелы на одинарные
        line = line.replaceAll("\t", " ").replaceAll(" +", " ").trim();
        
        if (line.isEmpty()) {
            return;
        }

        Scanner scanner = new Scanner(line);
        scanner.useLocale(java.util.Locale.US);
        
        if (!scanner.hasNext()) {
            scanner.close();
            return;
        }

        String token = scanner.next();

        switch (token) {
            case OBJ_VERTEX_TOKEN:
                // Проверяем, что это именно "v", а не "vt", "vn" и т.д.
                parseVertex(scanner, model, lineNumber);
                break;
            case OBJ_FACE_TOKEN:
                parseFace(scanner, model, lineNumber);
                break;
            default:
                // Игнорируем другие токены (vt, vn, mtl, usemtl, s, o, g и т.д.)
                // Это нормально для OBJ файлов
                break;
        }

        scanner.close();
    }

    /**
     * Парсит вершину (v x y z [w])
     */
    private static void parseVertex(Scanner scanner, Model model, int lineNumber) throws ObjReaderException {
        // Используем локаль с точкой для десятичных чисел
        scanner.useLocale(java.util.Locale.US);
        
        if (!scanner.hasNext()) {
            throw new ObjReaderException("Vertex data is missing. Expected format: v x y z");
        }

        String xStr = scanner.next();
        double x;
        try {
            x = Double.parseDouble(xStr);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid x coordinate: " + xStr + ". Expected format: v x y z");
        }

        if (!scanner.hasNext()) {
            throw new ObjReaderException("Vertex y coordinate is missing. Expected format: v x y z");
        }
        String yStr = scanner.next();
        double y;
        try {
            y = Double.parseDouble(yStr);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid y coordinate: " + yStr + ". Expected format: v x y z");
        }

        if (!scanner.hasNext()) {
            throw new ObjReaderException("Vertex z coordinate is missing. Expected format: v x y z");
        }
        String zStr = scanner.next();
        double z;
        try {
            z = Double.parseDouble(zStr);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid z coordinate: " + zStr + ". Expected format: v x y z");
        }

        // w координата опциональна
        double w = 1.0;
        if (scanner.hasNext()) {
            String wStr = scanner.next();
            try {
                w = Double.parseDouble(wStr);
            } catch (NumberFormatException e) {
                // Если w не число, игнорируем (может быть комментарий или другой токен)
            }
        }

        // Применяем w для нормализации (если w != 1.0)
        if (Math.abs(w - 1.0) > 1e-6) {
            x /= w;
            y /= w;
            z /= w;
        }

        model.addVertex(new Vertex(x, y, z));
    }

    /**
     * Парсит полигон (face) - f v1 v2 v3 ... или f v1/vt1 v2/vt2 ...
     */
    private static void parseFace(Scanner scanner, Model model, int lineNumber) throws ObjReaderException {
        List<Integer> vertexIndices = new ArrayList<>();

        while (scanner.hasNext()) {
            String vertexData = scanner.next();
            int vertexIndex = parseVertexIndex(vertexData, model, lineNumber);
            vertexIndices.add(vertexIndex);
        }

        if (vertexIndices.size() < 3) {
            throw new ObjReaderException("Face must have at least 3 vertices. Found: " + vertexIndices.size());
        }

        Polygon polygon = new Polygon(vertexIndices);
        model.addPolygon(polygon);
    }

    /**
     * Парсит индекс вершины из строки вида "v", "v/vt", "v/vt/vn"
     * OBJ использует индексацию с 1, а мы используем с 0
     */
    private static int parseVertexIndex(String vertexData, Model model, int lineNumber) throws ObjReaderException {
        // Разделяем по "/" для обработки форматов v, v/vt, v/vt/vn
        String[] parts = vertexData.split("/");

        if (parts.length == 0 || parts[0].isEmpty()) {
            throw new ObjReaderException("Invalid vertex index format: " + vertexData);
        }

        try {
            int vertexIndex = Integer.parseInt(parts[0]);

            if (vertexIndex > 0) {
                vertexIndex = vertexIndex - 1;
            } else if (vertexIndex < 0) {

                vertexIndex = model.getVertexCount() + vertexIndex;
            } else {
                throw new ObjReaderException("Vertex index cannot be zero: " + parts[0]);
            }


            if (vertexIndex < 0 || vertexIndex >= model.getVertexCount()) {
                throw new ObjReaderException(
                    String.format("Vertex index %d is out of bounds. Model has %d vertices.", 
                        vertexIndex + 1, model.getVertexCount()));
            }

            return vertexIndex;
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid vertex index: " + parts[0]);
        }
    }
}
