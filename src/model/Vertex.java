package model;

/**
 * Класс для представления вершины 3D модели
 */
public class Vertex {
    private double x;
    private double y;
    private double z;
    // UV-координаты для текстурирования
    private double u = 0.0;
    private double v = 0.0;
    // Нормали для освещения
    private double nx = 0.0;
    private double ny = 0.0;
    private double nz = 1.0;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(double x, double y, double z, double u, double v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
    }

    public Vertex(double x, double y, double z, double u, double v, double nx, double ny, double nz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }
    public double getU() {
        return u;
    }

    public double getV() {
        return v;
    }

    public void setU(double u) {
        this.u = u;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getNx() {
        return nx;
    }

    public double getNy() {
        return ny;
    }

    public double getNz() {
        return nz;
    }

    public void setNormal(double nx, double ny, double nz) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
    // Вычисление длины нормали
    public double normalLength() {
        return Math.sqrt(nx * nx + ny * ny + nz * nz);
    }
    // Нормализация нормали
    public void normalizeNormal() {
        double len = normalLength();
        if (len > 1e-8) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
    }

    @Override
    public String toString() {
        return String.format("v %.6f %.6f %.6f vt %.6f %.6f", x, y, z, u, v);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertex vertex = (Vertex) obj;
        return Double.compare(vertex.x, x) == 0 &&
               Double.compare(vertex.y, y) == 0 &&
               Double.compare(vertex.z, z) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
