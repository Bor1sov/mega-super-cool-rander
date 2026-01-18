
package physics.interpolation;

import math.Vector3f;
import math.MathUtils;

public class BarycentricComputer {
    
    public float[] computeBarycentric2D(float x, float y, 
                                       Vector3f v0, Vector3f v1, Vector3f v2) {
        float[] barycentric = new float[3];
        
        float area = edgeFunction(v0, v1, v2);
        if (Math.abs(area) < MathUtils.EPSILON) {
            barycentric[0] = barycentric[1] = barycentric[2] = 1.0f / 3.0f;
            return barycentric;
        }
        
        float invArea = 1.0f / area;
        Vector3f p = new Vector3f(x, y, 0);
        
        barycentric[0] = edgeFunction(v1, v2, p) * invArea;
        barycentric[1] = edgeFunction(v2, v0, p) * invArea;
        barycentric[2] = edgeFunction(v0, v1, p) * invArea;
        
        return barycentric;
    }
    
    public float[] computeBarycentric3D(Vector3f point,
                                       Vector3f v0, Vector3f v1, Vector3f v2) {
        float[] barycentric = new float[3];
        
        Vector3f v0v1 = v1.sub(v0);
        Vector3f v0v2 = v2.sub(v0);
        Vector3f v0p = point.sub(v0);
        
        float d00 = v0v1.dot(v0v1);
        float d01 = v0v1.dot(v0v2);
        float d11 = v0v2.dot(v0v2);
        float d20 = v0p.dot(v0v1);
        float d21 = v0p.dot(v0v2);
        
        float denom = d00 * d11 - d01 * d01;
        if (Math.abs(denom) < MathUtils.EPSILON) {
            barycentric[0] = barycentric[1] = barycentric[2] = 1.0f / 3.0f;
            return barycentric;
        }
        
        float invDenom = 1.0f / denom;
        barycentric[1] = (d11 * d20 - d01 * d21) * invDenom;
        barycentric[2] = (d00 * d21 - d01 * d20) * invDenom;
        barycentric[0] = 1.0f - barycentric[1] - barycentric[2];
        
        return barycentric;
    }
    
    public boolean isPointInTriangle(float[] barycentric) {
        return barycentric[0] >= -MathUtils.EPSILON && 
               barycentric[1] >= -MathUtils.EPSILON && 
               barycentric[2] >= -MathUtils.EPSILON &&
               barycentric[0] + barycentric[1] + barycentric[2] <= 1.0f + MathUtils.EPSILON;
    }
    
    public boolean isPointInTriangle(float x, float y,
                                    Vector3f v0, Vector3f v1, Vector3f v2) {
        float[] barycentric = computeBarycentric2D(x, y, v0, v1, v2);
        return isPointInTriangle(barycentric);
    }
    
    private float edgeFunction(Vector3f a, Vector3f b, Vector3f c) {
        return (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
    }
    
    public Vector3f interpolateAttribute(Vector3f attr0, Vector3f attr1, Vector3f attr2,
                                        float[] barycentric) {
        return attr0.mul(barycentric[0])
                   .add(attr1.mul(barycentric[1]))
                   .add(attr2.mul(barycentric[2]));
    }
    
    public float interpolateAttribute(float attr0, float attr1, float attr2,
                                     float[] barycentric) {
        return attr0 * barycentric[0] + attr1 * barycentric[1] + attr2 * barycentric[2];
    }
}
