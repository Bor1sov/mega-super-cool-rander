
package physics.projection;

import math.Frustum;
import math.Matrix4f;
import math.Vector3f;
import utils.PhysicsConfig;

public class PerspectiveManager {
    private final PhysicsConfig config;
    private final ReverseZProjection reverseZProjection;
    private final LogarithmicDepthBuffer logarithmicDepth;
    
    private final Frustum currentFrustum;
    
    public PerspectiveManager(PhysicsConfig config) {
        this.config = config;
        this.reverseZProjection = new ReverseZProjection();
        this.logarithmicDepth = new LogarithmicDepthBuffer();
        this.currentFrustum = new Frustum();
    }
    
    public ProjectionUpdate update(Vector3f cameraPos,
                                  Vector3f forward,
                                  Vector3f up,
                                  Vector3f right,
                                  float near,
                                  float far,
                                  float fov,
                                  float aspect) {
        Matrix4f projectionMatrix;
        String projectionType;
        
        if (config.useReverseZ) {
            projectionMatrix = reverseZProjection.createMatrix(fov, aspect, near, far);
            projectionType = "REVERSE_Z";
        } else if (config.useLogarithmicDepth) {
            projectionMatrix = logarithmicDepth.createMatrix(fov, aspect, near, far);
            projectionType = "LOGARITHMIC";
        } else {
            projectionMatrix = createStandardPerspective(fov, aspect, near, far);
            projectionType = "STANDARD";
        }
        
        currentFrustum.updateFromCamera(cameraPos, forward, up, right, fov, aspect, near, far);
        
        DepthPrecisionMetrics precisionMetrics = computeDepthPrecision(near, far);
        
        return new ProjectionUpdate(
            projectionMatrix,
            currentFrustum,
            precisionMetrics,
            projectionType
        );
    }
    
    private Matrix4f createStandardPerspective(float fov, float aspect, float near, float far) {
        float tanHalfFov = (float)Math.tan(Math.toRadians(fov) * 0.5f);
        float f = 1.0f / tanHalfFov;
        
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, f / aspect);
        mat.set(1, 1, f);
        mat.set(2, 2, (far + near) / (near - far));
        mat.set(2, 3, (2.0f * far * near) / (near - far));
        mat.set(3, 2, -1.0f);
        mat.set(3, 3, 0.0f);
        
        return mat;
    }
    
    private DepthPrecisionMetrics computeDepthPrecision(float near, float far) {
        float depthRatio = far / near;
        float precisionNear = 1.0f / near;
        float precisionFar = 1.0f / far;
        float qualityScore = Math.min(1.0f, 10000.0f / depthRatio);
        
        return new DepthPrecisionMetrics(depthRatio, precisionNear, precisionFar, qualityScore);
    }
    
    public static class ProjectionUpdate {
        public final Matrix4f projectionMatrix;
        public final Frustum frustum;
        public final DepthPrecisionMetrics precisionMetrics;
        public final String projectionType;
        
        public ProjectionUpdate(Matrix4f projectionMatrix, Frustum frustum,
                              DepthPrecisionMetrics precisionMetrics, String projectionType) {
            this.projectionMatrix = projectionMatrix;
            this.frustum = frustum;
            this.precisionMetrics = precisionMetrics;
            this.projectionType = projectionType;
        }
    }
    
    public static class DepthPrecisionMetrics {
        public final float depthRatio;
        public final float precisionNear;
        public final float precisionFar;
        public final float qualityScore;
        
        public DepthPrecisionMetrics(float depthRatio, float precisionNear,
                                   float precisionFar, float qualityScore) {
            this.depthRatio = depthRatio;
            this.precisionNear = precisionNear;
            this.precisionFar = precisionFar;
            this.qualityScore = qualityScore;
        }
    }
}
