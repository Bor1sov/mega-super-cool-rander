
package physics.field;

import java.util.*;
import math.Vector3f;
import utils.PhysicsConfig;

public class SpatialPartition {
    private static class GridCell {
        List<Integer> objectIndices = new ArrayList<>();
    }
    
    private final PhysicsConfig config;
    private GridCell[][][] grid;
    private final float cellSize;
    private final int gridSize;
    
    private Vector3f worldMin;
    private Vector3f worldMax;
    private Vector3f[] objectPositions;
    private final int searchRadiusCells = 3; 
    
    public SpatialPartition(PhysicsConfig config) {
        this.config = config;
        this.cellSize = config.spatialGridSize;
        this.gridSize = 100;
    }
    
    public void initialize() {
        grid = new GridCell[gridSize][gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                for (int k = 0; k < gridSize; k++) {
                    grid[i][j][k] = new GridCell();
                }
            }
        }
        
        float halfWorld = gridSize * cellSize * 0.5f;
        worldMin = new Vector3f(-halfWorld, -halfWorld, -halfWorld);
        worldMax = new Vector3f(halfWorld, halfWorld, halfWorld);
    }
    
    public void update(Vector3f[] positions, float[] radii) {
        this.objectPositions = positions;

        clearGrid();
        
        for (int i = 0; i < positions.length; i++) {
            Vector3f pos = positions[i];
            float radius = radii[i];
            
            int[] minCell = worldToGrid(pos.sub(new Vector3f(radius, radius, radius)));
            int[] maxCell = worldToGrid(pos.add(new Vector3f(radius, radius, radius)));
            
            for (int x = Math.max(0, minCell[0]); x <= Math.min(gridSize - 1, maxCell[0]); x++) {
                for (int y = Math.max(0, minCell[1]); y <= Math.min(gridSize - 1, maxCell[1]); y++) {
                    for (int z = Math.max(0, minCell[2]); z <= Math.min(gridSize - 1, maxCell[2]); z++) {
                        grid[x][y][z].objectIndices.add(i);
                    }
                }
            }
        }
    }
    
    public List<Integer> getNearbyObjects(Vector3f position) {
        List<Integer> nearby = new ArrayList<>();
        Set<Integer> unique = new HashSet<>();
        
        int[] centerCell = worldToGrid(position);
        int searchRadius = searchRadiusCells;
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = centerCell[0] + dx;
                    int y = centerCell[1] + dy;
                    int z = centerCell[2] + dz;
                    
                    if (x >= 0 && x < gridSize && 
                        y >= 0 && y < gridSize && 
                        z >= 0 && z < gridSize) {
                        for (int index : grid[x][y][z].objectIndices) {
                            if (unique.add(index)) {
                                nearby.add(index);
                            }
                        }
                    }
                }
            }
        }
        
        nearby.sort((a, b) -> {
            float distA = position.distance(objectPositions[a]);
            float distB = position.distance(objectPositions[b]);
            return Float.compare(distA, distB);
        });
        
        if (nearby.size() > config.maxNearbyObjects) {
            nearby = nearby.subList(0, config.maxNearbyObjects);
        }
        
        return nearby;
    }
    
    private int[] worldToGrid(Vector3f worldPos) {
        float x = (worldPos.x - worldMin.x) / (worldMax.x - worldMin.x) * gridSize;
        float y = (worldPos.y - worldMin.y) / (worldMax.y - worldMin.y) * gridSize;
        float z = (worldPos.z - worldMin.z) / (worldMax.z - worldMin.z) * gridSize;
        
        return new int[]{
            Math.max(0, Math.min(gridSize - 1, (int)Math.floor(x))),
            Math.max(0, Math.min(gridSize - 1, (int)Math.floor(y))),
            Math.max(0, Math.min(gridSize - 1, (int)Math.floor(z)))
        };
    }
    
    private void clearGrid() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                for (int k = 0; k < gridSize; k++) {
                    grid[i][j][k].objectIndices.clear();
                }
            }
        }
    }
}
