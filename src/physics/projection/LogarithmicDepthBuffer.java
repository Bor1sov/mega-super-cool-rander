package physics.projection;

import math.Matrix4f;


public class LogarithmicDepthBuffer {
    
    public Matrix4f createMatrix(float fov, float aspect, float near, float far) {
        float tanHalfFov = (float)Math.tan(fov * 0.5f);
        float f = 1.0f / tanHalfFov;
        
        Matrix4f mat = new Matrix4f();
        
        mat.set(0, 0, f / aspect);
        mat.set(1, 1, f);
        
        float C = 1.0f;
        
        mat.set(2, 2, 1.0f / (float)Math.log(C * far + 1));
        mat.set(2, 3, 0);
        mat.set(3, 2, C / (float)Math.log(C * far + 1));
        mat.set(3, 3, 1.0f);
        
        return mat;
    }
    
    public float transformDepth(float linearDepth, float near, float far) {

        float C = 1.0f;
        return (float)(Math.log(C * linearDepth + 1) / Math.log(C * far + 1));
    }
    
    public float inverseTransformDepth(float logDepth, float near, float far) {
        float C = 1.0f;
        return (float)((Math.exp(logDepth * Math.log(C * far + 1)) - 1) / C);
    }
    
    public float computeDepthPrecision(float linearDepth, float near, float far) {
  
        float C = 1.0f;
        float derivative = C / ((C * linearDepth + 1) * (float)Math.log(C * far + 1));
    
        return derivative;
    }
    
    public float getOptimalC(float near, float far, float targetPrecisionAtNear) {
       
        float C = 1.0f;
        float step = 0.1f;
        float bestC = C;
        float bestError = Float.MAX_VALUE;
        
        for (int i = 0; i < 100; i++) {
            float precision = computeDepthPrecision(near, near, far);
            float error = Math.abs(precision - targetPrecisionAtNear);
            
            if (error < bestError) {
                bestError = error;
                bestC = C;
            }
            
            if (precision > targetPrecisionAtNear) {
                C += step;
            } else {
                C -= step;
            }
            
            step *= 0.95f;
        }
        
        return bestC;
    }
}