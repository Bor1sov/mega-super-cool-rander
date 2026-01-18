package physics.collision;

import math.Vector3f;

public class BoundingVolume {
    public Vector3f center;
    public float radius;

    public BoundingVolume(Vector3f center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public static BoundingVolume createSphere(Vector3f center, float radius) {
        return new BoundingVolume(center, radius);
    }
}