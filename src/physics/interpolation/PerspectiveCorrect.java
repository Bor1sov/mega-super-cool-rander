package physics.interpolation;

import math.MathUtils;
import math.Vector3f;
import math.Vector4f;

public class PerspectiveCorrect {
    
    public float interpolateDepth(float[] barycentric,
                                 float[] vertexDepths,
                                 Vector4f[] clipPositions) {
        float[] correctedBarycentric = correctBarycentric(barycentric, clipPositions);
        
        return correctedBarycentric[0] * vertexDepths[0] +
               correctedBarycentric[1] * vertexDepths[1] +
               correctedBarycentric[2] * vertexDepths[2];
    }
    
    public Vector3f interpolateAttribute(Vector3f attr0, Vector3f attr1, Vector3f attr2,
                                        float[] barycentric,
                                        Vector4f[] clipPositions) {
        float[] correctedBarycentric = correctBarycentric(barycentric, clipPositions);
        
        return new Vector3f(
            attr0.x * correctedBarycentric[0] + attr1.x * correctedBarycentric[1] + attr2.x * correctedBarycentric[2],
            attr0.y * correctedBarycentric[0] + attr1.y * correctedBarycentric[1] + attr2.y * correctedBarycentric[2],
            attr0.z * correctedBarycentric[0] + attr1.z * correctedBarycentric[1] + attr2.z * correctedBarycentric[2]
        );
    }
    
    public float interpolateAttribute(float attr0, float attr1, float attr2,
                                     float[] barycentric,
                                     Vector4f[] clipPositions) {
        float[] correctedBarycentric = correctBarycentric(barycentric, clipPositions);
        
        return attr0 * correctedBarycentric[0] + 
               attr1 * correctedBarycentric[1] + 
               attr2 * correctedBarycentric[2];
    }
    
    private float[] correctBarycentric(float[] barycentric, Vector4f[] clipPositions) {
        float[] corrected = new float[3];
        
        float w0 = clipPositions[0].w;
        float w1 = clipPositions[1].w;
        float w2 = clipPositions[2].w;
        
        float denom = barycentric[0] / w0 + barycentric[1] / w1 + barycentric[2] / w2;
        
        if (Math.abs(denom) < MathUtils.EPSILON) {
            corrected[0] = corrected[1] = corrected[2] = 1.0f / 3.0f;
            return corrected;
        }
        
        corrected[0] = (barycentric[0] / w0) / denom;
        corrected[1] = (barycentric[1] / w1) / denom;
        corrected[2] = (barycentric[2] / w2) / denom;
        
        return corrected;
    }
    
    public float computeScreenSpaceDepth(float worldDepth,
                                        float near, float far,
                                        boolean useReverseZ) {
        if (useReverseZ) {
            return 1.0f - ((worldDepth - near) / (far - near));
        } else {
            return (worldDepth - near) / (far - near);
        }
    }
    
    public float computeLinearDepth(float screenDepth,
                                   float near, float far,
                                   boolean useReverseZ) {
        if (useReverseZ) {
            return near + (1.0f - screenDepth) * (far - near);
        } else {
            return near + screenDepth * (far - near);
        }
    }
}