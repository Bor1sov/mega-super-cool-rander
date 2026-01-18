package math;

public class Plane {
    public Vector3f normal;
    public float distance;
    
    public Plane() {
        this.normal = new Vector3f(0, 1, 0);
        this.distance = 0;
    }
    
    public Plane(Vector3f normal, float distance) {
        this.normal = normal.normalize();
        this.distance = distance;
    }
    
    public Plane(Vector3f point, Vector3f normal) {
        this.normal = normal.normalize();
        this.distance = -this.normal.dot(point);
    }
    
    public void set(Vector3f normal, float distance) {
        this.normal = normal.normalize();
        this.distance = distance;
    }
    
    public float distance(Vector3f point) {
        return normal.dot(point) + distance;
    }
    
    public Vector3f closestPoint(Vector3f point) {
        float dist = distance(point);
        return point.sub(normal.mul(dist));
    }
    
    public boolean isPointOnPositiveSide(Vector3f point) {
        return distance(point) >= 0;
    }
    
    public Plane normalize() {
        float length = normal.length();
        if (length == 0) return this;
        return new Plane(normal.div(length), distance / length);
    }
}