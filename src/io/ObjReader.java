package io;

import model.Model;
import model.Vertex;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Класс для чтения 3D моделей из OBJ файлов
 */
public class ObjReader {
    private static final String OBJ_COMMENT_TOKEN = "#";

    /**
     * Читает модель из файла
     * @param filePath путь к файлу
     * @return объект Model
     * @throws ObjReaderException если произошла ошибка при чтении файла
     */
    public static Model read(String filePath) throws ObjReaderException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ObjReaderException("File not found: " + filePath);
        }
        String modelName = file.getName();
        Model model = new Model(modelName);
        List<double[]> tempVertices = new ArrayList<>();
        List<double[]> tempTexCoords = new ArrayList<>();
        List<double[]> tempNormals = new ArrayList<>();
        List<int[]> faceVertexIndices = new ArrayList<>();
        List<int[]> faceTexIndices = new ArrayList<>();
        List<int[]> faceNormIndices = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) continue;
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v": {
                        double x = Double.parseDouble(tokens[1]);
                        double y = Double.parseDouble(tokens[2]);
                        double z = Double.parseDouble(tokens[3]);
                        tempVertices.add(new double[]{x, y, z});
                        break;
                    }
                    case "vt": {
                        double u = Double.parseDouble(tokens[1]);
                        double v = tokens.length > 2 ? Double.parseDouble(tokens[2]) : 0.0;
                        tempTexCoords.add(new double[]{u, v});
                        break;
                    }
                    case "vn": {
                        double nx = Double.parseDouble(tokens[1]);
                        double ny = Double.parseDouble(tokens[2]);
                        double nz = Double.parseDouble(tokens[3]);
                        tempNormals.add(new double[]{nx, ny, nz});
                        break;
                    }
                    case "f": {
                        int n = tokens.length - 1;
                        int[] vIdx = new int[n];
                        int[] tIdx = new int[n];
                        int[] nIdx = new int[n];
                        for (int i = 0; i < n; i++) {
                            String[] parts = tokens[i + 1].split("/");
                            vIdx[i] = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0]) - 1 : -1;
                            tIdx[i] = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) - 1 : -1;
                            nIdx[i] = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) - 1 : -1;
                        }
                        faceVertexIndices.add(vIdx);
                        faceTexIndices.add(tIdx);
                        faceNormIndices.add(nIdx);
                        break;
                    }
                    default:
                        // игнорировать другие строки
                }
            }

            // Создаем вершины с UV и нормалями (если есть)
            // Для простоты: для каждой грани создаём новые вершины (без оптимизации по совпадающим)
            for (int f = 0; f < faceVertexIndices.size(); f++) {
                int[] vIdx = faceVertexIndices.get(f);
                int[] tIdx = faceTexIndices.get(f);
                int[] nIdx = faceNormIndices.get(f);
                // Триангуляция: разбиваем n-угольник на (n-2) треугольника (фан)
                if (vIdx.length < 3) continue;
                for (int i = 1; i < vIdx.length - 1; i++) {
                    List<Integer> triIndices = new ArrayList<>();
                    int[] triV = {0, i, i+1};
                    for (int j = 0; j < 3; j++) {
                        int vi = vIdx[triV[j]];
                        int ti = tIdx[triV[j]];
                        int ni = nIdx[triV[j]];
                        double[] pos = vi >= 0 ? tempVertices.get(vi) : new double[]{0,0,0};
                        double u = (ti >= 0 && ti < tempTexCoords.size()) ? tempTexCoords.get(ti)[0] : 0.0;
                        double v = (ti >= 0 && ti < tempTexCoords.size()) ? tempTexCoords.get(ti)[1] : 0.0;
                        double nx = (ni >= 0 && ni < tempNormals.size()) ? tempNormals.get(ni)[0] : 0.0;
                        double ny = (ni >= 0 && ni < tempNormals.size()) ? tempNormals.get(ni)[1] : 0.0;
                        double nz = (ni >= 0 && ni < tempNormals.size()) ? tempNormals.get(ni)[2] : 1.0;
                        Vertex vert = new Vertex(pos[0], pos[1], pos[2], u, v, nx, ny, nz);
                        model.addVertex(vert);
                        triIndices.add(model.getVertexCount() - 1);
                    }
                    model.addPolygon(new Polygon(triIndices));
                }
            }

            // Если не было ни одной грани (face), добавить просто вершины
            if (faceVertexIndices.isEmpty()) {
                for (double[] pos : tempVertices) {
                    model.addVertex(new Vertex(pos[0], pos[1], pos[2]));
                }
            }

        } catch (FileNotFoundException e) {
            throw new ObjReaderException("File not found: " + filePath);
        } catch (Exception e) {
            throw new ObjReaderException("Error reading OBJ file: " + e.getMessage());
        }
        return model;
    }
}
