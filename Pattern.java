package polygo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import polygo.Geometry.*;


/* Pattern is the graphical implementation of a nested polygon pattern. */
public class Pattern extends JPanel implements ClipboardOwner {
    DrawingScheme drawingScheme;
    
    private static class DrawingParameters {
        // Margin from edges of panel.
        static final int CANVAS_MARGIN = 10;
        // Minimum displacement between neighbor polygons. When reached this
        // value, stop drawing.
        static final double MIN_DISPLACEMENT = 0.1;
    }

    public Pattern(DrawingScheme drawingSchemeIn) {
        super();
        drawingScheme = drawingSchemeIn;
    }

    /* Update pattern with a new drawing scheme. */
    public void newPattern(DrawingScheme drawingSchemeIn) {
        drawingScheme = drawingSchemeIn;
        update();
    }
    
    public void copyToClipboard() {  
       BufferedImage bufImage = new BufferedImage(getSize().width, getSize().height,BufferedImage.TYPE_INT_RGB);  
       paint(bufImage.createGraphics());
       TransferableImage trans = new TransferableImage(bufImage);
       Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
       c.setContents( trans, this );
    }  

    public void lostOwnership( Clipboard clip, Transferable trans ) {
    }

    private class TransferableImage implements Transferable {

        Image i;

        public TransferableImage( Image i ) {
            this.i = i;
        }

