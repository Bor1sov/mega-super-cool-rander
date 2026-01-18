package test.physics;

import math.Vector3f;
import physics.field.FieldPhysicsEngine;
import physics.field.FieldPhysicsEngine.PhysicsUpdateResult;
import utils.PhysicsConfig;

public class FieldPhysicsTest {
    
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
        System.out.println("=== Запуск тестов FieldPhysicsTest ===\n");
        
        testInitialization();
        testCameraCollision();
        testSmoothTransitions();
        
        System.out.println("\n=== Итог: " + passed + " пройдено, " + failed + " провалено ===");
    }
    
    private static void testInitialization() {
        System.out.println("Тест: testInitialization");
        
        PhysicsConfig config = new PhysicsConfig();
        config.useReverseZ = true;
        config.cameraSpeed = 3.0f;
        config.cameraCollisionRadius = 0.3f;
        
        FieldPhysicsEngine physicsEngine = new FieldPhysicsEngine(config);
        
        Vector3f cameraStart = new Vector3f(0, 2, 10);
        Vector3f target = new Vector3f(0, 0, 0);
        
        physicsEngine.initialize(cameraStart, target);
        
        PhysicsUpdateResult result = physicsEngine.update(1.0f / 60.0f, cameraStart,
                new Vector3f[0], new float[0]);
        
        assertEquals(cameraStart.x, result.cameraUpdate.position.x, 0.01f, "Инициализация X");
        assertEquals(cameraStart.y, result.cameraUpdate.position.y, 0.01f, "Инициализация Y");
        assertEquals(cameraStart.z, result.cameraUpdate.position.z, 0.01f, "Инициализация Z");
    }
    
    private static void testCameraCollision() {
        System.out.println("Тест: testCameraCollision");
        
        PhysicsConfig config = new PhysicsConfig();
        config.useReverseZ = true;
        config.cameraSpeed = 3.0f;
        config.cameraCollisionRadius = 0.3f;
        
        FieldPhysicsEngine physicsEngine = new FieldPhysicsEngine(config);
        
        Vector3f cameraStart = new Vector3f(0, 2, 10);
        Vector3f target = new Vector3f(0, 0, 0);
        physicsEngine.initialize(cameraStart, target);
        
        Vector3f[] objects = new Vector3f[1];
        float[] radii = new float[1];
        objects[0] = new Vector3f(0, 2, 5);
        radii[0] = 2.0f;
        
        Vector3f desiredPos = new Vector3f(0, 2, 4);
        
        PhysicsUpdateResult result = physicsEngine.update(1.0f / 60.0f, desiredPos, objects, radii);
        
        assertTrue(result.collisionResult.hasCollision, "Должна быть коллизия");
        
        float minAllowedZ = objects[0].z + radii[0] + config.cameraCollisionRadius + 0.1f;
        assertTrue(result.cameraUpdate.position.z > minAllowedZ,
                "Камера не должна проникать в объект (z = " + result.cameraUpdate.position.z + ")");
    }
    
    private static void testSmoothTransitions() {
        System.out.println("Тест: testSmoothTransitions");
        
        PhysicsConfig config = new PhysicsConfig();
        FieldPhysicsEngine physicsEngine = new FieldPhysicsEngine(config);
        
        Vector3f cameraStart = new Vector3f(0, 2, 10);
        Vector3f target = new Vector3f(0, 0, 0);
        physicsEngine.initialize(cameraStart, target);
        
        Vector3f[] objects = new Vector3f[1];
        float[] radii = new float[1];
        objects[0] = new Vector3f(0, 0, 0);
        radii[0] = 5.0f;
        
        float previousNear = 0.0f;
        float previousFar = 0.0f;
        
        for (int i = 0; i < 10; i++) {
            float t = i / 9.0f;
            Vector3f desiredPos = new Vector3f(0, 2, 20 + (5 - 20) * t);
            
            PhysicsUpdateResult result = physicsEngine.update(1.0f / 60.0f, desiredPos, objects, radii);
            
            float currentNear = result.cameraUpdate.near;
            float currentFar = result.cameraUpdate.far;
            
            if (i > 0) {
                float nearChange = Math.abs(currentNear - previousNear);
                assertTrue(nearChange < 0.5f, "Плавное изменение near (изменение: " + nearChange + ")");
                
                float farChange = Math.abs(currentFar - previousFar);
                assertTrue(farChange < 5.0f, "Плавное изменение far (изменение: " + farChange + ")");
            }
            
            previousNear = currentNear;
            previousFar = currentFar;
        }
    }
}