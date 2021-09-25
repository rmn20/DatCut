package vectorcut;

import java.awt.Color;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Bucket {
    
    static Vector3D[][] normals = new Vector3D[361][361];
    static {
        
        for(int yRot=0; yRot<=360; yRot++) {
            double xx = Math.cos(Math.toRadians(yRot/2));
            double zz = Math.sin(Math.toRadians(yRot/2));
            
            for(int xRot=-180; xRot<=180; xRot++) {
                double cos = Math.cos(xRot/2);
                normals[yRot][xRot+180] = new Vector3D(xx*cos, Math.sin(xRot/2), zz*cos);
            }
        }
    }
    public static double totalDist;
    
    public Vector3D n = new Vector3D();
    public Vector3D vectorPos = new Vector3D();
    public double length = 0;
    
    Vector<Vector3D> colorsVector = new Vector();
    Vector3D[] colors;
    
    public Bucket() {}
    
    public void compute() {
        //long start = System.currentTimeMillis();
        colors = colorsVector.toArray(new Vector3D[colorsVector.size()]);
        
        if(colors.length < 2) {
            length = 0;
            
            return;
        } else if(colors.length == 2) {
            Vector3D c1 = colors[0];
            Vector3D c2 = colors[1];
            
            vectorPos.div(2, 2, 2);
            n.build(c1, c2);
            length = c1.distance(c2);
            
            return;
        }
        
        vectorPos.div(colors.length, colors.length, colors.length);
        
        double minDist = Double.MAX_VALUE;
        Vector3D bestNormal = new Vector3D();
        
        if(!DatCut.fastLSR) {
            
            for(int yRot = 0; yRot < 360; yRot += 4) {
                for(int xRot = 0; xRot < 360; xRot += 4) {
                    Vector3D tmpNormal = normals[yRot][xRot];

                    double dist = rate(tmpNormal);
                    if(dist < minDist) {
                        minDist = dist;
                        bestNormal = tmpNormal;
                    }
                }
            }
            
        } else {
        
            n.set(0, 1, 0);
            int iterations = Math.min(400, Math.max(200, 200 * 65536 / colors.length));

            for(int i = 0; i < iterations; i++) {
                double nextDirectionx = 0, 
                        nextDirectiony = 0, 
                        nextDirectionz = 0;
                double nextCenterx = 0, 
                        nextCentery = 0, 
                        nextCenterz = 0;
                double dotSumm = 0;

                for(Vector3D p : colors) {
                    double centeredPointx = p.x - vectorPos.x,
                            centeredPointy = p.y - vectorPos.y,
                            centeredPointz = p.z - vectorPos.z;

                    double dot = centeredPointx * n.x + 
                            centeredPointy * n.y + 
                            centeredPointz * n.z;
                    nextDirectionx += dot * centeredPointx;
                    nextDirectiony += dot * centeredPointy;
                    nextDirectionz += dot * centeredPointz;
                
                    nextCenterx += Math.abs(dot) * p.x;
                    nextCentery += Math.abs(dot) * p.y;
                    nextCenterz += Math.abs(dot) * p.z;
                    dotSumm += Math.abs(dot);
                }
                n.set(nextDirectionx, nextDirectiony, nextDirectionz);
                n.normalize();
                
                if(dotSumm != 0) vectorPos.set(nextCenterx / dotSumm, nextCentery / dotSumm, nextCenterz / dotSumm);

                double dist = rate(n);
                if(dist < minDist) {
                    minDist = dist;
                    bestNormal.set(n);
                }
            }
        }
        
        n.set(bestNormal);
        totalDist += minDist;
        
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        
        for(Vector3D c : colors) {
            double pos = Vector3D.positionOnLine(n, vectorPos, c);
            
            if(pos > max) max = pos;
            if(pos < min) min = pos;
        }
        
        length = max - min;
    }
    
    private double rate(Vector3D n) {
        double addDist = 0;
        
        for(Vector3D c : colors) {
            addDist += Vector3D.distanceToVector(n, vectorPos, c);
        }
        
        return addDist;
    }
    
    public void divide(Bucket b1, Bucket b2) {
        if(colors.length == 2) {
            b1.add(colors[0]);
            b2.add(colors[1]);
        } else {
            Vector3D centerReal = new Vector3D();
            for(Vector3D c : colors) {
                centerReal.add(c);
            }
            centerReal.div(colors.length, colors.length, colors.length);
            Vector3D tmp = new Vector3D();
            Vector3D centerProject = Vector3D.projectToVector(n, vectorPos, centerReal);

            for(Vector3D color : colors) {
                tmp.set(color);
                tmp.sub(centerProject);
                //tmp.sub(center);

                if(tmp.dot(n) > 0) b1.add(color);
                else b2.add(color);
            }
        }
        
        b1.compute();
        b2.compute();
    }
    
    public void add(Vector3D color) {
        colorsVector.add(color);
        vectorPos.add(color);
    }
    
    public Color getColor() {
        Vector3D v = new Vector3D();
        
        for(Vector3D color : colors) {
            v.add(color);
        }
        v.div(colors.length, colors.length, colors.length);
        
        return v.toColor();
        //return v;
    }
}
