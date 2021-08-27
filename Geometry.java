package polygo;
import java.util.*;
import java.awt.*;
import java.io.Serializable;

/**
 * A helper class with various functions related to the math and geometry of the
 * nested polygon pattern.
 */
public class Geometry {

    /* A point with floating point indices. */
    public static class PointD implements Serializable {
        double x, y;

        public PointD(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public PointD() {
            x = 0;
            y = 0;
        }

        void set(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        /* Convert to an integer Point object, as used by Graphics2D. */
        public Point toIntPoint() {
            return(new Point((int)Math.round(x), (int)Math.round(y)));
        }
        
        @Override public String toString() {
            return("x: " + x + " ,y: " + y);
        }
    }

    /* Add a vector defined using polar coordinates (distance. angle). */
    public static PointD addPointDPolar(PointD point, double dist, double orientationDegrees) {
        PointD sumPoint = new PointD();
        sumPoint.set(point.x + dist * Math.cos(Math.toRadians(orientationDegrees)),
            point.y + dist * Math.sin(Math.toRadians(orientationDegrees)));
        return sumPoint;
    }        

    /* A polygon with floating point (PointD) vertices. */
    public static class PolygonD implements Serializable {
        PointD[] vertices;
        
        public PolygonD(int numSides) {
            vertices = new PointD[numSides];
            for (int s = 0; s < numSides; s++)
                vertices[s] = new PointD();
        }
        
        public void set(int index, PointD value) {
            vertices[index].set(value.x, value.y);
        }
        
        public void set(int index, double x, double y) {
            vertices[index].set(x, y);
        }
        
        public PointD get(int index) {
            return(vertices[index]);
        }
        
        public int length() {
            return vertices.length;
        }
        
        /* Convert to an integer Polygon object, as used in Graphics2D. */
        public Polygon toIntPolygon() {
            Polygon polygon = new Polygon();
            for (int v = 0; v < vertices.length; v++)
                polygon.addPoint((int)Math.round(vertices[v].x), (int)Math.round(vertices[v].y));
            return(polygon);
        }
        
        /* Find the left-, right, top- and bottom-most points. */
        
        public double getMinX() {
            double[] xArray;
            int numVertices = vertices.length;
            xArray = new double[numVertices];
            for (int v = 0; v < numVertices; v++)
                xArray[v] = vertices[v].x;
            Arrays.sort(xArray);
            return(xArray[0]);
        }
    
        public double getMaxX() {
            double[] xArray;
            int numVertices = vertices.length;
            xArray = new double[numVertices];
            for (int v = 0; v < numVertices; v++)
                xArray[v] = vertices[v].x;
            Arrays.sort(xArray);
            return(xArray[numVertices - 1]);
        }

        public double getMinY() {
            double[] yArray;
            int numVertices = vertices.length;
            yArray = new double[numVertices];
            for (int v = 0; v < numVertices; v++)
                yArray[v] = vertices[v].y;
            Arrays.sort(yArray);
            return(yArray[0]);
        }

        public double getMaxY() {
            double[] yArray;
            int numVertices = vertices.length;
            yArray = new double[numVertices];
            for (int v = 0; v < numVertices; v++)
                yArray[v] = vertices[v].y;
            Arrays.sort(yArray);
            return(yArray[numVertices - 1]);
        }
    
    }

    /* Create a regular polygon with floating point vertices. */
    public static PolygonD createRegularPolygon(int numSides) {
        PolygonD polygon = new PolygonD(numSides);
        PointD nextPoint = new PointD();
        double angle = (180 * (numSides - 2)) / numSides;
        double orientation = 0;
        for (int s = 0; s < numSides - 1; s++) {
            nextPoint = addPointDPolar(polygon.get(s), 1d, orientation);
            orientation += 180 - angle;
            polygon.set((s + 1) % numSides, nextPoint);
        }
        return(polygon);
    }

    /* Stretch polygon horizontally or vertically by an exponential factor. */
    public static PolygonD stretchPolygon(PolygonD polygon, double scalingFactor) {
        int numVertices = polygon.length();
        PolygonD strechedPolygon = new PolygonD(numVertices);
        for (int v = 0; v < numVertices; v++)
            strechedPolygon.set(v, polygon.get(v).x * scalingFactor, polygon.get(v).y);
        return(strechedPolygon);
    }

    /* Rotate polygon by a given angle. */
    public static PolygonD rotatePolygon(PolygonD polygon, int rotation) {
        double dist, orientation;
        PolygonD rotatedPolygon = new PolygonD(polygon.length());
        rotatedPolygon.set(0, polygon.get(0));
        int numVertices = polygon.length();
        int nextV;
        for (int v = 0; v < numVertices; v++) {
            nextV = (v + 1) % numVertices;
            dist = getDistance(polygon.get(v), polygon.get(nextV));
            orientation = getOrientation(polygon.get(v), polygon.get(nextV));
            rotatedPolygon.set(nextV, addPointDPolar(rotatedPolygon.get(v), dist, orientation + (double)rotation));
        }
        return(rotatedPolygon);
    }
    
    /* Change one angle of the polygon by adjusting the length of the adjoinging side
    and the other side of the next angle. */
    public static PolygonD changePolygonAngle(PolygonD polygon, int index, double newAngle) {
        int numVertices = polygon.length();
        PolygonD newPolygon = new PolygonD(numVertices);
        for (int v = 0; v < numVertices; v++)
            newPolygon.set(v, polygon.get(v));
        int nextIndex = (index + 1) % numVertices;
        PointD pointBefore = polygon.get((index - 1 + numVertices) % numVertices);
        PointD point = polygon.get(index);
        PointD pointAfter = polygon.get(nextIndex);
        PointD pointAfterAfter = polygon.get((index + 2) % numVertices);
        double side = getDistance(point, pointAfter);
        double angle = getAngle(pointBefore, point, pointAfter);
        double angleAfter = getAngle(point, pointAfter, pointAfterAfter);
        double angleDiff = newAngle - angle;
        double newDist = side * Math.sin(Math.toRadians(angleAfter)) / Math.sin(Math.toRadians(angleAfter - angleDiff));
        double orientation = getOrientation(point, pointAfter);
        double newOrientation = orientation - angleDiff;
        newPolygon.set(nextIndex, addPointDPolar(polygon.get(index), newDist, newOrientation));
        return(newPolygon);
    }
    
    /* Scale a polygon to a given canvas size. */
    public static PolygonD scalePolygon(PolygonD polygon, Dimension canvas, int margin) {
        int numSides = polygon.length();
        // Figure out the polygon's width and height relative to the canvas width and height.
        double verticesXSpan = polygon.getMaxX() - polygon.getMinX();
        double verticesYSpan = polygon.getMaxY() - polygon.getMinY();
        double xRatio = verticesXSpan / (double)(canvas.width - 2 * margin);
        double yRatio = verticesYSpan / (double)(canvas.height - 2 * margin);
        PolygonD scaledPolygon = new PolygonD(numSides);
        double minX = polygon.getMinX();
        double minY = polygon.getMinY();
        // scale and shift polygon to fit into canvas area
        for (int s = 0; s < numSides; s++) {
            // Normalize the polygon's coordinates to the leftmost and topmost coordinates.
            polygon.set(s, polygon.get(s).x - minX, polygon.get(s).y - minY);
            // scale to the smaller dimension: width or height.
            if (xRatio < yRatio)
                scaledPolygon.set(s, (int)Math.round(polygon.get(s).x / yRatio) + margin +
                    (canvas.width - 2 * margin - (int)Math.round(verticesXSpan / yRatio)) / 2,
                    (int)Math.round(polygon.get(s).y / yRatio) + margin);
            else
                scaledPolygon.set(s, (int)Math.round(polygon.get(s).x / xRatio) + margin,
                    (int)Math.round(polygon.get(s).y / xRatio) + margin +
                    (canvas.height - 2 * margin - (int)Math.round(verticesYSpan / xRatio)) / 2);
        }
        return(scaledPolygon);
    }

    /* Find the next inner polygon in the pattern based on the drawing scheme.
    This is done by finding the orientation of each side, then finding the point
    along that side at the distance (absolute or relative to the current side length)
    specified in the scheme. Everything is done using floating point numbers, which
    are only rounded when they are actually drawn. */
    public static PolygonD findNextPolygon(PolygonD polygon, DrawingScheme drawingScheme, double minDisplacement) {
        int numVertices = drawingScheme.numSides;
        PolygonD nextPolygon = new PolygonD(numVertices);
        double orientation, sideLength;
        // vertexDistances holds the distances between the vertices of the inner polygon and its parent.
        // When those distances are small enough, drawing will be terminated.
        double[] vertexDistances = new double[numVertices];
        PointD currentVertex = new PointD(), nextVertex = new PointD(), newVertex = new PointD();
        for (int v = 0; v < numVertices; v++) {
            currentVertex = polygon.get(v);
            if (drawingScheme.direction == DrawingScheme.Direction.CLOCKWISE) {
                nextVertex = polygon.get((v + 1) % numVertices);
            } else if (drawingScheme.direction == DrawingScheme.Direction.COUNTERCLOCKWISE) {
                nextVertex = polygon.get((v - 1 + numVertices) % numVertices);
            }
            orientation = getOrientation(currentVertex, nextVertex);
            sideLength = getDistance(currentVertex, nextVertex);
            if (drawingScheme.displacementType == DrawingScheme.DisplacementType.RELATIVE) {
                newVertex = addPointDPolar(currentVertex,
                        sideLength * drawingScheme.displacement / 100, orientation);
            } else if (drawingScheme.displacementType == DrawingScheme.DisplacementType.FIXED) {
                if (drawingScheme.displacement >= sideLength - 1) {
                    nextPolygon = null;
                    return (nextPolygon);
                }
                newVertex = addPointDPolar(currentVertex,
                        drawingScheme.displacement, orientation);
            }
            vertexDistances[v] = getDistance(currentVertex, newVertex);
            nextPolygon.set(v, newVertex);
        }
        // Sort to find the maximal distance. When all distance are below the mimimum,
        // assign null to the next polygon to trigger termination of the drawing loop.
        Arrays.sort(vertexDistances);
        if (vertexDistances[numVertices - 1] < minDisplacement) {
            nextPolygon = null;
        }
        return (nextPolygon);
    }
    
    /* Angles of a regular polygon with n sides. */
    protected static double regularPolygonAngle(int numSides) {
        return (180d - (360d / numSides));
    }

    /* Find the angles defined by three points. */
    public static double getAngle(PointD point1, PointD point2, PointD point3) {
        double angle1 = getOrientation(point1, point2);
        double angle2 = getOrientation(point2, point3);
        double angle = (angle2 - angle1);
        angle = ((180 - angle) + 180) % 180;
        return(angle);
    }
    
    /* Find the orientation of a line defined by two points. */
    public static double getOrientation(PointD point1, PointD point2) {
        double orientation = Math.atan2(point2.y - point1.y, point2.x - point1.x);
        return(Math.toDegrees(orientation));
    }
    
    /* Find the Euclidean distance between two points. */ 
    public static double getDistance(PointD point1, PointD point2) {
        return(Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2)));
    }
}