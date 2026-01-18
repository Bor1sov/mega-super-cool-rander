package model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс для управления сценой с несколькими моделями
 */
public class Scene {
    private List<Model> models;
    private Set<Integer> selectedModelIndices;
    private int activeModelIndex;

    public Scene() {
        this.models = new ArrayList<>();
        this.selectedModelIndices = new HashSet<>();
        this.activeModelIndex = -1;
    }

    public List<Model> getModels() {
        return new ArrayList<>(models);
    }

    public void addModel(Model model) {
        models.add(model);
        if (models.size() == 1) {

            setActiveModel(0);
        }
    }

    public void removeModel(int index) {
        if (index < 0 || index >= models.size()) {
            throw new IndexOutOfBoundsException("Model index out of bounds: " + index);
        }
        models.remove(index);
        selectedModelIndices.remove(index);
        

        Set<Integer> newSelected = new HashSet<>();
        for (Integer idx : selectedModelIndices) {
            if (idx < index) {
                newSelected.add(idx);
            } else if (idx > index) {
                newSelected.add(idx - 1);
            }
        }
        selectedModelIndices = newSelected;
        

        if (activeModelIndex == index) {
            activeModelIndex = models.isEmpty() ? -1 : Math.min(index, models.size() - 1);
        } else if (activeModelIndex > index) {
            activeModelIndex--;
        }
    }

    public Model getModel(int index) {
        if (index < 0 || index >= models.size()) {
            throw new IndexOutOfBoundsException("Model index out of bounds: " + index);
        }
        return models.get(index);
    }

    public int getModelCount() {
        return models.size();
    }

    public boolean isModelSelected(int index) {
        return selectedModelIndices.contains(index);
    }

    public void selectModel(int index) {
        if (index >= 0 && index < models.size()) {
            selectedModelIndices.add(index);
        }
    }

    public void deselectModel(int index) {
        selectedModelIndices.remove(index);
    }

    public void toggleModelSelection(int index) {
        if (isModelSelected(index)) {
            deselectModel(index);
        } else {
            selectModel(index);
        }
    }

    public void clearSelection() {
        selectedModelIndices.clear();
    }

    public Set<Integer> getSelectedModelIndices() {
        return new HashSet<>(selectedModelIndices);
    }

    public int getActiveModelIndex() {
        return activeModelIndex;
    }

    public Model getActiveModel() {
        if (activeModelIndex >= 0 && activeModelIndex < models.size()) {
            return models.get(activeModelIndex);
        }
        return null;
    }

    public void setActiveModel(int index) {
        if (index >= 0 && index < models.size()) {
            activeModelIndex = index;

            selectModel(index);
        } else {
            activeModelIndex = -1;
        }
    }

    public boolean hasActiveModel() {
        return activeModelIndex >= 0 && activeModelIndex < models.size();
    }
}
