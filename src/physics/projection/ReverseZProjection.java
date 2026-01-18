package physics.projection;

import math.Matrix4f;

public class ReverseZProjection {
    
    public Matrix4f createMatrix(float fovDegrees, float aspect, float near, float far) {
        // FOV передается в градусах, конвертируем в радианы
        float fovRad = (float)Math.toRadians(fovDegrees);
        float tanHalfFov = (float)Math.tan(fovRad * 0.5f);
        float f = 1.0f / tanHalfFov;
        
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, f / aspect);
        mat.set(1, 1, f);
        mat.set(2, 2, -near / (far - near)); 
        mat.set(2, 3, -far * near / (far - near));
        mat.set(3, 2, -1.0f);
        mat.set(3, 3, 0.0f);
        
        return mat;
    }
    
    public float transformDepth(float depth, float near, float far) {
        return 1.0f - (depth / far);
    }
}