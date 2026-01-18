package math;


public class Frustum {
    private final Plane[] planes = new Plane[6];
    
    public enum PlaneType {
        NEAR, FAR, LEFT, RIGHT, TOP, BOTTOM
    }
    
    public Frustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Plane();
        }
    }
    
    public void updateFromCamera(Vector3f position, Vector3f forward, 
                                Vector3f up, Vector3f right,
                                float fov, float aspect, 
                                float near, float far) {
        float tanFov = (float)Math.tan(fov * 0.5f);
        float nearHeight = 2.0f * tanFov * near;
        float nearWidth = nearHeight * aspect;
        
        Vector3f nearCenter = position.add(forward.mul(near));
        Vector3f farCenter = position.add(forward.mul(far));
        
        planes[PlaneType.NEAR.ordinal()].set(
            forward, 
            forward.dot(nearCenter)
        );
        
        planes[PlaneType.FAR.ordinal()].set(
            forward.mul(-1), 
            -forward.dot(farCenter)
        );
        
        Vector3f rightNormal = forward.cross(up).normalize();
        Vector3f rightPoint = position.add(right.mul(nearWidth * 0.5f));
        planes[PlaneType.RIGHT.ordinal()].set(
            rightNormal,
            rightNormal.dot(rightPoint)
        );
        
        Vector3f leftNormal = up.cross(forward).normalize();
        Vector3f leftPoint = position.add(right.mul(-nearWidth * 0.5f));
        planes[PlaneType.LEFT.ordinal()].set(
            leftNormal,
            leftNormal.dot(leftPoint)
        );

        Vector3f topNormal = right.cross(forward).normalize();
        Vector3f topPoint = position.add(up.mul(nearHeight * 0.5f));
        planes[PlaneType.TOP.ordinal()].set(
            topNormal,
            topNormal.dot(topPoint)
        );

        Vector3f bottomNormal = forward.cross(right).normalize();
        Vector3f bottomPoint = position.add(up.mul(-nearHeight * 0.5f));
        planes[PlaneType.BOTTOM.ordinal()].set(
            bottomNormal,
            bottomNormal.dot(bottomPoint)
        );
    }
    
    public boolean containsPoint(Vector3f point) {
        for (Plane plane : planes) {
            if (plane.distance(point) < 0) {
                return false;
            }
        }
        return true;
    }
    
    public boolean intersectsSphere(Vector3f center, float radius) {
        for (Plane plane : planes) {
            float distance = plane.distance(center);
            if (distance < -radius) {
                return false;
            }
        }
        return true;
    }
}