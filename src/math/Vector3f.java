package math;

public class Vector3f {
    public float x, y, z;
    
    public Vector3f() {
        this(0, 0, 0);
    }
    
    public Vector3f(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }
    
    public Vector3f add(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector3f sub(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3f mul(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }
 
    public Vector3f div(float scalar) {
        if (scalar == 0.0f || Math.abs(scalar) < MathUtils.EPSILON) {
            return new Vector3f(0, 0, 0); 
        }
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }
    
    public float length() {
        return (float)Math.sqrt(x*x + y*y + z*z);
    }
    
    public float lengthSquared() {
        return x*x + y*y + z*z;
    }
    
    public Vector3f normalize() {
        float len = length();
        if (len < MathUtils.EPSILON) {
            return new Vector3f(0, 0, 0);
        }
        return this.div(len); 
    }
    
    public float dot(Vector3f other) {
        return x*other.x + y*other.y + z*other.z;
    }
    
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
            y*other.z - z*other.y,
            z*other.x - x*other.z,
            x*other.y - y*other.x
        );
    }
    
    public float distance(Vector3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    public float distanceSquared(Vector3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f)", x, y, z);
    }
}
