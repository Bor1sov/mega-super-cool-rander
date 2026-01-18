package view;

import model.*;
import io.ObjReader;
import io.ObjWriter;
import io.ObjReaderException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Главное окно приложения для просмотра и редактирования 3D моделей
 */
public class ViewerWindow extends JFrame {
    private Scene scene;
    private ModelRenderer modelRenderer;
    private JList<String> modelList;
    private DefaultListModel<String> listModel;
    private JButton deleteVertexButton;
    private JButton deletePolygonButton;
    private JSpinner vertexSpinner;
    private JSpinner polygonSpinner;
    private boolean darkTheme = false;
    
    // Цвета для светлой темы
    private Color lightBgColor = new Color(240, 240, 240);
    private Color lightFgColor = Color.BLACK;
    private Color lightPanelColor = Color.WHITE;
    
    // Цвета для темной темы
    private Color darkBgColor = new Color(45, 45, 45);
    private Color darkFgColor = new Color(220, 220, 220);
    private Color darkPanelColor = new Color(60, 60, 60);

    public ViewerWindow() {
        this.scene = new Scene();
        initializeUI();
        applyTheme();
    }

    private void initializeUI() {
        setTitle("3D Model Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createMenuBar();

        // Центральная панель с рендерером
        modelRenderer = new ModelRenderer(scene);
        JScrollPane renderScrollPane = new JScrollPane(modelRenderer);
        renderScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Model View", 
            TitledBorder.LEFT, TitledBorder.TOP));
        add(renderScrollPane, BorderLayout.CENTER);

