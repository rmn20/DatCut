package vectorcut;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 *
 * @author Roman Lahin
 */
public class DatCut {
    
    public static Random rand = new Random(System.currentTimeMillis());
    
    public static int paletteSize =  256;
    public static boolean fastLSR = true, dither = true;
    
    public static int ditherW = 3, ditherH = 2;
    
    //Stucki
    /*public static float[][] ditherMatrix = new float[][]{
        new float[]{0, 0, 0, 8 / 42f, 4 / 42f},
        new float[]{2 / 42f, 4 / 42f, 8 / 42f, 4 / 42f, 2 / 42f},
        new float[]{1 / 42f, 2 / 42f, 4 / 42f, 2 / 42f, 1 / 42f}
    };*/
    //Floyd–Steinberg
    public static float[][] ditherMatrix = new float[][]{
        new float[]{0, 0, 7f / 16},
        new float[]{3f / 16f, 5f / 16, 1f / 16},
    };
    //Just testin
    /*public static float[][] ditherMatrix = new float[][]{
        new float[]{0, 0, 0.25f},
        new float[]{0.25f, 0.25f, 0.25f},
    };*/

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("DatCut");
            System.out.println("Enter the paths to the images as arguments.");
            System.out.println("Use -p to change palette size. (from 1 to 256 colors) (default is 256)");
            System.out.println("Use -fastlsr 0/1 to use fast least square regression method");
            System.out.println("Use -dither 0/1 to enable dithering. (default is 0)");
            (new Scanner(System.in)).nextLine();
            return;
        }
        
        for(int i=0; i<args.length; i++) {
            String arg = args[i];
            
            if(arg.startsWith("-")) {
                if(arg.equals("-p")) {
                    i++;
                    paletteSize = Integer.parseInt(args[i]);
                } else if(arg.equals("-fastlsr")) {
                    i++;
                    fastLSR = Integer.parseInt(args[i]) == 1;
                } else if(arg.equals("-dither")) {
                    i++;
                    dither = Integer.parseInt(args[i]) == 1;
                }
            }
        }
        
        for(int i=0; i<args.length; i++) {
            String arg = args[i];
            if(arg.startsWith("-")) {
                i++; continue;
            }
            
            try {
                Bucket.totalDist = 0;
                Color[][] image = loadPNG(arg);
                Color[] colors = buildPalette(image);
                exportPNG(image, colors, arg.substring(0, arg.lastIndexOf('.')) + "-compressed.png");
                System.out.println("Total lsr error: "+Bucket.totalDist);
                
            } catch(Throwable e) {
                e.printStackTrace();
                (new Scanner(System.in)).nextLine();
            }
        }
    }
    
    static void loadDither() throws Exception {
        BufferedImage img = ImageIO.read((new Object()).getClass().getResourceAsStream("/blue_noise.png"));
        ditherW = img.getWidth();
        ditherH = img.getHeight();
        
        int[] tmp = new int[ditherW*ditherH];
        img.getRGB(0, 0, ditherW, ditherH, tmp, 0, ditherW);
        
        ditherMatrix = new float[ditherH][ditherW];
        
        float total = 0;
        for(int x=0; x<ditherW; x++) {
            for(int y=0; y<ditherH; y++) {
                int v = (tmp[x+y*ditherW]&0xff);
                ditherMatrix[y][x] = v;
                total += v;
            }
        }
        
        for(int x=0; x<ditherW; x++) {
            for(int y=0; y<ditherH; y++) {
                ditherMatrix[y][x] /= total;
            }
        }
    }
    
    static Color[][] loadPNG(String path) throws Exception {
        File file = new File(path);
        if(!file.exists()) throw new Exception("File doesn't exist!");
        
        BufferedImage img = ImageIO.read(file);
        int imgW = img.getWidth(), imgH = img.getHeight();
        
        int[] rawColors = new int[imgW*imgH];
        img.getRGB(0, 0, imgW, imgH, rawColors, 0, imgW);
        
        Color[][] colors = new Color[imgW][imgH];
        for(int i=0; i<rawColors.length; i++) {
            int col = rawColors[i];
            
            colors[i%imgW][i/imgW] = new Color(col, true);
        }
        
        return colors;
    }

    static Color[] buildPalette(Color[][] image) {
        Vector<Bucket> buckets = new Vector();
        Vector<Color> colors = new Vector();
        boolean hasAlpha = false;
        
        Bucket firstBucket = new Bucket();
        
        int imgW = image.length, imgH = image[0].length;
        for(int x=0; x<imgW; x++) {
            for(int y=0; y<imgH; y++) {
                Color c = image[x][y];
                
                if(c.getAlpha() == 0) {
                    hasAlpha = true;
                    continue;
                }
                
                firstBucket.add(new Vector3D(c));
            }
        }
        
        if(firstBucket.colorsVector.size() > 0) {
            firstBucket.compute();
            buckets.add(firstBucket);
        }
        if(hasAlpha) {
            colors.add(new Color(0, 0, 0, 0));
            paletteSize--;
        }
        
        while(buckets.size() < paletteSize) {
            System.out.println(buckets.size() + " / " + paletteSize);
            
            Bucket longestBucket = null;
            double maxLength = -Double.MAX_VALUE;
            
            for(Bucket bucket : buckets) {
                int bucketColors = bucket.colors.length;
                double len = bucket.length * bucketColors;
                if(bucketColors > 1 && len > maxLength) {
                    maxLength = len;
                    longestBucket = bucket;
                }
            }
            
            if(longestBucket == null) break;
            
            Bucket b1 = new Bucket();
            Bucket b2 = new Bucket();
            longestBucket.divide(b1, b2);
            
            buckets.removeElement(longestBucket);
            buckets.add(b1);
            buckets.add(b2);
        }
        
        for(Bucket bucket : buckets) {
            colors.add(/*buildRGB(*/bucket.getColor()/*)*/);
        }
        
        //return new Color[]{Color.white, Color.black};
        return colors.toArray(new Color[colors.size()]);
    }

    static void exportPNG(Color[][] image, Color[] palette, String path) throws Exception {
        int imgW = image.length, imgH = image[0].length;
        
        byte[] r = new byte[palette.length],
                g = new byte[palette.length],
                b = new byte[palette.length],
                a = new byte[palette.length];
        
        for(int i=0; i<palette.length; i++) {
            Color c = palette[i];
            
            r[i] = (byte) c.getRed();
            g[i] = (byte) c.getGreen();
            b[i] = (byte) c.getBlue();
            a[i] = (byte) c.getAlpha();
        }
        
        IndexColorModel cm = new IndexColorModel(8, palette.length, r, g, b, a);
        BufferedImage bImg = new BufferedImage(imgW, imgH, BufferedImage.TYPE_BYTE_INDEXED, cm);
        
        Vector3D[][] editableImg = new Vector3D[imgW][imgH];
        
        for(int x=0; x<imgW; x++) {
            for(int y=0; y<imgH; y++) {
                Color c = image[x][y];
                editableImg[x][y] = new Vector3D(c);
            }
        }
        
        for(int y=0; y<imgH; y++) {
            
            for(int x=0; x<imgW; x++) {
                Vector3D oldPix = editableImg[x][y];
                Color newPix = findClosest(palette, oldPix, image[x][y].getAlpha());
                bImg.setRGB(x, y, newPix.getRGB());
                
                if(newPix.getAlpha() != 0 && dither) {
                    Vector3D quantError = new Vector3D(oldPix);
                    quantError.sub(newPix.getRed(), newPix.getGreen(), newPix.getBlue());
                    
                    int minx = Math.max(0, x-ditherW/2), maxx = Math.min(imgW-1, x+ditherW/2);
                    int miny = y, maxy = Math.min(imgH-1, y+ditherH-1);
                    
                    for(int xx=minx, dx=minx-(x-ditherW/2); xx<=maxx; xx++, dx++) {
                        for(int yy=miny, dy=0; yy<=maxy; yy++, dy++) {
                            Vector3D oldColor = editableImg[xx][yy];
                            double v = ditherMatrix[dy][dx];
                            
                            oldColor.add(
                                    quantError.x * v,
                                    quantError.y * v,
                                    quantError.z * v
                            );
                            
                            /*oldColor.x = Math.min(255, Math.max(0, oldColor.x));
                            oldColor.y = Math.min(255, Math.max(0, oldColor.y));
                            oldColor.z = Math.min(255, Math.max(0, oldColor.z));*/
                        }
                    }
                }
            }
        }
        
        ImageIO.write(bImg, "png", new File(path));
    }

    static Color findClosest(Color[] palette, Vector3D c, int alpha) {
        int minDist = Integer.MAX_VALUE;
        Color closest = null;
        
        for(Color palCol : palette) {
            int dist = distance(palCol, c, alpha);
            
            if(dist < minDist) {
                minDist = dist;
                closest = palCol;
            }
        }
        
        return closest;
    }

    static int distance(Color palCol, Vector3D c, int alpha) {
        if(palCol.getAlpha() == 0 || alpha == 0) {
            if(palCol.getAlpha() == alpha) return 0;
            else return Integer.MAX_VALUE;
        }
        double cr = c.x, cg = c.y, cb = c.z;
        
        double r = (palCol.getRed() - cr) / 2;
        double C = (2+r/256)*(palCol.getRed()- cr)*(palCol.getRed()- cr)
                + 4*(palCol.getGreen()- cg)*(palCol.getGreen()- cg)
                + (2+(255-r)/256)*(palCol.getBlue()- cb)*(palCol.getBlue()- cb);
        
        return (int)Math.round(C);
    }
    
}
