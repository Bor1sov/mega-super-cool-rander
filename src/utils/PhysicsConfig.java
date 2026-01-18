package utils;

public class PhysicsConfig {
    public float cameraSpeed = 5.0f;
    public float cameraRotationSpeed = 2.0f;
    public float minCameraDistance = 0.5f;
    public float maxCameraDistance = 100.0f;
    public float minHeight = 0.0f;
    
    public float nearPlane = 0.1f;
    public float farPlane = 100.0f;
    public float minNearPlane = 0.01f;
    public float maxNearPlane = 10.0f;
    public float minFarPlane = 10.0f;
    public float maxDepthRatio = 10000.0f;

    public float fov = 60.0f;
    public float minFov = 30.0f;
    public float maxFov = 120.0f;
    public float aspectRatio = 16.0f / 9.0f;

    public boolean useReverseZ = true;
    public boolean useLogarithmicDepth = false;
    public boolean usePerspectiveCorrectInterpolation = true;
    
    public enum DepthCurve {
        LINEAR,
        LOGARITHMIC,
        EXPONENTIAL
    }
    public DepthCurve depthCurveType = DepthCurve.LOGARITHMIC;
    
    public float cameraCollisionRadius = 0.3f;
    public float collisionResponseStrength = 1.0f;

    public float spatialGridSize = 5.0f;
    public int maxNearbyObjects = 50;
    
    public boolean enableOptimizations = true;
    public int maxPhysicsIterations = 10;
    public float physicsTimeStep = 1.0f / 60.0f;
    
    public boolean debugDraw = false;
    public boolean logMetrics = false;
    public float metricsLogInterval = 1.0f;
    
    public PhysicsConfig copy() {
        PhysicsConfig copy = new PhysicsConfig();
        copy.cameraSpeed = this.cameraSpeed;
        copy.cameraRotationSpeed = this.cameraRotationSpeed;
        copy.minCameraDistance = this.minCameraDistance;
        copy.maxCameraDistance = this.maxCameraDistance;
        copy.minHeight = this.minHeight;
        
        copy.nearPlane = this.nearPlane;
        copy.farPlane = this.farPlane;
        copy.minNearPlane = this.minNearPlane;
        copy.maxNearPlane = this.maxNearPlane;
        copy.minFarPlane = this.minFarPlane;
        copy.maxDepthRatio = this.maxDepthRatio;
        
        copy.fov = this.fov;
        copy.minFov = this.minFov;
        copy.maxFov = this.maxFov;
        copy.aspectRatio = this.aspectRatio;
        
        copy.useReverseZ = this.useReverseZ;
        copy.useLogarithmicDepth = this.useLogarithmicDepth;
        copy.usePerspectiveCorrectInterpolation = this.usePerspectiveCorrectInterpolation;
        copy.depthCurveType = this.depthCurveType;
        
        copy.cameraCollisionRadius = this.cameraCollisionRadius;
        copy.collisionResponseStrength = this.collisionResponseStrength;
        
        copy.spatialGridSize = this.spatialGridSize;
        copy.maxNearbyObjects = this.maxNearbyObjects;
        
        copy.enableOptimizations = this.enableOptimizations;
        copy.maxPhysicsIterations = this.maxPhysicsIterations;
        copy.physicsTimeStep = this.physicsTimeStep;
        
        copy.debugDraw = this.debugDraw;
        copy.logMetrics = this.logMetrics;
        copy.metricsLogInterval = this.metricsLogInterval;
        
        return copy;
    }
}