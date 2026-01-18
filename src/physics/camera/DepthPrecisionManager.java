package physics.camera;

import math.Vector3f;
import utils.PhysicsConfig;

public class DepthPrecisionManager {
    private final PhysicsConfig config;

    public DepthPrecisionManager(PhysicsConfig config) {
        this.config = config;
    }

    public Precision optimizePrecision(Vector3f cameraPos, 
                                      Vector3f target, 
                                      float near, 
                                      float far) {
        String zBufferFormat = (far / near > 1000.0f || config.useReverseZ) 
                ? "REVERSE_FLOAT32" 
                : "STANDARD_FLOAT32";
        
        String depthCompareFunc = zBufferFormat.contains("REVERSE") 
                ? "GREATER" 
                : "LESS";
        return new Precision(zBufferFormat, depthCompareFunc);
    }

    public static class Precision {
        public final String zBufferFormat;
        public final String depthCompareFunc;

        public Precision(String zBufferFormat, String depthCompareFunc) {
            this.zBufferFormat = zBufferFormat;
            this.depthCompareFunc = depthCompareFunc;
        }
    }
}
