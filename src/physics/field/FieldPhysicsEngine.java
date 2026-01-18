
package physics.field;

import math.Frustum;
import math.Vector3f;
import physics.camera.CameraPhysics;
import physics.camera.CameraPhysics.CameraUpdate;
import physics.collision.CollisionDetector;
import physics.collision.CollisionDetector.CollisionResolution;
import physics.interpolation.DepthInterpolator;
import physics.projection.PerspectiveManager;
import utils.PerformanceMonitor; 
import utils.PhysicsConfig;

public class FieldPhysicsEngine {
    private final CameraPhysics cameraPhysics;
    private final CollisionDetector collisionDetector;
    private final PerspectiveManager perspectiveManager;
    private final DepthInterpolator depthInterpolator;
    private final SpatialPartition spatialPartition;
    private final AdaptiveMetrics adaptiveMetrics;
    private final PerformanceMonitor performanceMonitor;
    
    private PhysicsConfig config;
    private boolean isInitialized = false;
    
    public FieldPhysicsEngine(PhysicsConfig config) {
        this.config = config;
        this.cameraPhysics = new CameraPhysics(config);
        this.collisionDetector = new CollisionDetector(config);
        this.perspectiveManager = new PerspectiveManager(config);
        this.depthInterpolator = new DepthInterpolator(config);
        this.spatialPartition = new SpatialPartition(config);
        this.adaptiveMetrics = new AdaptiveMetrics(config);
        this.performanceMonitor = new PerformanceMonitor();
    }
    
    public void initialize(Vector3f initialCameraPos, Vector3f initialTarget) {
        cameraPhysics.initialize(initialCameraPos, initialTarget);
        spatialPartition.initialize();
        isInitialized = true;
    }
    
    public PhysicsUpdateResult update(float deltaTime, 
                                     Vector3f desiredCameraPos,
                                     Vector3f[] objectPositions,
                                     float[] objectRadii) {
        performanceMonitor.startFrame();
        
        if (!isInitialized) {
            throw new IllegalStateException("Engine not initialized");
        }
        
        spatialPartition.update(objectPositions, objectRadii);
        
        adaptiveMetrics.update(cameraPhysics.getPosition(), objectPositions);
        
        Vector3f currentPos = cameraPhysics.getPosition();
        Vector3f cameraVelocity = desiredCameraPos.sub(currentPos).mul(1.0f / deltaTime);
        
        CollisionResolution collisionResult = collisionDetector.resolveCameraCollision(
            desiredCameraPos,
            currentPos,
            spatialPartition.getNearbyObjects(currentPos),
            objectPositions,
            objectRadii,
            cameraVelocity
        );
        
        CameraUpdate cameraUpdate = cameraPhysics.update(
            deltaTime,
            collisionResult.adjustedPosition,
            adaptiveMetrics.getCurrentNearPlane(),
            adaptiveMetrics.getCurrentFarPlane()
        );
        
        PerspectiveManager.ProjectionUpdate projectionUpdate = perspectiveManager.update(
            cameraUpdate.position,
            cameraUpdate.forward,
            cameraUpdate.up,
            cameraUpdate.right,
            cameraUpdate.near,
            cameraUpdate.far,
            config.fov,
            config.aspectRatio
        );
        
        Frustum currentFrustum = projectionUpdate.frustum;
        boolean[] visibilityFlags = new boolean[objectPositions.length];
        for (int i = 0; i < objectPositions.length; i++) {
            visibilityFlags[i] = currentFrustum.intersectsSphere(
                objectPositions[i], 
                objectRadii[i]
            );
        }
        
        DepthInterpolator.DepthInterpolationData depthData = depthInterpolator.prepareInterpolationData(
            cameraUpdate.position,
            objectPositions,
            objectRadii,
            visibilityFlags
        );
        
        performanceMonitor.endFrame();
        
        return new PhysicsUpdateResult(
            cameraUpdate,
            projectionUpdate,
            collisionResult,
            visibilityFlags,
            depthData,
            adaptiveMetrics.getMetrics(),
            performanceMonitor.getFrameStats()
        );
    }
    
    public void setConfig(PhysicsConfig newConfig) {
        this.config = newConfig.copy();
        cameraPhysics.setConfig(newConfig);
    }
    
    public static class PhysicsUpdateResult {
        public final CameraUpdate cameraUpdate;
        public final PerspectiveManager.ProjectionUpdate projectionUpdate;
        public final CollisionResolution collisionResult;
        public final boolean[] visibilityFlags;
        public final DepthInterpolator.DepthInterpolationData depthData;
        public final AdaptiveMetrics.Metrics metrics;
        public final PerformanceMonitor.FrameStats frameStats;
        
        public PhysicsUpdateResult(CameraUpdate cameraUpdate,
                                 PerspectiveManager.ProjectionUpdate projectionUpdate,
                                 CollisionResolution collisionResult,
                                 boolean[] visibilityFlags,
                                 DepthInterpolator.DepthInterpolationData depthData,
                                 AdaptiveMetrics.Metrics metrics,
                                 PerformanceMonitor.FrameStats frameStats) {
            this.cameraUpdate = cameraUpdate;
            this.projectionUpdate = projectionUpdate;
            this.collisionResult = collisionResult;
            this.visibilityFlags = visibilityFlags;
            this.depthData = depthData;
            this.metrics = metrics;
            this.frameStats = frameStats;
        }
    }
}