        public Object getTransferData( DataFlavor flavor )
        throws UnsupportedFlavorException, IOException {
            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for ( int i = 0; i < flavors.length; i++ ) {
                if ( flavor.equals( flavors[ i ] ) ) {
                    return true;
                }
            }

            return false;
        }
    }

    
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_SPEED);
        //RenderingHints.);
        // Fill the whole panel with the background color.
        g2.setColor(drawingScheme.backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Take the drawing scheme's base polygon (in arbitrary units), and scale it to fit
        // the current dimensions of the drawing panel.
        PolygonD basePolygon = Geometry.scalePolygon(drawingScheme.basePolygon, this.getSize(), DrawingParameters.CANVAS_MARGIN);
        // Need to keep track of which iteration is being drawn in order to select the right colors
        int iteration = 0;
        boolean reachedCenter = false;
        PolygonD currentPolygon, nextPolygon, nextNextPolygon;
        currentPolygon = basePolygon;
        while (!reachedCenter) {
            iteration++;
            // Calculate the vertices of the next polygon in the pattern.
            nextPolygon = Geometry.findNextPolygon(currentPolygon, drawingScheme, DrawingParameters.MIN_DISPLACEMENT);
            // nextPolygon == null means the center has been reached, so stop.
            // Also stop if reached the requested depth.
            if ((nextPolygon == null) 
                    || (!drawingScheme.infinite && (iteration > drawingScheme.iterations))) {
                reachedCenter = true;
            } else {
                if (!drawingScheme.infinite && (iteration == drawingScheme.iterations)) {
                    nextNextPolygon = null;
                } else {
                    nextNextPolygon = Geometry.findNextPolygon(nextPolygon, drawingScheme, DrawingParameters.MIN_DISPLACEMENT);
                }
                fillSpaceBetweenPolygons(g2, currentPolygon, nextPolygon, nextNextPolygon, drawingScheme, iteration - 1);
                drawPolygon(g2, currentPolygon, drawingScheme, iteration - 1);
                drawPolygon(g2, nextPolygon, drawingScheme, iteration);
                currentPolygon = nextPolygon;
            }
        }
        // Fill the last polygon.
        if (drawingScheme.innerFill) {
            g2.setColor(drawingScheme.innerFillColor);
        } else {
            g2.setColor(drawingScheme.backgroundColor);
        }
        g2.fillPolygon(currentPolygon.toIntPolygon());
        drawPolygon(g2, currentPolygon, drawingScheme, iteration);
    }

    public void update() {
        repaint();
    }

    /* Draw one polygon in the pattern based on the drawing scheme */
    private static void drawPolygon(Graphics2D g2, PolygonD polygon, DrawingScheme drawingScheme, int iteration) {
        int numSides = drawingScheme.numSides;
        int lineWidth = drawingScheme.lineWidth;
        Stroke stroke = new BasicStroke((float)lineWidth, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
        int nextIndex;
        Color lineColor = null;
        for (int s = 0; s < numSides; s++) {
            switch (drawingScheme.lineColorScheme) {
                case ONE_COLOR:
                    lineColor = drawingScheme.lineColors.get(0);
                    break;
                case ONE_SIDE_ONE_COLOR:
                    lineColor = drawingScheme.lineColors.get(s);
                    break;
                case ONE_POLYGON_ONE_COLOR:
                    // "iteration" represents the number of polygons drawn so far.
                    lineColor = drawingScheme.lineColors.get(iteration % drawingScheme.lineColors.size());
                    break;
                case CUSTOM:
                    // Count the total number of sides drawn up to now.
                    lineColor = drawingScheme.lineColors.get((iteration * numSides + s) % drawingScheme.lineColors.size());
                    break;
                case NONE:
                    break;
            }
            if (lineColor != null) {
                g2.setColor(lineColor);
                // Set the line width.
                g2.setStroke(stroke);
                nextIndex = (s + 1) % numSides;
                // Draw each line separately and not the whole polygon because each side might have a different color.
                g2.drawLine((int)Math.round(polygon.get(s).x), (int)Math.round(polygon.get(s).y),
                        (int)Math.round(polygon.get(nextIndex).x), (int)Math.round(polygon.get(nextIndex).y));
            }
        } // sides
    }

    /* Fill the space between the sides of two adjacent polygons */
    private static void fillSpaceBetweenPolygons(Graphics2D g2, PolygonD outerPolygon,
        PolygonD innerPolygon, PolygonD innerInnerPolygon, DrawingScheme drawingScheme, int iteration) {
        int numSides = drawingScheme.numSides;
        // fillPolygon is an integer polygon.
        Polygon fillPolygon = new Polygon();
        Color fillColor = null;
        Point outerPolygonPointRounded, innerPolygonPoint1Rounded, innerPolygonPoint2Rounded,
            innerInnerPolygonPointRounded;
        for (int s = 0; s < numSides; s++) {
            fillPolygon.reset();
            // Add two points from the inner polygon and one from the outer polygon.
            innerPolygonPoint1Rounded = innerPolygon.get(s).toIntPoint();
            fillPolygon.addPoint(innerPolygonPoint1Rounded.x, innerPolygonPoint1Rounded.y);
            if (drawingScheme.direction == DrawingScheme.Direction.CLOCKWISE) {
                outerPolygonPointRounded = outerPolygon.get((s + 1) % numSides).toIntPoint();
            } else { // counterclockwise
                outerPolygonPointRounded = outerPolygon.get(s).toIntPoint();
            }
            fillPolygon.addPoint(outerPolygonPointRounded.x, outerPolygonPointRounded.y);
            innerPolygonPoint2Rounded = innerPolygon.get((s + 1) % numSides).toIntPoint();
            fillPolygon.addPoint(innerPolygonPoint2Rounded.x, innerPolygonPoint2Rounded.y);
            switch (drawingScheme.fillColorScheme) {
                case ONE_COLOR:
                    fillColor = drawingScheme.fillColors.get(0);
                    break;
                case ONE_SIDE_ONE_COLOR:
                    fillColor = drawingScheme.fillColors.get(s);
                    break;
                case ONE_POLYGON_ONE_COLOR:
                    // "iteration" represents the number of polygons drawn so far.
                    fillColor = drawingScheme.fillColors.get(iteration % drawingScheme.fillColors.size());
                    break;
                case CUSTOM:
                    // Count the total number of sides drawn up to now.
                    fillColor = drawingScheme.fillColors.get((iteration * numSides + s) % drawingScheme.fillColors.size());
                    break;
                case NONE:
                    fillColor = drawingScheme.backgroundColor;
                    break;
            }
            // I added a point from the inner inner polygon to avoid some points along the side
            // of the inner polygon remaining blank. Therefore, the polygon being painted is in fact
            // a four sided polygon. Comment out the next statement to see the blank lines.
            if (innerInnerPolygon != null) {
                if (drawingScheme.direction == DrawingScheme.Direction.CLOCKWISE) {
                    innerInnerPolygonPointRounded = innerInnerPolygon.get((s + 1) % numSides).toIntPoint();
                } else {
                    innerInnerPolygonPointRounded = innerInnerPolygon.get(s).toIntPoint();
                }
                fillPolygon.addPoint(innerInnerPolygonPointRounded.x, innerInnerPolygonPointRounded.y);
            }
            g2.setColor(fillColor);
            g2.fillPolygon(fillPolygon);
        }
    }
} // Pattern
