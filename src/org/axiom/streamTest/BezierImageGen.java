package org.axiom.streamTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class BezierImageGen {
    private final int[] pixelArray;
    private final Point imageSize;

    enum MODIFICATIONS {ROTATE, SCALE, MIRROR}


    public BezierImageGen(Point imageSize, Color fillColor) {
        this.imageSize = imageSize;
        this.pixelArray = new int[imageSize.x * imageSize.y];
        for (int i = 0; i < pixelArray.length ; i++)
            pixelArray[i] = fillColor.getRGB();
    }


    /**
     * Writes approximation of the bezier line into the list of PointF,
     * then apply the modifications, them writes list into the image buffer
     */
    void drawBezierLine(PointF[] mesh, Color drawColor, Object ... mods) {
        PointF[][] points = new PointF[mesh.length][mesh.length];
        //  points[n][x] = x-th point of the n-th level of recursion

        //  List to store Points for further modifications
        List<PointF> line = new LinkedList<>();
        line.add(mesh[0]);

        //  write first line of points matrix with input points -
        //  logically they are first recursion level
        System.arraycopy(mesh, 0, points[0], 0, mesh.length);

        //  Since we draw not pixels, but lines,
        //  here is previous point to construct the line
        PointF previousPoint = mesh[0];

        //  Since we draw lines, not points, less precision is required
        //  float step = 1.f /  (float)Math.pow(Math.max(Math.abs(mesh[0].x - mesh[mesh.length-1].x), Math.abs(mesh[0].y - mesh[mesh.length-1].y)), mesh.length-1);
        float step = 0.01f;

        for(float t = 0.0f ; t<1.f ; t+=step) {
            //  Calculating new points into @points matrix
            //  The final 1 point is what we have to draw on tha canvas
            int n = mesh.length;
            for(int i=1 ; i<mesh.length ; ++i) {
                //  calculating n-i points from the points on the upper level
                for(int j=0 ; j<n-i ; ++j)
                    points[i][j] = newPoint(points[i-1][j], points[i-1][j+1], t);

            }
            //  putPixel(points[mesh.length-1][0], drawColor);
            //  putLine(previousPoint, points[mesh.length-1][0], drawColor);
            //  previousPoint = points[mesh.length-1][0];
            line.add(points[mesh.length-1][0]);
        }

        line.add(mesh[mesh.length - 1]);

        applyModifications(line, mods);
        putIntoArr(line, drawColor);
    }


    /**
     * Reads array of modifiers, applies given modifications to the list of points
     */
    private void applyModifications(List<PointF> points, Object ... mods) {
        int currentMod = 0;
        float[][] matrix = new float[2][2];
        while(mods.length > currentMod) {
            PointF tempPoint = points.get(0).clone();
            //  Mirrors line around first point --> last pint line
            if(mods[currentMod] == MODIFICATIONS.MIRROR) {
                //  todo fill matrix
                for(int i=0 ; i<points.size() ; ++i)
                    pointMultiplication(points.get(i), matrix, tempPoint);
                currentMod++;
            }
            else if(mods[currentMod] == MODIFICATIONS.SCALE) {
                //  matrix[0, 0] and [1, 1] is a scaling coefficients, that must be present in the modification
                //  aka SCALE, 3.f, 2.1f
                matrix[0][0] = (Float)mods[++currentMod];
                matrix[1][1] = (Float)mods[++currentMod];
                matrix[0][1] = .0f;
                matrix[1][0] = .0f;

                for (PointF point : points) {
                    pointMultiplication(point, matrix, tempPoint);
                }
                currentMod++;
            }
            else if(mods[currentMod] == MODIFICATIONS.ROTATE) {
                float angle = (Float)mods[++currentMod];

                float cosfi = (float)Math.cos(angle);
                float sinfi = (float)Math.sin(angle);

                matrix[0][0] = cosfi;
                matrix[0][1] = sinfi;
                matrix[1][0] = -sinfi;
                matrix[1][1] = cosfi;
                //  this will rotate around center of the coordinates

                for (PointF point : points) {
                    pointMultiplication(point, matrix, tempPoint);
                }
                currentMod++;
            }
        }
    }


    /**
     * Writes lines between line points using the DDA algorithm into the image buffer
     */
    private void putIntoArr(List<PointF> line, Color drawColor) {
        PointF previous = line.get(0);
        for(int i=1 ; i<line.size() ; ++i) {
            putLine(previous, line.get(i), drawColor);
            previous = line.get(i);
        }

    }

    /**
     * Moves point at the beginning of the coordinates, multiplies point on an matrix, and moves it back
     */
    private void pointMultiplication(PointF point, float[][] matrix, PointF firstPoint) {
        float xDiff = firstPoint.x;
        float yDiff = firstPoint.y;
        point.y -= yDiff;
        point.x -= xDiff;

        point.x = matrix[0][0] * point.x + matrix[1][0] * point.y;
        point.y = matrix[0][1] * point.x + matrix[1][1] * point.y;

        point.y += yDiff;
        point.x += xDiff;

    }


    public void writeIntoFile(File path) {
        try{
            BufferedImage image = new BufferedImage(imageSize.x, imageSize.y, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0,0, imageSize.x, imageSize.y, pixelArray,0, imageSize.x);
            ImageIO.write(image, "png", path);
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }

    }


    //  t in [0, 1]
    private PointF newPoint(PointF a, PointF b, float t) {
        return new PointF(interpolate(a.x, b.x, t), interpolate(a.y, b.y, t));
    }


    //  t in [0, 1]
    private float interpolate(float a, float b, float t) {
        return a*(1.f-t) + b*t;
    }


    private void putPixel(PointF place, Color drawColor) {
        if(place.x > imageSize.x || place.x < 0) return;
        if(place.y > imageSize.y || place.y < 0) return;
        pixelArray[imageSize.x* (int)place.y + (int)place.x] = drawColor.getRGB();
    }


    // DDA algorithm to draw the line
    private void putLine(PointF a, PointF b, Color drawColor) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float step = Math.max(Math.abs(dx), Math.abs(dy));
        float x, y;

        dx /= step;
        dy /= step;
        x = a.x;
        y = a.y;

        for (int i = 0; i <= step ; ++i) {
            putPixel(new PointF(x, y), drawColor);
            x += dx;
            y += dy;
        }

    }


    public static class PointF {
        public float x;
        public float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }


        @Override
        public PointF clone() {
            return new PointF(this.x, this.y);
        }
    }

}
