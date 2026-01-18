package test.physics;

import math.Vector3f;
import physics.camera.AdaptiveClipping;
import physics.camera.CameraPhysics;
import utils.PhysicsConfig;

public class CameraCollisionTest {
    
    private static int passed = 0;
    private static int failed = 0;
    
    private static void assertTrue(boolean condition, String message) {
        if (condition) {
            System.out.println("[PASSED] " + message);
            passed++;
        } else {
            System.out.println("[FAILED] " + message);
            failed++;
        }
    }
    
    private static void assertEquals(float expected, float actual, float delta, String message) {
        boolean ok = Math.abs(expected - actual) <= delta;
        if (ok) {
            System.out.println("[PASSED] " + message);
            passed++;
        } else {
            System.out.println("[FAILED] " + message + " (expected: " + expected + ", actual: " + actual + ")");
            failed++;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Запуск тестов CameraCollisionTest ===\n");
        
        testCameraMovementConstraints();
        testAdaptiveClippingNearPlane();
        testCameraVectorsOrthogonality();
        testClippingPlaneStability();
        
        System.out.println("\n=== Итог: " + passed + " пройдено, " + failed + " провалено ===");
    }
    
    private static void testCameraMovementConstraints() {
        System.out.println("Тест: testCameraMovementConstraints");
        
        PhysicsConfig config = new PhysicsConfig();
        config.minHeight = 0.0f;
        
        CameraPhysics cameraPhysics = new CameraPhysics(config);
        cameraPhysics.initialize(new Vector3f(0, 2, 10), new Vector3f(0, 0, 0));
        
        Vector3f belowGround = new Vector3f(0, -1, 10);
        
        CameraPhysics.CameraUpdate update = cameraPhysics.update(
            1.0f / 60.0f, belowGround, config.minNearPlane, config.minFarPlane);
        
        assertEquals(config.minHeight, update.position.y, 0.01f, "Ограничение по минимальной высоте");
    }
    
    private static void testAdaptiveClippingNearPlane() {
        System.out.println("Тест: testAdaptiveClippingNearPlane");
        
        PhysicsConfig config = new PhysicsConfig();
        AdaptiveClipping adaptiveClipping = new AdaptiveClipping(config);
        
        AdaptiveClipping.ClipResult result = adaptiveClipping.computeClippingPlanes(
            new Vector3f(0, 2, 1), new Vector3f(0, 0, 0),
            config.nearPlane, config.farPlane);
        
        assertTrue(result.near <= config.minNearPlane * 2.0f, "Near должен быть маленьким при близости");
        assertTrue(result.near >= config.minNearPlane, "Near не ниже минимума");
        assertTrue(result.far / result.near <= config.maxDepthRatio * 1.1f, "Соотношение far/near в пределах");
    }
    
    private static void testCameraVectorsOrthogonality() {
        System.out.println("Тест: testCameraVectorsOrthogonality");
        
        PhysicsConfig config = new PhysicsConfig();
        CameraPhysics cameraPhysics = new CameraPhysics(config);
        cameraPhysics.initialize(new Vector3f(0, 2, 10), new Vector3f(0, 0, 0));
        
        Vector3f desiredPos = new Vector3f(0, 3, 8);
        CameraPhysics.CameraUpdate update = cameraPhysics.update(
            1.0f / 60.0f, desiredPos, config.minNearPlane, config.minFarPlane);
        
        Vector3f forward = update.forward;
        Vector3f up = update.up;
        Vector3f right = update.right;
        
        assertEquals(0.0f, forward.dot(right), 0.001f, "Ортогональность forward-right");
        assertEquals(0.0f, forward.dot(up), 0.001f, "Ортогональность forward-up");
        assertEquals(0.0f, right.dot(up), 0.001f, "Ортогональность right-up");
        
        assertEquals(1.0f, forward.length(), 0.001f, "Нормализация forward");
        assertEquals(1.0f, right.length(), 0.001f, "Нормализация right");
        assertEquals(1.0f, up.length(), 0.001f, "Нормализация up");
        
        Vector3f cross = right.cross(up);
        assertTrue(cross.dot(forward) > 0.99f, "Правая система координат");
    }
    
    private static void testClippingPlaneStability() {
        System.out.println("Тест: testClippingPlaneStability");
        
        PhysicsConfig config = new PhysicsConfig();
        AdaptiveClipping adaptiveClipping = new AdaptiveClipping(config);
        
        float previousNear = 0.0f;
        float previousFar = 0.0f;
        
        for (int i = 0; i < 10; i++) {
            float z = 20.0f - i * 1.5f;
            Vector3f cameraPos = new Vector3f(0, 2, z);
            
            AdaptiveClipping.ClipResult result = adaptiveClipping.computeClippingPlanes(
                cameraPos, new Vector3f(0, 0, 0), config.nearPlane, config.farPlane);
            
            if (i > 0) {
                float nearChange = Math.abs(result.near - previousNear);
                assertTrue(nearChange < 0.2f, "Стабильность near (изменение: " + nearChange + ")");
                
                float farChange = Math.abs(result.far - previousFar);
                assertTrue(farChange < 5.0f, "Стабильность far (изменение: " + farChange + ")");
            }
            
            previousNear = result.near;
            previousFar = result.far;
        }
    }
}