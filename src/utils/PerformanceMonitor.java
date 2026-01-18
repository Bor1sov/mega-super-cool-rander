package utils;

import java.util.LinkedList;
import java.util.Queue;

public class PerformanceMonitor {
    private long frameStartTime;
    private long physicsTime;
    private long collisionTime;
    private long projectionTime;
    private long interpolationTime;
    
    private final Queue<Float> fpsHistory = new LinkedList<>();
    private final Queue<Long> frameTimeHistory = new LinkedList<>();
    private final int historySize = 60;
    
    public void startFrame() {
        frameStartTime = System.nanoTime();
        physicsTime = 0;
        collisionTime = 0;
        projectionTime = 0;
        interpolationTime = 0;
    }
    
    public void startPhysics() {
        physicsTime = System.nanoTime();
    }
    
    public void endPhysics() {
        if (physicsTime > 0) {
            physicsTime = System.nanoTime() - physicsTime;
        }
    }
    
    
    public void endFrame() {
        long frameTime = System.nanoTime() - frameStartTime;
        frameTimeHistory.add(frameTime);
        
        float fps = 1.0f / (frameTime / 1e9f);
        fpsHistory.add(fps);
        
        if (frameTimeHistory.size() > historySize) {
            frameTimeHistory.poll();
            fpsHistory.poll();
        }
    }
    
    public FrameStats getFrameStats() {
        float avgFps = 0;
        long avgFrameTime = 0;
        
        for (Float fps : fpsHistory) avgFps += fps;
        for (Long time : frameTimeHistory) avgFrameTime += time;
        
        if (!fpsHistory.isEmpty()) {
            avgFps /= fpsHistory.size();
            avgFrameTime /= frameTimeHistory.size();
        }
        
        return new FrameStats(
            avgFps,
            avgFrameTime / 1_000_000f,
            physicsTime / 1_000_000f,
            collisionTime / 1_000_000f,
            projectionTime / 1_000_000f,
            interpolationTime / 1_000_000f
        );
    }
    
    public static class FrameStats {
        public final float fps;
        public final float frameTimeMs;
        public final float physicsTimeMs;
        public final float collisionTimeMs;
        public final float projectionTimeMs;
        public final float interpolationTimeMs;
        
        public FrameStats(float fps, float frameTimeMs,
                         float physicsTimeMs, float collisionTimeMs,
                         float projectionTimeMs, float interpolationTimeMs) {
            this.fps = fps;
            this.frameTimeMs = frameTimeMs;
            this.physicsTimeMs = physicsTimeMs;
            this.collisionTimeMs = collisionTimeMs;
            this.projectionTimeMs = projectionTimeMs;
            this.interpolationTimeMs = interpolationTimeMs;
        }
    }
}