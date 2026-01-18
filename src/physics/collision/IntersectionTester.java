package physics.collision;

import math.Vector3f;

public class IntersectionTester {
    public static CollisionDetector.CollisionInfo testSphereCollision(
            Vector3f position, float radius, BoundingVolume volume, Vector3f velocity) {
        if (volume == null) return null;
        
        float dist = position.distance(volume.center);
        if (dist >= radius + volume.radius) return null;
        
        Vector3f normal = volume.center.sub(position).normalize();
        if (dist == 0) normal = velocity.normalize().mul(-1);  
        
        float penetration = radius + volume.radius - dist;
        
        float relativeSpeed = velocity.dot(normal);
        if (relativeSpeed > 0) penetration += relativeSpeed * 0.1f; 
        return new CollisionDetector.CollisionInfo(normal, penetration, null);
    }
}