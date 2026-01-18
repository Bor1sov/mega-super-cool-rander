package physics.camera;

import math.Matrix4f;
import math.Vector3f;
import utils.PhysicsConfig;

public class CameraPhysics {
    private Vector3f position;
    private Vector3f target;
    private Vector3f forward;
    private Vector3f up;
    private Vector3f right;
    
    private float currentNear;
    private float currentFar;
    private float movementSpeed = 5.0f;
    
    private final PhysicsConfig config;
    private final AdaptiveClipping adaptiveClipping;
    private final DepthPrecisionManager depthPrecisionManager;
    
    public CameraPhysics(PhysicsConfig config) {
        this.config = config;
        this.adaptiveClipping = new AdaptiveClipping(config);
        this.depthPrecisionManager = new DepthPrecisionManager(config);
    }
    
    public void initialize(Vector3f initialPosition, Vector3f initialTarget) {
        this.position = initialPosition;
        this.target = initialTarget;
        updateVectors();
        
        currentNear = config.nearPlane;
        currentFar = config.farPlane;
    }
    
    public CameraUpdate update(float deltaTime, 
                              Vector3f desiredPosition,
                              float adaptiveNear,
                              float adaptiveFar) {

        Vector3f newPosition = smoothMove(position, desiredPosition, deltaTime);
        
        updateVectors();

        AdaptiveClipping.ClipResult clipResult = adaptiveClipping.computeClippingPlanes(
            newPosition,
            target,
            adaptiveNear,
            adaptiveFar
        );
        
        currentNear = clipResult.near;
        currentFar = clipResult.far;
        
        DepthPrecisionManager.Precision precision = depthPrecisionManager.optimizePrecision(
            newPosition,
            target,
            currentNear,
            currentFar
        );
        
        Vector3f direction = target.sub(newPosition).normalize();
        if (newPosition.distance(target) < config.minCameraDistance) {
            position = target.add(direction.mul(config.minCameraDistance));
        } else {
            position = newPosition;
        }
        
        String zBufferFormat = precision.zBufferFormat;
        String depthCompareFunc = precision.depthCompareFunc;
        
        return new CameraUpdate(position, target, forward, up, right,
                                currentNear, currentFar,
                                zBufferFormat, depthCompareFunc);
    }
    
    private Vector3f smoothMove(Vector3f current, Vector3f desired, float deltaTime) {
        float speed = movementSpeed * deltaTime;
        Vector3f diff = desired.sub(current);
        float dist = diff.length();
        if (dist <= speed) return desired;
        return current.add(diff.normalize().mul(speed));
    }
    
    private void updateVectors() {
        forward = target.sub(position).normalize();
        right = forward.cross(new Vector3f(0, 1, 0)).normalize();
        up = right.cross(forward).normalize();
    }
    
    public Matrix4f getViewMatrix() {
        Vector3f f = forward;
        Vector3f r = right;
        Vector3f u = up;
        
        Matrix4f view = new Matrix4f();
        view.set(0, 0, r.x); view.set(0, 1, r.y); view.set(0, 2, r.z);
        view.set(1, 0, u.x); view.set(1, 1, u.y); view.set(1, 2, u.z);
        view.set(2, 0, -f.x); view.set(2, 1, -f.y); view.set(2, 2, -f.z);
        
        view.set(0, 3, -r.dot(position));
        view.set(1, 3, -u.dot(position));
        view.set(2, 3, f.dot(position));
        
        return view;
    }
    
    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
    public Vector3f getForward() { return forward; }
    public float getCurrentNear() { return currentNear; }
    public float getCurrentFar() { return currentFar; }
    
    public void setTarget(Vector3f newTarget) {
        this.target = newTarget;
        updateVectors();
    }
    
    public void setConfig(PhysicsConfig config) {
        this.movementSpeed = config.cameraSpeed;
    }
    
    public static class CameraUpdate {
        public final Vector3f position;
        public final Vector3f target;
        public final Vector3f forward;
        public final Vector3f up;
        public final Vector3f right;
        public final float near;
        public final float far;
        public final String zBufferFormat;
        public final String depthCompareFunc;
        
        public CameraUpdate(Vector3f position, Vector3f target,
                          Vector3f forward, Vector3f up, Vector3f right,
                          float near, float far,
                          String zBufferFormat, String depthCompareFunc) {
            this.position = position;
            this.target = target;
            this.forward = forward;
            this.up = up;
            this.right = right;
            this.near = near;
            this.far = far;
            this.zBufferFormat = zBufferFormat;
            this.depthCompareFunc = depthCompareFunc;
        }
    }
}
