
package physics.interpolation;

import math.Vector3f;
import math.Vector4f;
import utils.PhysicsConfig;

public class DepthInterpolator {
    private final PhysicsConfig config;
    private final PerspectiveCorrect perspectiveCorrect;
    
    public DepthInterpolator(PhysicsConfig config) {
        this.config = config;
        this.perspectiveCorrect = new PerspectiveCorrect();
    }
    
    public DepthInterpolationData prepareInterpolationData(
        Vector3f cameraPos,
        Vector3f[] objectPositions,
        float[] objectRadii,
        boolean[] visibilityFlags
    ) {
        int visibleCount = 0;
        for (boolean visible : visibilityFlags) {
            if (visible) visibleCount++;
        }
        
        float[] depths = new float[objectPositions.length];
        float[] distances = new float[objectPositions.length];
        float minDistance = Float.MAX_VALUE;
        float maxDistance = 0;
        
        for (int i = 0; i < objectPositions.length; i++) {
            if (visibilityFlags[i]) {
                float distance = cameraPos.distance(objectPositions[i]);
                distances[i] = distance;
                depths[i] = distance - objectRadii[i];
                
                minDistance = Math.min(minDistance, distance);
                maxDistance = Math.max(maxDistance, distance);
            } else {
                depths[i] = 0.0f;
                distances[i] = 0.0f;
            }
        }
        
        float[] interpolationFactors = computeInterpolationFactors(distances, minDistance, maxDistance);
        
        return new DepthInterpolationData(
            depths,
            distances,
            interpolationFactors,
            minDistance,
            maxDistance,
            visibleCount
        );
    }
    
    private float[] computeInterpolationFactors(float[] distances, float minDist, float maxDist) {
        float[] factors = new float[distances.length];
        float range = maxDist - minDist;
        if (range < 0.001f) range = 1.0f;
        
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] > 0.01f) {
                float t = (distances[i] - minDist) / range;
                factors[i] = applyDepthCurve(t);
            } else {
                factors[i] = 0.0f;
            }
        }
        
        return factors;
    }
    
    private float applyDepthCurve(float t) {
        return (float) (switch (config.depthCurveType) {
            case LOGARITHMIC -> Math.log1p(t * 9.0f) / Math.log(10.0f);
            case EXPONENTIAL -> t * t;
            default -> t;
        });
    }
    
    public float interpolateDepth(float[] barycentric,
                                 float[] vertexDepths,
                                 Vector4f[] clipPositions) {
        if (config.usePerspectiveCorrectInterpolation && clipPositions != null) {
            return perspectiveCorrect.interpolateDepth(barycentric, vertexDepths, clipPositions);
        } else {
            return barycentric[0] * vertexDepths[0] +
                   barycentric[1] * vertexDepths[1] +
                   barycentric[2] * vertexDepths[2];
        }
    }
    
    public static class DepthInterpolationData {
        public final float[] depths;
        public final float[] distances;
        public final float[] interpolationFactors;
        public final float minDistance;
        public final float maxDistance;
        public final int visibleCount;
        
        public DepthInterpolationData(float[] depths, float[] distances,
                                    float[] interpolationFactors,
                                    float minDistance, float maxDistance,
                                    int visibleCount) {
            this.depths = depths;
            this.distances = distances;
            this.interpolationFactors = interpolationFactors;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.visibleCount = visibleCount;
        }
    }
}
