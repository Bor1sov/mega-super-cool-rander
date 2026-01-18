package physics.collision;
import java.util.List;
import math.Vector3f;
import utils.PhysicsConfig;

public class CollisionDetector {
    private final PhysicsConfig config;

    public CollisionDetector(PhysicsConfig config) {
        this.config = config;
    }

    public CollisionResolution resolveCameraCollision(
            Vector3f desiredPosition,
            Vector3f currentPosition,
            List<Integer> nearbyObjectIndices,
            Vector3f[] objectPositions,
            float[] objectRadii,
            Vector3f cameraVelocity) {

        Vector3f position = desiredPosition;
        CollisionInfo deepestCollision = null;
        for (int iteration = 0; iteration < config.maxPhysicsIterations; iteration++) {
            deepestCollision = null;

            for (int index : nearbyObjectIndices) {
                if (index < 0 || index >= objectPositions.length) continue;

                BoundingVolume volume = BoundingVolume.createSphere(
                        objectPositions[index], objectRadii[index]);

                CollisionInfo info = IntersectionTester.testSphereCollision(
                        position, config.cameraCollisionRadius, volume, cameraVelocity);

                if (info != null && 
                    (deepestCollision == null || info.penetration > deepestCollision.penetration)) {
                    deepestCollision = info;
                }
            }

            if (deepestCollision == null) break;

            position = resolvePenetration(position, deepestCollision);
        }

        if (position.y < config.minHeight) {
            position = new Vector3f(position.x, config.minHeight, position.z);
        }

        return new CollisionResolution(
                position,
                deepestCollision != null,
                deepestCollision
        );
    }

    private Vector3f resolvePenetration(Vector3f position, CollisionInfo collision) {
        float elasticity = config.collisionResponseStrength;
        float separation = collision.penetration * elasticity + 0.05f;
        return position.add(collision.normal.mul(separation));
    }

    public static class CollisionResolution {
        public final Vector3f adjustedPosition;
        public final boolean hasCollision;
        public final CollisionInfo collisionInfo;

        public CollisionResolution(Vector3f adjustedPosition, 
                                  boolean hasCollision,
                                  CollisionInfo collisionInfo) {
            this.adjustedPosition = adjustedPosition;
            this.hasCollision = hasCollision;
            this.collisionInfo = collisionInfo;
        }
    }

    public static class CollisionInfo {
        public final Vector3f normal;
        public final float penetration;
        public final Object object; 
        public CollisionInfo(Vector3f normal, float penetration, Object object) {
            this.normal = normal;
            this.penetration = penetration;
            this.object = object;
        }
    }
}
