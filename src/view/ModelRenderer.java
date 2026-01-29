package view;

import model.*;
import model.Polygon;
import physics.field.FieldPhysicsEngine;
import physics.field.FieldPhysicsEngine.PhysicsUpdateResult;
import physics.camera.CameraPhysics.CameraUpdate;
import math.Vector3f;
import math.Vector4f;
import math.Matrix4f;
import utils.PhysicsConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Компонент для визуализации 3D моделей с физическим движком
 */
public class ModelRenderer extends JPanel {

                    // Растеризация треугольника с освещением и текстурой
                    private void rasterizeTriangle(Graphics2D g2d,
                                                   double[] v0, double[] v1, double[] v2,
                                                   double[] n0, double[] n1, double[] n2,
                                                   double[] t0, double[] t1, double[] t2,
                                                   Color baseColor,
                                                   java.awt.image.BufferedImage texture) {
                        // Ограничивающий прямоугольник
                        int minX = (int)Math.max(0, Math.min(Math.min(v0[0], v1[0]), v2[0]));
                        int maxX = (int)Math.max(0, Math.max(Math.max(v0[0], v1[0]), v2[0]));
                        int minY = (int)Math.max(0, Math.min(Math.min(v0[1], v1[1]), v2[1]));
                        int maxY = (int)Math.max(0, Math.max(Math.max(v0[1], v1[1]), v2[1]));
                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {
                                double[] bary = barycentric(v0, v1, v2, x + 0.5, y + 0.5);
                                if (bary[0] < 0 || bary[1] < 0 || bary[2] < 0) continue;
                                // Интерполяция нормали
                                double nx = n0[0] * bary[0] + n1[0] * bary[1] + n2[0] * bary[2];
                                double ny = n0[1] * bary[0] + n1[1] * bary[1] + n2[1] * bary[2];
                                double nz = n0[2] * bary[0] + n1[2] * bary[1] + n2[2] * bary[2];
                                double norm = Math.sqrt(nx * nx + ny * ny + nz * nz);
                                if (norm > 1e-8) { nx /= norm; ny /= norm; nz /= norm; }
                                // Интерполяция UV
                                double u = 0, v = 0;
                                if (t0 != null && t1 != null && t2 != null) {
                                    u = t0[0] * bary[0] + t1[0] * bary[1] + t2[0] * bary[2];
                                    v = t0[1] * bary[0] + t1[1] * bary[1] + t2[1] * bary[2];
                                }
                                // Освещение (Lambert + Phong)
                                // Для viewDir используем направление на "камеру" (0,0,1) в экранных координатах
                                double light = enableLighting ? computeLighting(nx, ny, nz, 0, 0, 1) : 1.0;
                                Color color = baseColor;
                                if (enableTexture && texture != null && t0 != null) {
                                    color = sampleTexture(texture, u, v);
                                }
                                int r = (int)Math.round(color.getRed() * light);
                                int g = (int)Math.round(color.getGreen() * light);
                                int b = (int)Math.round(color.getBlue() * light);
                                int a = color.getAlpha();
                                g2d.setColor(new Color(
                                    Math.max(0, Math.min(255, r)),
                                    Math.max(0, Math.min(255, g)),
                                    Math.max(0, Math.min(255, b)),
                                    a));
                                g2d.fillRect(x, y, 1, 1);
                            }
                        }
                    }

                    // Барицентрические координаты
                    private static double[] barycentric(double[] v0, double[] v1, double[] v2, double px, double py) {
                        double x0 = v0[0], y0 = v0[1];
                        double x1 = v1[0], y1 = v1[1];
                        double x2 = v2[0], y2 = v2[1];
                        double denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
                        if (Math.abs(denom) < 1e-8) return new double[]{-1, -1, -1};
                        double w0 = ((y1 - y2) * (px - x2) + (x2 - x1) * (py - y2)) / denom;
                        double w1 = ((y2 - y0) * (px - x2) + (x0 - x2) * (py - y2)) / denom;
                        double w2 = 1.0 - w0 - w1;
                        return new double[]{w0, w1, w2};
                    }
                // Phong (Lambert + ambient + specular) shading
                private double computeLighting(double nx, double ny, double nz, double vx, double vy, double vz) {
                    double[] lightDir = normalize(lightDirX, lightDirY, lightDirZ);
                    double[] viewDir = normalize(vx, vy, vz);
                    // Diffuse
                    double dot = nx * lightDir[0] + ny * lightDir[1] + nz * lightDir[2];
                    double diffuse = Math.max(0, dot);
                    // Specular
                    double[] reflectDir = new double[] {
                        2 * dot * nx - lightDir[0],
                        2 * dot * ny - lightDir[1],
                        2 * dot * nz - lightDir[2]
                    };
                    double spec = Math.pow(Math.max(0, reflectDir[0] * viewDir[0] + reflectDir[1] * viewDir[1] + reflectDir[2] * viewDir[2]), shininess);
                    double result = ambientStrength + (1.0 - ambientStrength) * diffuse * lightIntensity + specularStrength * spec;
                    return Math.min(1.0, Math.max(0.0, result));
                }

                // Sample texture color by UV (u,v in [0,1])
                private Color sampleTexture(java.awt.image.BufferedImage texture, double u, double v) {
                    if (texture == null) return Color.LIGHT_GRAY;
                    int w = texture.getWidth();
                    int h = texture.getHeight();
                    int x = (int)(Math.abs(u % 1.0) * (w - 1));
                    int y = (int)((1.0 - Math.abs(v % 1.0)) * (h - 1));
                    x = Math.max(0, Math.min(w - 1, x));
                    y = Math.max(0, Math.min(h - 1, y));
                    int rgb = texture.getRGB(x, y);
                    return new Color(rgb, true);
                }
            // Вспомогательный метод для нормализации вектора
            private static double[] normalize(double x, double y, double z) {
                double len = Math.sqrt(x * x + y * y + z * z);
                if (len < 1e-8) return new double[]{0, 0, 1};
                return new double[]{x / len, y / len, z / len};
            }
        // --- Lighting and Texture parameters ---
        private boolean enableLighting = true;
        private boolean enableTexture = true;
        private double ambientStrength = 0.2;
        private double lightIntensity = 1.2;
        private double specularStrength = 0.5;
        private double shininess = 32.0;
        // Light direction (normalized)
        private double lightDirX = -0.5, lightDirY = -0.5, lightDirZ = 1.0;
    private Scene scene;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Физический движок
    private FieldPhysicsEngine physicsEngine;
    private PhysicsConfig physicsConfig;
    private PhysicsUpdateResult lastPhysicsUpdate;
    private boolean physicsInitialized = false;
    
    // Управление камерой
    private boolean[] keysPressed = new boolean[256];
    private float cameraRotationX = 0;
    private float cameraRotationY = 0;
    private float cameraDistance = 10.0f;
    private Vector3f cameraTarget = new Vector3f(0, 0, 0);
    private Vector3f desiredCameraPos = new Vector3f(0, 0, 10);
    
    // Таймер для обновления физики
    private Timer physicsTimer;
    private long lastUpdateTime = System.currentTimeMillis();

    public ModelRenderer(Scene scene) {
        this.scene = scene;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // Инициализация физического движка
        physicsConfig = new PhysicsConfig();
        physicsConfig.aspectRatio = 16.0f / 9.0f;
        physicsConfig.fov = 60.0f;
        physicsEngine = new FieldPhysicsEngine(physicsConfig);
        
        // Обновляем aspect ratio при изменении размера окна
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = getHeight();
                if (width > 0 && height > 0) {
                    physicsConfig.aspectRatio = (float)width / (float)height;
                    physicsEngine.setConfig(physicsConfig);
                }
            }
        });
        
        // Инициализация управления
        setupInputHandlers();
        
        // Запуск цикла обновления физики
        physicsTimer = new Timer(16, e -> updatePhysics()); // ~60 FPS
        physicsTimer.start();
        
        setFocusable(true);
        requestFocusInWindow();
    }
    
    private void setupInputHandlers() {
        // Обработка клавиатуры
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keysPressed[e.getKeyCode()] = true;
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                keysPressed[e.getKeyCode()] = false;
            }
        });
        
        // Обработка мыши для вращения камеры
        addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            cameraDistance *= (rotation > 0) ? 1.1f : 0.9f;
            if (cameraDistance < 1.0f) cameraDistance = 1.0f;
            if (cameraDistance > 100.0f) cameraDistance = 100.0f;
            updateDesiredCameraPosition();
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point lastPoint;
            private boolean isRotating = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastPoint = e.getPoint();
                    isRotating = true;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isRotating && lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    
                    cameraRotationY += dx * 0.01f;
                    cameraRotationX += dy * 0.01f;
                    
                    // Ограничиваем вертикальное вращение
                    if (cameraRotationX > Math.PI / 2 - 0.1f) cameraRotationX = (float)(Math.PI / 2 - 0.1f);
                    if (cameraRotationX < -Math.PI / 2 + 0.1f) cameraRotationX = (float)(-Math.PI / 2 + 0.1f);
                    
                    lastPoint = e.getPoint();
                    updateDesiredCameraPosition();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isRotating = false;
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    private void updateDesiredCameraPosition() {
        // Вычисляем позицию камеры на основе расстояния и углов (сферические координаты)
        float x = (float)(cameraDistance * Math.sin(cameraRotationY) * Math.cos(cameraRotationX));
        float y = (float)(cameraDistance * Math.sin(cameraRotationX));
        float z = (float)(cameraDistance * Math.cos(cameraRotationY) * Math.cos(cameraRotationX));
        
        desiredCameraPos = cameraTarget.add(new Vector3f(x, y, z));
        
        // Обновляем физический движок, если он инициализирован
        if (physicsInitialized) {
            // Обновляем target в физическом движке через камеру
            // (это делается автоматически через update, но мы можем принудительно обновить)
        }
    }
    
    private void updatePhysics() {
        if (!physicsInitialized) {
            initializePhysics();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;
        
        if (deltaTime > 0.1f) deltaTime = 0.1f; // Ограничиваем максимальный шаг
        
        // Обработка клавиатуры для перемещения камеры
        handleKeyboardInput(deltaTime);
        
        // Получаем позиции объектов для физики
        List<Model> models = scene.getModels();
        if (models.isEmpty()) {
            repaint();
            return;
        }
        
        // Подготавливаем данные для физического движка
        List<Vector3f> objectPositions = new ArrayList<>();
        List<Float> objectRadii = new ArrayList<>();
        
        for (Model model : models) {
            // Вычисляем центр модели
            Vector3f center = calculateModelCenter(model);
            objectPositions.add(center);
            
            // Вычисляем радиус ограничивающей сферы
            float radius = calculateModelRadius(model, center);
            objectRadii.add(radius);
        }
        
        Vector3f[] positionsArray = objectPositions.toArray(new Vector3f[0]);
        float[] radiiArray = new float[objectRadii.size()];
        for (int i = 0; i < objectRadii.size(); i++) {
            radiiArray[i] = objectRadii.get(i);
        }
        
        // Обновляем целевой объект камеры (центр активной модели)
        // Камера автоматически следует за активной моделью
        Vector3f newTarget;
        if (scene.hasActiveModel()) {
            Model activeModel = scene.getActiveModel();
            newTarget = calculateModelCenter(activeModel);
        } else if (!models.isEmpty()) {
            newTarget = calculateModelCenter(models.get(0));
        } else {
            newTarget = cameraTarget;
        }
        
        // Плавно перемещаем камеру к новому целевому объекту, если он изменился
        float targetDistance = newTarget.distance(cameraTarget);
        if (targetDistance > 0.01f) {
            // Плавное перемещение целевого объекта
            float lerpFactor = Math.min(1.0f, deltaTime * 2.0f); // Скорость следования
            cameraTarget = new Vector3f(
                cameraTarget.x + (newTarget.x - cameraTarget.x) * lerpFactor,
                cameraTarget.y + (newTarget.y - cameraTarget.y) * lerpFactor,
                cameraTarget.z + (newTarget.z - cameraTarget.z) * lerpFactor
            );
            
            // Обновляем target в физическом движке
            physicsEngine.setCameraTarget(cameraTarget);
            
            // Обновляем желаемую позицию камеры относительно нового target
            updateDesiredCameraPosition();
        }
        
        // Обновляем физический движок
        try {
            lastPhysicsUpdate = physicsEngine.update(
                deltaTime,
                desiredCameraPos,
                positionsArray,
                radiiArray
            );
        } catch (Exception e) {
            // Если произошла ошибка, используем fallback рендеринг
            System.err.println("Physics update error: " + e.getMessage());
            e.printStackTrace();
        }
        
        repaint();
    }
    
    private void initializePhysics() {
        List<Model> models = scene.getModels();
        if (models.isEmpty()) {
            return;
        }
        
        // Вычисляем начальную позицию камеры и целевой объект
        Vector3f initialTarget;
        
        if (scene.hasActiveModel()) {
            initialTarget = calculateModelCenter(scene.getActiveModel());
        } else {
            initialTarget = calculateModelCenter(models.get(0));
        }
        
        cameraTarget = initialTarget;
        
        // Вычисляем начальное расстояние до модели
        float maxRadius = 0;
        for (Model model : models) {
            Vector3f center = calculateModelCenter(model);
            float radius = calculateModelRadius(model, center);
            maxRadius = Math.max(maxRadius, radius);
        }
        
        // Устанавливаем расстояние камеры в зависимости от размера модели
        cameraDistance = Math.max(maxRadius * 2.5f, 5.0f);
        if (cameraDistance > 50.0f) cameraDistance = 50.0f;
        
        updateDesiredCameraPosition();
        Vector3f initialPos = desiredCameraPos;
        
        // Инициализируем физический движок
        physicsEngine.initialize(initialPos, initialTarget);
        physicsInitialized = true;
        
        // Обновляем aspect ratio
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            physicsConfig.aspectRatio = (float)width / (float)height;
            physicsEngine.setConfig(physicsConfig);
        }
    }
    
    private void handleKeyboardInput(float deltaTime) {
        float moveSpeed = 5.0f * deltaTime;
        Vector3f move = new Vector3f(0, 0, 0);
        
        if (keysPressed[KeyEvent.VK_W]) {
            move = move.add(new Vector3f(0, 0, -moveSpeed));
        }
        if (keysPressed[KeyEvent.VK_S]) {
            move = move.add(new Vector3f(0, 0, moveSpeed));
        }
        if (keysPressed[KeyEvent.VK_A]) {
            move = move.add(new Vector3f(-moveSpeed, 0, 0));
        }
        if (keysPressed[KeyEvent.VK_D]) {
            move = move.add(new Vector3f(moveSpeed, 0, 0));
        }
        if (keysPressed[KeyEvent.VK_Q]) {
            move = move.add(new Vector3f(0, moveSpeed, 0));
        }
        if (keysPressed[KeyEvent.VK_E]) {
            move = move.add(new Vector3f(0, -moveSpeed, 0));
        }
        
        if (move.length() > 0) {
            // Применяем вращение к вектору движения
            Vector3f rotatedMove = rotateVector(move, cameraRotationY, cameraRotationX);
            desiredCameraPos = desiredCameraPos.add(rotatedMove);
            cameraTarget = cameraTarget.add(rotatedMove);
        }
    }
    
    private Vector3f rotateVector(Vector3f v, float yaw, float pitch) {
        // Вращение вокруг Y (yaw)
        float cosY = (float)Math.cos(yaw);
        float sinY = (float)Math.sin(yaw);
        float x1 = v.x * cosY - v.z * sinY;
        float z1 = v.x * sinY + v.z * cosY;
        
        // Вращение вокруг X (pitch)
        float cosP = (float)Math.cos(pitch);
        float sinP = (float)Math.sin(pitch);
        float y1 = v.y * cosP - z1 * sinP;
        float z2 = v.y * sinP + z1 * cosP;
        
        return new Vector3f(x1, y1, z2);
    }
    
    private Vector3f calculateModelCenter(Model model) {
        if (model.getVertexCount() == 0) {
            return new Vector3f(0, 0, 0);
        }
        
        double sumX = 0, sumY = 0, sumZ = 0;
        for (Vertex v : model.getVertices()) {
            sumX += v.getX();
            sumY += v.getY();
            sumZ += v.getZ();
        }
        
        int count = model.getVertexCount();
        return new Vector3f(
            (float)(sumX / count),
            (float)(sumY / count),
            (float)(sumZ / count)
        );
    }
    
    private float calculateModelRadius(Model model, Vector3f center) {
        float maxDist = 0;
        for (Vertex v : model.getVertices()) {
            Vector3f vertexPos = new Vector3f(
                (float)v.getX(),
                (float)v.getY(),
                (float)v.getZ()
            );
            float dist = center.distance(vertexPos);
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        return Math.max(maxDist, 1.0f); // Минимальный радиус 1.0
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Очищаем фон
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);

        List<Model> models = scene.getModels();
        if (models.isEmpty()) {
            // если нет моделей
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            String message = "No models loaded. Use File > Open Model to load a model.";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(message)) / 2;
            int y = height / 2;
            g2d.drawString(message, x, y);
            return;
        }
        
        // Обновляем aspect ratio
        if (width > 0 && height > 0) {
            physicsConfig.aspectRatio = (float)width / (float)height;
        }

        // По умолчанию используем простой рендеринг для надежности
        // Физический рендеринг можно включить позже, когда он будет полностью протестирован
        boolean usePhysicsRendering = false; // Временно отключено
        
        if (usePhysicsRendering) {
            // Используем физический движок для рендеринга, если он инициализирован
            // Если физика не готова, пытаемся инициализировать её
            if (!physicsInitialized && !models.isEmpty()) {
                initializePhysics();
            }
            
            if (physicsInitialized && lastPhysicsUpdate != null) {
                try {
                    drawWithPhysics(g2d, width, height);
                } catch (Exception e) {
                    // Если ошибка при рендеринге с физикой, используем простой рендеринг
                    System.err.println("Error in physics rendering: " + e.getMessage());
                    e.printStackTrace();
                    drawSimple(g2d, width, height);
                }
            } else {
                // Fallback к простому рендерингу
                drawSimple(g2d, width, height);
            }
        } else {
            // Используем простой рендеринг
            drawSimple(g2d, width, height);
        }

        // Информация о моделях и физике
        drawInfo(g2d, width, height);
    }
    
    private void drawWithPhysics(Graphics2D g2d, int width, int height) {
        CameraUpdate cameraUpdate = lastPhysicsUpdate.cameraUpdate;
        
        // Создаем view матрицу из данных камеры
        Matrix4f viewMatrix = createViewMatrix(cameraUpdate);
        Matrix4f projectionMatrix = lastPhysicsUpdate.projectionUpdate.projectionMatrix;
        
        // Комбинируем view и projection матрицы
        Matrix4f viewProjMatrix = projectionMatrix.multiply(viewMatrix);
        
        // Рендерим каждую модель
        int activeIndex = scene.getActiveModelIndex();
        List<Model> models = scene.getModels();
        
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            boolean isActive = (i == activeIndex);
            boolean isSelected = scene.isModelSelected(i);

            // Разные цвета для активных и неактивных моделей
            if (isActive) {
                g2d.setColor(isSelected ? new Color(0, 150, 255) : new Color(0, 100, 200));
            } else if (isSelected) {
                g2d.setColor(new Color(150, 150, 150));
            } else {
                g2d.setColor(new Color(100, 100, 100));
            }

            boolean visible = i < lastPhysicsUpdate.visibilityFlags.length ? 
                             lastPhysicsUpdate.visibilityFlags[i] : true;
            drawModel3D(g2d, model, viewProjMatrix, width, height, visible);
        }
    }
    
    private Matrix4f createViewMatrix(CameraUpdate cameraUpdate) {
        Vector3f f = cameraUpdate.forward;
        Vector3f r = cameraUpdate.right;
        Vector3f u = cameraUpdate.up;
        Vector3f pos = cameraUpdate.position;
        
        Matrix4f view = new Matrix4f();
        view.set(0, 0, r.x); view.set(0, 1, r.y); view.set(0, 2, r.z);
        view.set(1, 0, u.x); view.set(1, 1, u.y); view.set(1, 2, u.z);
        view.set(2, 0, -f.x); view.set(2, 1, -f.y); view.set(2, 2, -f.z);
        
        view.set(0, 3, -r.dot(pos));
        view.set(1, 3, -u.dot(pos));
        view.set(2, 3, f.dot(pos));
        
        return view;
    }
    
    private void drawModel3D(Graphics2D g2d, Model model, Matrix4f viewProjMatrix, 
                             int width, int height, boolean visible) {
        if (!visible) return;
        
        List<Vertex> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();
        
        // Преобразуем вершины в экранные координаты
        List<ScreenPoint> screenPoints = new ArrayList<>();
        for (Vertex vertex : vertices) {
            Vector4f worldPos = new Vector4f(
                (float)vertex.getX(),
                (float)vertex.getY(),
                (float)vertex.getZ(),
                1.0f
            );
            
            Vector4f clipPos = viewProjMatrix.multiply(worldPos);
            
            // Проверяем, находится ли точка в видимой области перед perspective divide
            if (Math.abs(clipPos.w) < 0.0001f) {
                // Точка слишком близко к камере или на бесконечности
                continue;
            }
            
            Vector4f ndcPos = clipPos.perspectiveDivide();
            
            // Проверяем, находится ли точка в видимой области NDC ([-1, 1] для x, y, z)
            // Но рисуем даже если немного выходит за границы для лучшей видимости
            boolean inView = (ndcPos.x >= -2.0f && ndcPos.x <= 2.0f && 
                             ndcPos.y >= -2.0f && ndcPos.y <= 2.0f &&
                             ndcPos.z >= -2.0f && ndcPos.z <= 2.0f);
            
            // Преобразуем NDC в экранные координаты
            int screenX = (int)((ndcPos.x + 1.0f) * 0.5f * width);
            int screenY = (int)((1.0f - ndcPos.y) * 0.5f * height);
            
            screenPoints.add(new ScreenPoint(screenX, screenY, ndcPos.z, inView));
        }
        
        // Рисуем полигоны
        g2d.setStroke(new BasicStroke(1.0f));
        for (Polygon polygon : polygons) {
            List<Integer> indices = polygon.getVertexIndices();
            if (indices.size() < 2) continue;
            
            int[] xPoints = new int[indices.size()];
            int[] yPoints = new int[indices.size()];
            boolean allVisible = true;
            
            for (int i = 0; i < indices.size(); i++) {
                int idx = indices.get(i);
                if (idx < 0 || idx >= screenPoints.size()) {
                    allVisible = false;
                    break;
                }
                ScreenPoint sp = screenPoints.get(idx);
                xPoints[i] = sp.x;
                yPoints[i] = sp.y;
            }
            
            // Рисуем полигон, если есть хотя бы 2 точки
            if (allVisible && xPoints.length >= 2) {
                // Проверяем, есть ли хотя бы одна точка в видимой области
                boolean hasVisiblePoint = false;
                for (int i = 0; i < indices.size(); i++) {
                    int idx = indices.get(i);
                    if (idx >= 0 && idx < screenPoints.size()) {
                        ScreenPoint sp = screenPoints.get(idx);
                        if (sp.inView || (sp.x >= -width && sp.x <= width * 2 && 
                                         sp.y >= -height && sp.y <= height * 2)) {
                            hasVisiblePoint = true;
                            break;
                        }
                    }
                }
                
                if (hasVisiblePoint) {
                    g2d.drawPolyline(xPoints, yPoints, xPoints.length);
                    // Замыкаем полигон
                    if (xPoints.length > 2) {
                        g2d.drawLine(xPoints[xPoints.length - 1], yPoints[yPoints.length - 1],
                                   xPoints[0], yPoints[0]);
                    }
                }
            }
        }
        
        // Рисуем вершины точками (только видимые)
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2.0f));
        for (ScreenPoint sp : screenPoints) {
            if (sp.inView && sp.x >= -10 && sp.x <= width + 10 && 
                sp.y >= -10 && sp.y <= height + 10) {
                g2d.fillOval(sp.x - 2, sp.y - 2, 4, 4);
            }
        }
    }
    
    private static class ScreenPoint {
        int x, y;
        float z;
        boolean inView;
        ScreenPoint(int x, int y, float z, boolean inView) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.inView = inView;
        }
    }
    
    private void drawSimple(Graphics2D g2d, int width, int height) {
        // Простой рендеринг без физики (fallback)
        List<Model> models = scene.getModels();
        
        // Находим границы всех моделей для центрирования
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

        for (Model model : models) {
            for (Vertex vertex : model.getVertices()) {
                minX = Math.min(minX, vertex.getX());
                maxX = Math.max(maxX, vertex.getX());
                minY = Math.min(minY, vertex.getY());
                maxY = Math.max(maxY, vertex.getY());
                minZ = Math.min(minZ, vertex.getZ());
                maxZ = Math.max(maxZ, vertex.getZ());
            }
        }

        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;
        double centerZ = (minZ + maxZ) / 2;

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        double rangeZ = maxZ - minZ;
        double maxRange = Math.max(Math.max(rangeX, rangeY), rangeZ);

        // Автоматический масштаб для вписывания в экран
        // Всегда пересчитываем масштаб для правильного отображения
        if (maxRange > 0) {
            double scaleX = (width * 0.8) / Math.max(rangeX, 0.001);
            double scaleY = (height * 0.8) / Math.max(rangeY, 0.001);
            double newScale = Math.min(scaleX, scaleY);
            
            // Если масштаб не был установлен или модель изменилась, обновляем его
            if (scale == 1.0 || Math.abs(scale - newScale) > 0.1) {
                scale = newScale;
            }
        }

        // Рендерим каждую модель
        int activeIndex = scene.getActiveModelIndex();
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            boolean isActive = (i == activeIndex);
            boolean isSelected = scene.isModelSelected(i);

            // Разные цвета для активных и неактивных моделей
            if (isActive) {
                g2d.setColor(isSelected ? new Color(0, 150, 255) : new Color(0, 100, 200));
            } else if (isSelected) {
                g2d.setColor(new Color(150, 150, 150));
            } else {
                g2d.setColor(new Color(100, 100, 100));
            }

            drawModel(g2d, model, centerX, centerY, centerZ, width, height);
        }
    }
    
    private void drawInfo(Graphics2D g2d, int width, int height) {
        List<Model> models = scene.getModels();
        int activeIndex = scene.getActiveModelIndex();
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int yPos = 20;
        
        // Информация о моделях
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            String info = model.getName() + ": " + model.getVertexCount() + 
                         " vertices, " + model.getPolygonCount() + " polygons";
            if (i == activeIndex) {
                info = "► " + info;
            }
            g2d.drawString(info, 10, yPos);
            yPos += 20;
        }
        
        // Информация о физике
        if (physicsInitialized && lastPhysicsUpdate != null) {
            yPos += 10;
            g2d.setColor(new Color(100, 100, 100));
            CameraUpdate cam = lastPhysicsUpdate.cameraUpdate;
            g2d.drawString(String.format("Camera: (%.2f, %.2f, %.2f)", 
                cam.position.x, cam.position.y, cam.position.z), 10, yPos);
            yPos += 15;
            g2d.drawString(String.format("Target: (%.2f, %.2f, %.2f)", 
                cam.target.x, cam.target.y, cam.target.z), 10, yPos);
            yPos += 15;
            g2d.drawString(String.format("Near: %.3f, Far: %.3f", 
                cam.near, cam.far), 10, yPos);
            yPos += 15;
            g2d.drawString(String.format("Physics: %s", 
                lastPhysicsUpdate.projectionUpdate.projectionType), 10, yPos);
            yPos += 15;
            g2d.drawString("Controls: Mouse drag = rotate, Wheel = zoom, WASD = move, QE = up/down", 10, yPos);
        } else if (!models.isEmpty()) {
            yPos += 10;
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("Using simple rendering (physics disabled)", 10, yPos);
            yPos += 15;
            g2d.drawString(String.format("Scale: %.2f, Offset: (%.0f, %.0f)", scale, offsetX, offsetY), 10, yPos);
        }
    }

    private void drawModel(Graphics2D g2d, Model model, 
                          double centerX, double centerY, double centerZ,
                          int width, int height) {

        List<Vertex> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();
        
        if (vertices.isEmpty() || polygons.isEmpty()) {
            return; // Нет данных для отображения
        }

        // Рисуем полигоны (треугольники с освещением и текстурой)
        g2d.setStroke(new BasicStroke(1.5f));
        int polygonsDrawn = 0;
        java.awt.image.BufferedImage texture = model.getTexture();
        for (Polygon polygon : polygons) {
            List<Integer> indices = polygon.getVertexIndices();
            if (indices.size() == 3) {
                // Только заливка/текстура для треугольников
                double[][] v = new double[3][];
                double[][] n = new double[3][];
                double[][] t = new double[3][];
                for (int i = 0; i < 3; i++) {
                    int idx = indices.get(i);
                    if (idx < 0 || idx >= vertices.size()) continue;
                    Vertex vert = vertices.get(idx);
                    double[] transformed = transformVertexWithCamera(
                        vert.getX(), vert.getY(), vert.getZ(),
                        centerX, centerY, centerZ
                    );
                    double x = transformed[0] * scale;
                    double y = transformed[1] * scale;
                    double z = transformed[2];
                    double perspective = cameraDistance / (cameraDistance + z);
                    x *= perspective;
                    y *= perspective;
                    int screenX = (int) (width / 2 + x + offsetX);
                    int screenY = (int) (height / 2 - y + offsetY);
                    v[i] = new double[]{screenX, screenY, z};
                    n[i] = new double[]{vert.getNx(), vert.getNy(), vert.getNz()};
                    t[i] = new double[]{vert.getU(), vert.getV()};
                }
                rasterizeTriangle(g2d, v[0], v[1], v[2], n[0], n[1], n[2], t[0], t[1], t[2], g2d.getColor(), texture);
                polygonsDrawn++;
            } else if (indices.size() >= 2) {
                // Wireframe только для не-треугольников
                int[] xPoints = new int[indices.size()];
                int[] yPoints = new int[indices.size()];
                boolean hasValidPoints = false;
                for (int i = 0; i < indices.size(); i++) {
                    int idx = indices.get(i);
                    if (idx < 0 || idx >= vertices.size()) continue;
                    Vertex vtx = vertices.get(idx);
                    double[] transformed = transformVertexWithCamera(
                        vtx.getX(), vtx.getY(), vtx.getZ(),
                        centerX, centerY, centerZ
                    );
                    double x = transformed[0] * scale;
                    double y = transformed[1] * scale;
                    double z = transformed[2];
                    double perspective = cameraDistance / (cameraDistance + z);
                    x *= perspective;
                    y *= perspective;
                    int screenX = (int) (width / 2 + x + offsetX);
                    int screenY = (int) (height / 2 - y + offsetY);
                    xPoints[i] = screenX;
                    yPoints[i] = screenY;
                    if (screenX >= -width && screenX <= width * 2 && screenY >= -height && screenY <= height * 2) {
                        hasValidPoints = true;
                    }
                }
                if (hasValidPoints && xPoints.length >= 2) {
                    g2d.drawPolyline(xPoints, yPoints, xPoints.length);
                    if (xPoints.length > 2) {
                        g2d.drawLine(xPoints[xPoints.length - 1], yPoints[yPoints.length - 1], xPoints[0], yPoints[0]);
                    }
                    polygonsDrawn++;
                }
            }
        }
        
        // Отладочная информация
        if (polygonsDrawn == 0 && !polygons.isEmpty()) {
            System.out.println("Warning: No polygons drawn. Vertices: " + vertices.size() + 
                             ", Polygons: " + polygons.size() + 
                             ", Scale: " + scale + 
                             ", Center: (" + centerX + ", " + centerY + ", " + centerZ + ")");
        }

        // Рисуем вершины точками (более заметными)
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2.0f));
        int verticesDrawn = 0;
        for (Vertex vertex : vertices) {
            // Применяем трансформации камеры
            double[] transformed = transformVertexWithCamera(
                vertex.getX(), vertex.getY(), vertex.getZ(),
                centerX, centerY, centerZ
            );
            
            double x = transformed[0] * scale;
            double y = transformed[1] * scale;
            double z = transformed[2];

            // Простая перспективная проекция
            double perspective = cameraDistance / (cameraDistance + z);
            x *= perspective;
            y *= perspective;

            int screenX = (int) (width / 2 + x + offsetX);
            int screenY = (int) (height / 2 - y + offsetY);
            
            // Рисуем только если точка в видимой области
            if (screenX >= -10 && screenX <= width + 10 && 
                screenY >= -10 && screenY <= height + 10) {
                g2d.fillOval(screenX - 3, screenY - 3, 6, 6); // Немного больше
                verticesDrawn++;
            }
        }
        
        // Отладочная информация
        if (verticesDrawn == 0 && !vertices.isEmpty()) {
            System.out.println("Warning: No vertices drawn. Total vertices: " + vertices.size());
        }
    }
    
    /**
     * Трансформирует вершину с учетом камеры (вращение и смещение камеры)
     * Возвращает [x, y, z] в пространстве камеры
     */
    private double[] transformVertexWithCamera(double vx, double vy, double vz,
                                               double centerX, double centerY, double centerZ) {
        // Центрируем относительно центра модели
        double x = vx - centerX;
        double y = vy - centerY;
        double z = vz - centerZ;
        
        // Применяем смещение камеры (cameraTarget - это то, на что смотрит камера)
        // Вычисляем относительное смещение от центра модели
        double dx = centerX - cameraTarget.x;
        double dy = centerY - cameraTarget.y;
        double dz = centerZ - cameraTarget.z;
        
        x -= dx;
        y -= dy;
        z -= dz;
        
        // Применяем вращение вокруг Y (yaw)
        double cosY = Math.cos(-cameraRotationY);
        double sinY = Math.sin(-cameraRotationY);
        double x1 = x * cosY - z * sinY;
        double z1 = x * sinY + z * cosY;
        x = x1;
        z = z1;
        
        // Применяем вращение вокруг X (pitch)
        double cosX = Math.cos(-cameraRotationX);
        double sinX = Math.sin(-cameraRotationX);
        double y1 = y * cosX - z * sinX;
        double z2 = y * sinX + z * cosX;
        y = y1;
        z = z2;
        
        return new double[]{x, y, z};
    }

    public void resetView() {
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
        cameraDistance = 10.0f;
        cameraRotationX = 0;
        cameraRotationY = 0;
        physicsInitialized = false;
        repaint();
    }
    
    public void onSceneChanged() {
        // Переинициализируем физику при изменении сцены
        physicsInitialized = false;
        lastPhysicsUpdate = null;
        
        // Сбрасываем камеру и масштаб к начальному состоянию
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
        cameraDistance = 10.0f;
        cameraRotationX = 0;
        cameraRotationY = 0;
        
        if (!scene.getModels().isEmpty()) {
            // Вычисляем новый центр сцены
            List<Model> models = scene.getModels();
            Vector3f sceneCenter = new Vector3f(0, 0, 0);
            int totalVertices = 0;
            
            for (Model model : models) {
                Vector3f center = calculateModelCenter(model);
                int vertexCount = model.getVertexCount();
                if (vertexCount > 0) {
                    sceneCenter = sceneCenter.add(center.mul(vertexCount));
                    totalVertices += vertexCount;
                }
            }
            
            if (totalVertices > 0) {
                cameraTarget = sceneCenter.mul(1.0f / totalVertices);
            } else {
                cameraTarget = new Vector3f(0, 0, 0);
            }
            
            updateDesiredCameraPosition();
            
            // Инициализируем физику сразу
            initializePhysics();
            
            // Принудительно обновляем физику один раз для первого кадра
            if (physicsInitialized) {
                try {
                    List<Model> modelsList = scene.getModels();
                    if (!modelsList.isEmpty()) {
                        List<Vector3f> objectPositions = new ArrayList<>();
                        List<Float> objectRadii = new ArrayList<>();
                        
                        for (Model model : modelsList) {
                            Vector3f center = calculateModelCenter(model);
                            objectPositions.add(center);
                            float radius = calculateModelRadius(model, center);
                            objectRadii.add(radius);
                        }
                        
                        Vector3f[] positionsArray = objectPositions.toArray(new Vector3f[0]);
                        float[] radiiArray = new float[objectRadii.size()];
                        for (int i = 0; i < objectRadii.size(); i++) {
                            radiiArray[i] = objectRadii.get(i);
                        }
                        
                        lastPhysicsUpdate = physicsEngine.update(0.016f, desiredCameraPos, positionsArray, radiiArray);
                    }
                } catch (Exception e) {
                    System.err.println("Error initializing physics update: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            cameraTarget = new Vector3f(0, 0, 0);
            desiredCameraPos = new Vector3f(0, 0, 10);
        }
        
        repaint();
    }
}