        // Правая панель с инструментами
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Левая панель с моделями
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Файл
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open Model...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openModel());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save Model...", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveModel());
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem themeItem = new JMenuItem("Toggle Theme (Light/Dark)", KeyEvent.VK_T);
        themeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        themeItem.addActionListener(e -> toggleTheme());
        viewMenu.add(themeItem);

        menuBar.add(viewMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Models", 
            TitledBorder.LEFT, TitledBorder.TOP));
        panel.setPreferredSize(new Dimension(200, 0));

        listModel = new DefaultListModel<>();
        modelList = new JList<>(listModel);
        modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = modelList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    scene.setActiveModel(selectedIndex);
                    updateVertexAndPolygonSpinners();
                    modelRenderer.repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(modelList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton removeModelButton = new JButton("Remove Model");
        removeModelButton.addActionListener(e -> removeSelectedModel());
        panel.add(removeModelButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Tools", 
            TitledBorder.LEFT, TitledBorder.TOP));
        panel.setPreferredSize(new Dimension(250, 0));

        // Удаление вершин
        JPanel vertexPanel = new JPanel(new BorderLayout());
        vertexPanel.setBorder(BorderFactory.createTitledBorder("Delete Vertex"));
        
        vertexSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        JPanel vertexSpinnerPanel = new JPanel(new FlowLayout());
        vertexSpinnerPanel.add(new JLabel("Index:"));
        vertexSpinnerPanel.add(vertexSpinner);
        vertexPanel.add(vertexSpinnerPanel, BorderLayout.NORTH);

        deleteVertexButton = new JButton("Delete Vertex");
        deleteVertexButton.addActionListener(e -> deleteVertex());
        vertexPanel.add(deleteVertexButton, BorderLayout.SOUTH);

        panel.add(vertexPanel);

        // Удаление полигонов
        JPanel polygonPanel = new JPanel(new BorderLayout());
        polygonPanel.setBorder(BorderFactory.createTitledBorder("Delete Polygon"));
        
        polygonSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        JPanel polygonSpinnerPanel = new JPanel(new FlowLayout());
        polygonSpinnerPanel.add(new JLabel("Index:"));
        polygonSpinnerPanel.add(polygonSpinner);
        polygonPanel.add(polygonSpinnerPanel, BorderLayout.NORTH);

        deletePolygonButton = new JButton("Delete Polygon");
        deletePolygonButton.addActionListener(e -> deletePolygon());
        polygonPanel.add(deletePolygonButton, BorderLayout.SOUTH);

        panel.add(polygonPanel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void openModel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
            }

            @Override
            public String getDescription() {
                return "OBJ Files (*.obj)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Model model = ObjReader.read(selectedFile.getAbsolutePath());
                scene.addModel(model);
                updateModelList();
                scene.setActiveModel(scene.getModelCount() - 1);
                modelList.setSelectedIndex(scene.getActiveModelIndex());
                updateVertexAndPolygonSpinners();
                modelRenderer.onSceneChanged();
                modelRenderer.repaint();
                
                JOptionPane.showMessageDialog(this,
                    "Model loaded successfully!\n" +
                    "Name: " + model.getName() + "\n" +
                    "Vertices: " + model.getVertexCount() + "\n" +
                    "Polygons: " + model.getPolygonCount(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (ObjReaderException e) {
                showErrorDialog("Error loading model", e.getMessage());
            } catch (Exception e) {
                showErrorDialog("Error loading model", 
                    "An unexpected error occurred:\n" + e.getMessage());
            }
        }
    }

    private void saveModel() {
        if (!scene.hasActiveModel()) {
            JOptionPane.showMessageDialog(this,
                "No active model to save. Please load or select a model first.",
                "No Model Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
            }

            @Override
            public String getDescription() {
                return "OBJ Files (*.obj)";
            }
        });

        Model activeModel = scene.getActiveModel();
        fileChooser.setSelectedFile(new File(activeModel.getName() + ".obj"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // Добавляем расширение .obj, если его нет
            if (!filePath.toLowerCase().endsWith(".obj")) {
                filePath += ".obj";
            }

            try {
                ObjWriter.write(activeModel, filePath);
                JOptionPane.showMessageDialog(this,
                    "Model saved successfully to:\n" + filePath,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                showErrorDialog("Error saving model", 
                    "Failed to save model:\n" + e.getMessage());
            }
        }
    }

    private void removeSelectedModel() {
        int selectedIndex = modelList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a model to remove.",
                "No Model Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove model \"" + 
            scene.getModel(selectedIndex).getName() + "\"?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            scene.removeModel(selectedIndex);
            updateModelList();
            if (scene.hasActiveModel()) {
                modelList.setSelectedIndex(scene.getActiveModelIndex());
                updateVertexAndPolygonSpinners();
            } else {
                updateVertexAndPolygonSpinners();
            }
            modelRenderer.onSceneChanged();
            modelRenderer.repaint();
        }
    }

    private void deleteVertex() {
        if (!scene.hasActiveModel()) {
            JOptionPane.showMessageDialog(this,
                "No active model. Please select a model first.",
                "No Model Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Model activeModel = scene.getActiveModel();
        int vertexIndex = (Integer) vertexSpinner.getValue();

        if (vertexIndex < 0 || vertexIndex >= activeModel.getVertexCount()) {
            JOptionPane.showMessageDialog(this,
                "Invalid vertex index: " + vertexIndex,
                "Invalid Index",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete vertex at index " + vertexIndex + "?\n" +
            "All polygons using this vertex will also be removed.",
            "Confirm Vertex Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            activeModel.removeVertex(vertexIndex);
            updateVertexAndPolygonSpinners();
            modelRenderer.onSceneChanged();
            modelRenderer.repaint();
        }
    }

    private void deletePolygon() {
        if (!scene.hasActiveModel()) {
            JOptionPane.showMessageDialog(this,
                "No active model. Please select a model first.",
                "No Model Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Model activeModel = scene.getActiveModel();
        int polygonIndex = (Integer) polygonSpinner.getValue();

        if (polygonIndex < 0 || polygonIndex >= activeModel.getPolygonCount()) {
            JOptionPane.showMessageDialog(this,
                "Invalid polygon index: " + polygonIndex,
                "Invalid Index",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete polygon at index " + polygonIndex + "?",
            "Confirm Polygon Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            activeModel.removePolygon(polygonIndex);
            updateVertexAndPolygonSpinners();
            modelRenderer.onSceneChanged();
            modelRenderer.repaint();
        }
    }

    private void updateModelList() {
        listModel.clear();
        List<Model> models = scene.getModels();
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            String label = model.getName() + " (" + model.getVertexCount() + 
                          " v, " + model.getPolygonCount() + " p)";
            if (i == scene.getActiveModelIndex()) {
                label = "► " + label;
            }
            listModel.addElement(label);
        }
    }

    private void updateVertexAndPolygonSpinners() {
        if (scene.hasActiveModel()) {
            Model activeModel = scene.getActiveModel();
            int vertexCount = activeModel.getVertexCount();
            int polygonCount = activeModel.getPolygonCount();

            vertexSpinner.setModel(new SpinnerNumberModel(
                Math.min((Integer) vertexSpinner.getValue(), Math.max(0, vertexCount - 1)),
                0, Math.max(0, vertexCount - 1), 1));

            polygonSpinner.setModel(new SpinnerNumberModel(
                Math.min((Integer) polygonSpinner.getValue(), Math.max(0, polygonCount - 1)),
                0, Math.max(0, polygonCount - 1), 1));
        } else {
            vertexSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            polygonSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
        }
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        applyTheme();
    }

    private void applyTheme() {
        Color bgColor = darkTheme ? darkBgColor : lightBgColor;
        Color fgColor = darkTheme ? darkFgColor : lightFgColor;
        Color panelColor = darkTheme ? darkPanelColor : lightPanelColor;

        // Применяем тему ко всем компонентам
        applyThemeRecursive(this, bgColor, fgColor, panelColor);
        modelRenderer.setBackground(bgColor);
        repaint();
    }

    private void applyThemeRecursive(Component component, Color bgColor, Color fgColor, Color panelColor) {
        if (component instanceof JPanel || component instanceof JList || 
            component instanceof JSpinner || component instanceof JMenuBar) {
            component.setBackground(panelColor);
            if (component instanceof JComponent) {
                ((JComponent) component).setForeground(fgColor);
            }
        } else if (component instanceof JLabel || component instanceof JButton || 
                   component instanceof JMenuItem || component instanceof JMenu) {
            component.setForeground(fgColor);
            if (component instanceof JButton || component instanceof JPanel) {
                component.setBackground(panelColor);
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeRecursive(child, bgColor, fgColor, panelColor);
            }
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "3D Model Viewer\n\n" +
            "A simple application for viewing and editing 3D models in OBJ format.\n\n" +
            "Features:\n" +
            "• Load and save OBJ files\n" +
            "• Multiple model support\n" +
            "• Delete vertices and polygons\n" +
            "• Light/Dark theme\n\n" +
            "Version 1.0",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this,
            message,
            title,
            JOptionPane.ERROR_MESSAGE);
    }
}
