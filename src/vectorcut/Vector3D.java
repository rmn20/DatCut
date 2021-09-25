package vectorcut;

import java.awt.Color;

/**
 *
 * @author Roman Lahin
 */
public class Vector3D {
    
    double x, y, z;
    
    public Vector3D() {}
    
    public Vector3D(double x, double y, double z) {
        set(x, y, z);
    }
    
    public Vector3D(Vector3D v) {
        set(v);
    }
    
    public Vector3D(Color c) {
        set(c.getRed(), c.getGreen(), c.getBlue());
    }
    
    public Color toColor() {
        return toColor(x, y, z);
    }
    
    public void set(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }
    
    public void set(Vector3D v) {
        this.x = v.x; this.y = v.y; this.z = v.z;
    }
    
    public void add(double x, double y, double z) {
        this.x += x; this.y += y; this.z += z;
    }
    
    public void add(Vector3D v) {
        this.x += v.x; this.y += v.y; this.z += v.z;
    }
    
    public void sub(double x, double y, double z) {
        this.x -= x; this.y -= y; this.z -= z;
    }
    
    public void sub(Vector3D v) {
        this.x -= v.x; this.y -= v.y; this.z -= v.z;
    }
    
    public void mul(double x, double y, double z) {
        this.x *= x; this.y *= y; this.z *= z;
    }
    
    public void mul(Vector3D v) {
        this.x *= v.x; this.y *= v.y; this.z *= v.z;
    }
    
    public void div(double x, double y, double z) {
        this.x /= x; this.y /= y; this.z /= z;
    }
    
    public void div(Vector3D v) {
        this.x /= v.x; this.y /= v.y; this.z /= v.z;
    }
    
    public void build(Vector3D a, Vector3D b) {
        double len = a.distance(b);
        x = (b.x - a.x) / len;
        y = (b.y - a.y) / len;
        z = (b.z - a.z) / len;
    }
    
    public void normalize() {
        double len = Math.sqrt(x*x + y*y + z*z);
        if(len == 0) return;
        x /= len; y /= len; z /= len;
    }
    
    public double distance(Vector3D v) {
        return Math.sqrt((v.x-x)*(v.x-x) + (v.y-y)*(v.y-y) + (v.z-z)*(v.z-z));
    }
    
    public double distanceSqr(Vector3D v) {
        return (v.x-x)*(v.x-x) + (v.y-y)*(v.y-y) + (v.z-z)*(v.z-z);
    }
    
    public double dot(Vector3D v) {
        return x*v.x + y*v.y + z*v.z;
    }
    
    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }
    
    static final Vector3D projected = new Vector3D();
    
    public static double distanceToVector(Vector3D normal, Vector3D start, Vector3D pos) {
        double dt = 
                (pos.x - start.x) * normal.x + 
                (pos.y - start.y) * normal.y + 
                (pos.z - start.z) * normal.z;
        
        double x = normal.x * dt + start.x - pos.x;
        double y = normal.y * dt + start.y - pos.y;
        double z = normal.z * dt + start.z - pos.z;
        
        return Math.sqrt(x*x + y*y + z*z);
    }
    
    public static double positionOnLine(Vector3D normal, Vector3D start, Vector3D pos) {
        return (pos.x - start.x) * normal.x + 
                (pos.y - start.y) * normal.y + 
                (pos.z - start.z) * normal.z;
    }
    
    public static Vector3D projectToVector(Vector3D normal, Vector3D start, Vector3D pos) {
        double dt = 
                (pos.x - start.x) * normal.x + 
                (pos.y - start.y) * normal.y + 
                (pos.z - start.z) * normal.z;
        
        return new Vector3D(normal.x * dt + start.x, normal.y * dt + start.y, normal.z * dt + start.z);
    }
    
    public static Color toColor(double x, double y, double z) {
        return new Color(
                (int) Math.max(0, Math.min(255, Math.round(x))),
                (int) Math.max(0, Math.min(255, Math.round(y))),
                (int) Math.max(0, Math.min(255, Math.round(z))),
                255
        );
    }

}
