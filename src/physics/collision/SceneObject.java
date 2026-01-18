package physics.collision;

import math.Vector3f;

public class SceneObject {
    private final int id;
    private final Vector3f position;
    private final float radius;
    private final BoundingVolume boundingVolume;
    
    public SceneObject(int id, Vector3f position, float radius) {
        this.id = id;
        this.position = position;
        this.radius = radius;
        this.boundingVolume = BoundingVolume.createSphere(position, radius);
    }
    
    public int getId() { return id; }
    public Vector3f getPosition() { return position; }
    public float getRadius() { return radius; }
    public BoundingVolume getBoundingVolume() { return boundingVolume; }
}