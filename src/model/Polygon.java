package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления полигона (многоугольника) 3D модели
 */
public class Polygon {
    private List<Integer> vertexIndices;

    public Polygon() {
        this.vertexIndices = new ArrayList<>();
    }

    public Polygon(List<Integer> vertexIndices) {
        this.vertexIndices = new ArrayList<>(vertexIndices);
    }

    public List<Integer> getVertexIndices() {
        return new ArrayList<>(vertexIndices);
    }

    public void addVertexIndex(int index) {
        vertexIndices.add(index);
    }

    public int getVertexCount() {
        return vertexIndices.size();
    }

    public boolean containsVertex(int vertexIndex) {
        return vertexIndices.contains(vertexIndex);
    }

    /**
     * Обновляет индексы вершин: уменьшает индексы больше удаленного на 1
     */
    public void updateIndicesAfterVertexRemoval(int removedIndex) {
        List<Integer> newIndices = new ArrayList<>();
        for (Integer idx : vertexIndices) {
            if (idx > removedIndex) {
                newIndices.add(idx - 1);
            } else if (idx < removedIndex) {
                newIndices.add(idx);
            }

        }
        this.vertexIndices = newIndices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("f");
        for (Integer index : vertexIndices) {
            sb.append(" ").append(index + 1);
        }
        return sb.toString();
    }
}
