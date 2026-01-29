package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления 3D модели
 */
public class Model {
    private String name;
    private List<Vertex> vertices;
    private List<Polygon> polygons;
    // Текстура модели
    private java.awt.image.BufferedImage texture;
    // Нормали для вершин/глобально
    private List<double[]> normals = new ArrayList<>(); // [nx, ny, nz]

    public void setTexture(java.awt.image.BufferedImage texture) {
        this.texture = texture;
    }

    public java.awt.image.BufferedImage getTexture() {
        return texture;
    }

    public void addNormal(double nx, double ny, double nz) {
        normals.add(new double[]{nx, ny, nz});
    }

    public List<double[]> getNormals() {
        return new ArrayList<>(normals);
    }

    public double[] getNormal(int index) {
        return normals.get(index);
    }

    public int getNormalCount() {
        return normals.size();
    }

    public Model(String name) {
        this.name = name;
        this.vertices = new ArrayList<>();
        this.polygons = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Vertex> getVertices() {
        return new ArrayList<>(vertices);
    }

    public List<Polygon> getPolygons() {
        return new ArrayList<>(polygons);
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getPolygonCount() {
        return polygons.size();
    }

    public Vertex getVertex(int index) {
        if (index < 0 || index >= vertices.size()) {
            throw new IndexOutOfBoundsException("Vertex index out of bounds: " + index);
        }
        return vertices.get(index);
    }

    public Polygon getPolygon(int index) {
        if (index < 0 || index >= polygons.size()) {
            throw new IndexOutOfBoundsException("Polygon index out of bounds: " + index);
        }
        return polygons.get(index);
    }

    /**
     * Удаляет вершину по индексу и обновляет индексы в полигонах
     */
    public void removeVertex(int index) {
        if (index < 0 || index >= vertices.size()) {
            throw new IndexOutOfBoundsException("Vertex index out of bounds: " + index);
        }

        List<Polygon> polygonsToRemove = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.containsVertex(index)) {
                polygonsToRemove.add(polygon);
            }
        }
        polygons.removeAll(polygonsToRemove);

        vertices.remove(index);

        for (Polygon polygon : polygons) {
            polygon.updateIndicesAfterVertexRemoval(index);
        }
    }

    /**
     * Удаляет полигон по индексу
     */
    public void removePolygon(int index) {
        if (index < 0 || index >= polygons.size()) {
            throw new IndexOutOfBoundsException("Polygon index out of bounds: " + index);
        }
        polygons.remove(index);
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", vertices=" + vertices.size() +
                ", polygons=" + polygons.size() +
                '}';
    }
}
