package math;

public class Vector4f {
    public float x, y, z, w;
    
    public Vector4f(float x, float y, float z, float w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }
    
    public Vector4f(Vector3f v, float w) {
        this.x = v.x; this.y = v.y; this.z = v.z; this.w = w;
    }
    
    public Vector4f add(Vector4f other) {
        return new Vector4f(x + other.x, y + other.y, z + other.z, w + other.w);
    }
    
    public Vector4f sub(Vector4f other) {
        return new Vector4f(x - other.x, y - other.y, z - other.z, w - other.w);
    }
    
    public Vector4f mul(float scalar) {
        return new Vector4f(x * scalar, y * scalar, z * scalar, w * scalar);
    }
    
    public Vector4f div(float scalar) {
        if (scalar == 0) return new Vector4f(0, 0, 0, 0);
        return new Vector4f(x / scalar, y / scalar, z / scalar, w / scalar);
    }
    
    public float dot(Vector4f other) {
        return x*other.x + y*other.y + z*other.z + w*other.w;
    }
    
    public float length() {
        return (float)Math.sqrt(x*x + y*y + z*z + w*w);
    }
    
    public Vector4f normalize() {
        float len = length();
        if (len == 0) return new Vector4f(0, 0, 0, 0);
        return new Vector4f(x/len, y/len, z/len, w/len);
    }
    
    public Vector3f xyz() {
        return new Vector3f(x, y, z);
    }
    
    public Vector4f perspectiveDivide() {
        if (w == 0) return new Vector4f(x, y, z, 0);
        return new Vector4f(x/w, y/w, z/w, 1);
    }
}