
package physics.field;

import math.Vector3f;
import utils.PhysicsConfig;

public class AdaptiveMetrics {
    private final PhysicsConfig config;
    
    private float currentNearPlane;
    private float currentFarPlane;
    private float currentFov;
    private float currentDepthQuality;
    
    private final MetricsHistory history;
    
    public AdaptiveMetrics(PhysicsConfig config) {
        this.config = config;
        this.currentNearPlane = config.nearPlane;
        this.currentFarPlane = config.farPlane;
        this.currentFov = config.fov;
        this.history = new MetricsHistory(60);
    }
    
    public void update(Vector3f cameraPos, Vector3f[] objectPositions) {
        float minDistance = Float.MAX_VALUE;
        float maxDistance = 0.0f;
        float avgDistance = 0.0f;
        int visibleCount = objectPositions.length;
        
        for (Vector3f pos : objectPositions) {
            float dist = cameraPos.distance(pos);
            if (dist > 0.01f) {
                minDistance = Math.min(minDistance, dist);
                maxDistance = Math.max(maxDistance, dist);
                avgDistance += dist;
            }
        }
        
        if (visibleCount > 0) {
            avgDistance /= visibleCount;
        } else {
            minDistance = 1.0f;
            maxDistance = 100.0f;
            avgDistance = 50.0f;
        }
   
        float targetNear = Math.max(config.minNearPlane, minDistance * 0.05f);
        currentNearPlane = lerp(currentNearPlane, targetNear, 0.1f);
       
        float targetFar = Math.max(config.minFarPlane, maxDistance * 2.0f);
        float maxRatio = config.maxDepthRatio;
        if (targetFar / currentNearPlane > maxRatio) {
            targetFar = currentNearPlane * maxRatio;
        }
        currentFarPlane = lerp(currentFarPlane, targetFar, 0.05f);
        
        float targetFov = config.fov;
        if (minDistance < 10.0f) {
            targetFov = config.fov * 0.8f;
        }
        currentFov = lerp(currentFov, targetFov, 0.05f);
        
        currentDepthQuality = 1.0f - Math.min(1.0f, (currentFarPlane / currentNearPlane) / 1000.0f);
        
        history.add(new FrameMetrics(
            currentNearPlane, currentFarPlane, currentFov,
            currentDepthQuality, minDistance, maxDistance, avgDistance
        ));
    }
    
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    
    public float getCurrentNearPlane() { return currentNearPlane; }
    public float getCurrentFarPlane() { return currentFarPlane; }
    public float getCurrentFov() { return currentFov; }
    
    public Metrics getMetrics() {
        return new Metrics(
            currentNearPlane, currentFarPlane, currentFov,
            currentDepthQuality,
            history.getAverageQuality(),
            history.getStabilityScore()
        );
    }
    
    public static class Metrics {
        public final float nearPlane;
        public final float farPlane;
        public final float fov;
        public final float depthQuality;
        public final float avgQuality;
        public final float stability;
        
        public Metrics(float nearPlane, float farPlane, float fov,
                      float depthQuality, float avgQuality, float stability) {
            this.nearPlane = nearPlane;
            this.farPlane = farPlane;
            this.fov = fov;
            this.depthQuality = depthQuality;
            this.avgQuality = avgQuality;
            this.stability = stability;
        }
    }
    
    private static class FrameMetrics {
        final float quality;
        
        FrameMetrics(float near, float far, float fov, float quality,
                    float minDist, float maxDist, float avgDist) {
            this.quality = quality;
        }
    }
    
    private static class MetricsHistory {
        private final FrameMetrics[] buffer;
        private int head = 0;
        private int size = 0;
        
        MetricsHistory(int capacity) {
            buffer = new FrameMetrics[capacity];
        }
        
        void add(FrameMetrics metrics) {
            buffer[head] = metrics;
            head = (head + 1) % buffer.length;
            if (size < buffer.length) size++;
        }
        
        float getAverageQuality() {
            if (size == 0) return 1.0f;
            float sum = 0.0f;
            for (int i = 0; i < size; i++) {
                int idx = (head - 1 - i + buffer.length) % buffer.length;
                sum += buffer[idx].quality;
            }
            return sum / size;
        }
        
        float getStabilityScore() {
            if (size < 2) return 1.0f;
            float mean = getAverageQuality();
            float variance = 0.0f;
            for (int i = 0; i < size; i++) {
                int idx = (head - 1 - i + buffer.length) % buffer.length;
                float diff = buffer[idx].quality - mean;
                variance += diff * diff;
            }
            variance /= size;
            return 1.0f / (1.0f + variance * 100.0f);
        }
    }
}
