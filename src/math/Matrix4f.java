package math;

public final class Matrix4f {
    private final float[][] m = new float[4][4];
    
    public Matrix4f() {
        identity();
    }
    
    public void identity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Matrix index out of bounds: " + row + ", " + col);
        }
        m[row][col] = value;
    }
    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Matrix index out of bounds: " + row + ", " + col);
        }
        return m[row][col];
    }
    
    public Matrix4f multiply(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        result.identity(); 
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0.0f;
                for (int k = 0; k < 4; k++) {
                    sum += m[i][k] * other.m[k][j];
                }
                result.m[i][j] = sum;
            }
        }
        return result;
    }
    
    public Vector4f multiply(Vector4f vec) {
        return new Vector4f(
            m[0][0]*vec.x + m[0][1]*vec.y + m[0][2]*vec.z + m[0][3]*vec.w,
            m[1][0]*vec.x + m[1][1]*vec.y + m[1][2]*vec.z + m[1][3]*vec.w,
            m[2][0]*vec.x + m[2][1]*vec.y + m[2][2]*vec.z + m[2][3]*vec.w,
            m[3][0]*vec.x + m[3][1]*vec.y + m[3][2]*vec.z + m[3][3]*vec.w
        );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("| %.3f %.3f %.3f %.3f |\n", m[i][0], m[i][1], m[i][2], m[i][3]));
        }
        return sb.toString();
    }
}
