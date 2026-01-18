package physics.camera;

import math.Vector3f;
import utils.PhysicsConfig;

public class AdaptiveClipping {
    private final PhysicsConfig config;
    private float lastNear;
    private float lastFar;
    
    public AdaptiveClipping(PhysicsConfig config) {
        this.config = config;
        this.lastNear = config.nearPlane;
        this.lastFar = config.farPlane;
    }
    
    public ClipResult computeClippingPlanes(Vector3f cameraPos,
                                          Vector3f target,
                                          float suggestedNear,
                                          float suggestedFar) {
        float distanceToTarget = cameraPos.distance(target);
    
        float adaptiveNear = Math.max(
            config.minNearPlane,
            Math.min(
                distanceToTarget * 0.01f,
                config.maxNearPlane
            )
        );
        
        adaptiveNear = smoothTransition(lastNear, adaptiveNear, 0.1f);
        
        float adaptiveFar = Math.max(
            distanceToTarget * 2.0f, 
            config.minFarPlane
        );
       
        float ratio = adaptiveFar / adaptiveNear;
        if (ratio > config.maxDepthRatio) {
            adaptiveFar = adaptiveNear * config.maxDepthRatio;
        }
        
        adaptiveFar = smoothTransition(lastFar, adaptiveFar, 0.05f);
        
        lastNear = adaptiveNear;
        lastFar = adaptiveFar;
        
        return new ClipResult(adaptiveNear, adaptiveFar, ratio);
    }
    
    private float smoothTransition(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
    
    public static class ClipResult {
        public final float near;
        public final float far;
        public final float depthRatio;
        
        public ClipResult(float near, float far, float depthRatio) {
            this.near = near;
            this.far = far;
            this.depthRatio = depthRatio;
        }
    }
}