package io;

// import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для чтения MTL-файлов (материалов) для OBJ-моделей
 */
public class MtlReader {
    public static class MaterialInfo {
        public String name;
        public String textureFile;
    }

    /**
     * Парсит MTL-файл и возвращает карту материалов (имя -> MaterialInfo)
     */
    public static Map<String, MaterialInfo> read(String mtlPath) throws IOException {
        Map<String, MaterialInfo> materials = new HashMap<>();
        MaterialInfo current = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mtlPath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("newmtl ")) {
                    current = new MaterialInfo();
                    current.name = line.substring(7).trim();
                    materials.put(current.name, current);
                } else if (current != null && line.startsWith("map_Kd ")) {
                    current.textureFile = line.substring(7).trim();
                }
            }
        }
        return materials;
    }
}
