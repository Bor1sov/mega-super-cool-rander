package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import math.Frustum;
import math.Vector3f;
import physics.camera.CameraPhysics.CameraUpdate;

public class DebugVisualizer {
    private List<DebugObject> debugObjects = new ArrayList<>();
    private boolean enabled = false;
    
    public DebugVisualizer() {

    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void addFrustum(Frustum frustum, Color color) {
        if (!enabled) return;
        
        DebugObject obj = new DebugObject();
        obj.type = DebugObject.Type.FRUSTUM;
        obj.color = color;
        debugObjects.add(obj);
    }
    
    public void addBoundingSphere(Vector3f center, float radius, Color color) {
        if (!enabled) return;
        
        DebugObject obj = new DebugObject();
        obj.type = DebugObject.Type.SPHERE;
        obj.position = center;
        obj.radius = radius;
        obj.color = color;
        debugObjects.add(obj);
    }
    
    public void addPoint(Vector3f point, Color color, float size) {
        if (!enabled) return;
        
        DebugObject obj = new DebugObject();
        obj.type = DebugObject.Type.POINT;
        obj.position = point;
        obj.size = size;
        obj.color = color;
        debugObjects.add(obj);
    }
    
    public void addLine(Vector3f start, Vector3f end, Color color, float width) {
        if (!enabled) return;
        
        DebugObject obj = new DebugObject();
        obj.type = DebugObject.Type.LINE;
        obj.position = start;
        obj.endPosition = end;
        obj.width = width;
        obj.color = color;
        debugObjects.add(obj);
    }
    
    public void clear() {
        debugObjects.clear();
    }
    
    public BufferedImage renderOverlay(int width, int height, PhysicsConfig config,
                                      CameraUpdate camera, float near, float far) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new java.awt.Color(0f, 0f, 0f, 0.5f));
        g2d.fillRect(0, 0, 300, 300);
        
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        int y = 20;
        int lineHeight = 20;
        
        g2d.drawString("Debug Overlay", 10, y); y += lineHeight * 2;
        
        g2d.drawString(String.format("Camera Pos: (%.2f, %.2f, %.2f)", 
                camera.position.x, camera.position.y, camera.position.z), 10, y); y += lineHeight;
        
        g2d.drawString(String.format("Near/Far: %.3f / %.3f", near, far), 10, y); y += lineHeight;
        g2d.drawString(String.format("Ratio: %.0f:1", far/near), 10, y); y += lineHeight;
        y += lineHeight;
        
        g2d.drawString("Projection:", 10, y); y += lineHeight;
        g2d.drawString(String.format("  Reverse Z: %s", config.useReverseZ), 20, y); y += lineHeight;
        g2d.drawString(String.format("  Log Depth: %s", config.useLogarithmicDepth), 20, y); y += lineHeight;
        g2d.drawString(String.format("  Persp. Correct: %s", config.usePerspectiveCorrectInterpolation), 20, y);
        
        y += lineHeight * 2;
        g2d.drawString("Debug Objects (" + debugObjects.size() + "):", 10, y); y += lineHeight;
        
        for (DebugObject obj : debugObjects) {
            String info = switch (obj.type) {
                case POINT -> String.format("Point at %s (size=%.1f)", obj.position, obj.size);
                case LINE -> String.format("Line from %s to %s (width=%.1f)", obj.position, obj.endPosition, obj.width);
                case SPHERE -> String.format("Sphere at %s (r=%.1f)", obj.position, obj.radius);
                case FRUSTUM -> "Frustum";
                case TEXT -> obj.text != null ? obj.text : "No text";
            };
            java.awt.Color awtColor = obj.color != null ? obj.color.toAWT() : java.awt.Color.YELLOW;
            g2d.setColor(awtColor);
            g2d.drawString("• " + info, 20, y); y += lineHeight;
        }
        
        g2d.dispose();
        return image;
    }
    
    private static class DebugObject {
        enum Type { POINT, LINE, SPHERE, FRUSTUM, TEXT }
        
        Type type;
        Vector3f position;
        Vector3f endPosition;
        float radius;
        float size;
        float width;
        String text;
        Color color;
    }
    
    public static class Color {
        private final float r, g, b, a;
        
        public Color(float r, float g, float b, float a) {
            this.r = r; this.g = g; this.b = b; this.a = a;
        }
        
        public Color(float r, float g, float b) {
            this(r, g, b, 1.0f);
        }
        
        public static final Color RED = new Color(1, 0, 0);
        public static final Color GREEN = new Color(0, 1, 0);
        public static final Color BLUE = new Color(0, 0, 1);
        public static final Color CYAN = new Color(0, 1, 1);
        public static final Color WHITE = new Color(1, 1, 1);
        
        public int getRed() { return (int)(r * 255); }
        public int getGreen() { return (int)(g * 255); }
        public int getBlue() { return (int)(b * 255); }
        public int getAlpha() { return (int)(a * 255); }
        
        public java.awt.Color toAWT() {
            return new java.awt.Color(r, g, b, a);
        }
    }
}