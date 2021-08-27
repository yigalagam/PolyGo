package polygo;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import polygo.Geometry.*;

/* DrawingScheme is a data structure outlining how the pattern should be drawn.
It is used by the control panel to arrange the GUI and draw the pattern. */
public class DrawingScheme implements Serializable {
    // Number of sides in each polygon.
    int numSides;
    // Rotation of the base (most exterior) polygon.
    int rotation;
    // Polygon angles
    double[] angles;
    // Constants controlling width/height ratio
    int aspectRatioX, aspectRatioY;
    // Type of displacement of each new polygon relative to its container polygon:
    // percent of side length or a fixed number of pixels.
    static enum DisplacementType {
        RELATIVE,
        FIXED
    }
    DisplacementType displacementType;
    int displacement;
    // Direction (CW/CCW) in which each polygon is rotated with respect to its container polygon.
    static enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }
    Direction direction;
    // Depth of nesting (how many polygons to draw).
    // Can be a finite number, or infinite until reaching the center point.
    boolean infinite;
    int iterations;
    // Line and fill colors: can be a single color, vary by side or by polygon, or custom.
    static enum ColorScheme {
        ONE_COLOR { @Override public String toString() {
            return("One Color"); } },
        ONE_SIDE_ONE_COLOR { @Override public String toString() {
            return("One Side, One Color"); } },
        ONE_POLYGON_ONE_COLOR { @Override public String toString() {
            return("One Polygon, One Color"); } },
        CUSTOM { @Override public String toString() {
            return("Custom"); } },
        NONE { @Override public String toString() {
            return("None"); } };
    }
    // Line attributes: color and length.
    ColorScheme lineColorScheme;
    ArrayList<Color> lineColors;
    int lineWidth;
    // Fill colors: space between polygons, background, and inner space when nesting is finite.
    ColorScheme fillColorScheme;
    ArrayList<Color> fillColors;
    Color backgroundColor;
    boolean innerFill;
    Color innerFillColor;
    // The base polygon for drawing the pattern. Inside the drawing scheme the base polygon
    // is represented with arbitraty floating point units and used for figuring out the angles.
    // Only when it is drawn it gets scaled to whatever the canvas size is and converted to integer units.
    PolygonD basePolygon;
    
    /* Default values for contructor. */
    protected static class DefaultValues {
        static final int NUM_SIDES = 3;
        static final int ROTATION = 0;
        static final DisplacementType DISPLACEMENT_TYPE = DisplacementType.RELATIVE;
        static final int DISPLACEMENT_PERCENT = 5;
        static final int DISPLACEMENT_PIXELS = 5;
        static final Direction DIRECTION = Direction.CLOCKWISE;
        static final int ITERATIONS = 100;
        static final boolean INFINITE = true;
        // default colors for line and fill - 10 color are defined for multiple
        // color modes. for polygons with more than 10 sides, colors will be repeated
        static final ColorScheme LINE_COLOR_SCHEME = ColorScheme.ONE_COLOR;
        static final Color[] LINE_COLORS = {Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA,
            Color.CYAN, Color.DARK_GRAY, Color.ORANGE, Color.PINK, Color.LIGHT_GRAY};
        static final Color LINE_COLOR_SINGLE = Color.BLACK;
        static final int LINE_WIDTH = 1;
        static final ColorScheme FILL_COLOR_SCHEME = ColorScheme.ONE_SIDE_ONE_COLOR;
        static final Color[] FILL_COLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.DARK_GRAY, Color.ORANGE, Color.PINK, Color.LIGHT_GRAY};
        static final Color FILL_COLOR_SINGLE = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.WHITE;
        static final boolean INNER_FILL = false;
        static final Color INNER_FILL_COLOR = Color.WHITE;       
    }
    
    /* Limits on some drawing parameters, to be used when constructing the control panel. */
    protected static class Limits {
        static final int MAX_NUM_SIDES = 20;
        static final int MIN_ROTATION = -180;
        static final int MAX_ROTATION = 180;
        // how much (in percent) the pattern is stretched with every click
        static final int STRETCH_PERCENT = 5;
        static final double MIN_ANGLE = 10;
        static final double MAX_ANGLE = 180 - MIN_ANGLE;
        // limit relative diplacement to 50%, because 51% = 49% in the other direction
        static final int MAX_DISPLACEMENT_PERCENT = 50;
        // fixed displacement essentially unlimited,
        // but if displacemnt exceeds polygon size, drawing will stop
        static final int MAX_DISPLACEMENT_PIXELS = 999;
        // no need to limit iterations, will likely reach the center before hitting this limit
        static final int MAX_ITERATIONS = 9999;
        static final int MAX_LINE_WIDTH = 20;

    }
    
    public DrawingScheme() {
        newDrawingScheme();
    }
    
    public void newDrawingScheme() {
        /* assign default values to class variables. */
        numSides = DefaultValues.NUM_SIDES;
        rotation = DefaultValues.ROTATION;
        createBasePolygon();
        // Angle set for a regular polygon, calculated from the base polygon.
        getAngles();
        displacementType = DefaultValues.DISPLACEMENT_TYPE;
        if (displacementType == DisplacementType.RELATIVE) {
            displacement = DefaultValues.DISPLACEMENT_PERCENT;
        } else if (displacementType == DisplacementType.FIXED) {
            displacement = DefaultValues.DISPLACEMENT_PIXELS;
        }
        direction = DefaultValues.DIRECTION;
        infinite = DefaultValues.INFINITE;
        iterations = DefaultValues.ITERATIONS;
        // Initialize array colors for line and fill based on the color schemes
        lineColorScheme = DefaultValues.LINE_COLOR_SCHEME;
        initializeColors(PaletteType.LINE);
        lineWidth = DefaultValues.LINE_WIDTH;
        fillColorScheme = DefaultValues.FILL_COLOR_SCHEME;
        initializeColors(PaletteType.FILL);        
        backgroundColor = DefaultValues.BACKGROUND_COLOR;
        innerFill = DefaultValues.INNER_FILL;
        innerFillColor = DefaultValues.INNER_FILL_COLOR;
    } // DrawingScheme initialization

    /* Copy all the fields from another drawing scheme (e.g., loaded from a file) */
    public void copyDrawingScheme(DrawingScheme newDrawingScheme) {
        basePolygon = newDrawingScheme.basePolygon;
        numSides = newDrawingScheme.numSides;
        rotation = newDrawingScheme.rotation;
        angles = newDrawingScheme.angles;
        displacementType = newDrawingScheme.displacementType;
        displacement = newDrawingScheme.displacement;
        direction = newDrawingScheme.direction;
        infinite = newDrawingScheme.infinite;
        iterations = newDrawingScheme.iterations;
        lineColorScheme = newDrawingScheme.lineColorScheme;
        lineColors = newDrawingScheme.lineColors;
        lineWidth = newDrawingScheme.lineWidth;
        fillColorScheme = newDrawingScheme.fillColorScheme;
        fillColors = newDrawingScheme.fillColors;
        backgroundColor = newDrawingScheme.backgroundColor;
        innerFill = newDrawingScheme.innerFill;
        innerFillColor = newDrawingScheme.innerFillColor;
    }
    
    /* create the base polygon as a regular polygon with /numSides/ sides,
    rotated by /rotation/ degrees. */
    private void createBasePolygon() {
        basePolygon = Geometry.createRegularPolygon(numSides);
        rotateBasePolygon(rotation);
        getAngles();
    }
    
    /* rotate the base polygon */
    private void rotateBasePolygon(int rotationDegrees) {
        basePolygon = Geometry.rotatePolygon(basePolygon, rotationDegrees);
    }
    
    /* find the base polygon's set of angles based on vertex locations */
    private void getAngles() {
        angles = null;
        angles = new double[numSides];
        for (int a = 0; a < numSides; a++) {
            angles[a] = Geometry.getAngle(basePolygon.get((a - 1 + numSides) % numSides),
                basePolygon.get(a), basePolygon.get((a + 1) % numSides));
        }
    }
    
    /* check if a polygon conforms to the angle limits */
    private boolean isPolygonValid(PolygonD polygon) {
        double angle;
        double angleSum = 0;
        for (int a = 0; a < numSides; a++) {
            angle = Geometry.getAngle(polygon.get((a - 1 + numSides) % numSides),
                polygon.get(a), polygon.get((a + 1) % numSides));
            if ((angle < Limits.MIN_ANGLE) || (angle > Limits.MAX_ANGLE)) {
                return(false);
            }
            angleSum += angle;
        }
        if ((int)Math.round(angleSum) != 180 * (numSides - 2)) {
            return(false);
        }
        return(true);
    }

    /* Change in number of sides - Requires updating angles and colors. */
    protected void numSidesChange (int newNumSides) {
        numSides = newNumSides;
        rotation = 0;
        createBasePolygon();
        updateColorsNumSidesChange(PaletteType.LINE);
        updateColorsNumSidesChange(PaletteType.FILL);        
    }

    /* change in the rotation of the base polygon */
    protected void rotationChange(int newRotation) {
        int rotationDegrees = newRotation - rotation;
        rotation = newRotation;
        rotateBasePolygon(rotationDegrees);
    }

    /* stretch the base polygon horizontally or vertically */
    protected void stretch(boolean horizontalStretch) {
        PolygonD newBasePolygon;
        if (horizontalStretch) {
            newBasePolygon = Geometry.stretchPolygon(basePolygon, ((double)(100 + Limits.STRETCH_PERCENT)) / 100d);
        } else {
            newBasePolygon = Geometry.stretchPolygon(basePolygon, 100d / ((double)(100 + Limits.STRETCH_PERCENT)));
        }
        // Has to be checked for validity to make sure angle limits were not exceeded.
        if (isPolygonValid(newBasePolygon)) {
            basePolygon = newBasePolygon;
        }
        // refresh the base polygon angles after the stretch
        getAngles();
    }
    
    /* change one of the angles. This is done by adjusting the next angle and
    the length of the two sides around the next angle. */
    protected void angleChange (int angleIndex, double newAngle) {
        PolygonD newBasePolygon = Geometry.changePolygonAngle(basePolygon, angleIndex, newAngle);
        if (isPolygonValid(newBasePolygon)) {
            basePolygon = newBasePolygon;
        }
        getAngles();

    }
    
    /* Change in displacement mode or amount. */
    
    protected void relativeDisplacementSelected() {
        displacementType = DisplacementType.RELATIVE;
        displacement = DefaultValues.DISPLACEMENT_PERCENT;
    }
    
    protected void fixedDisplacementSelected() {
        displacementType = DisplacementType.FIXED;
        displacement = DefaultValues.DISPLACEMENT_PIXELS;
    }

    protected void displacementChange (int newDisplacement) {
        displacement = newDisplacement;
    }

    /* Change in nesting direction. */
    
    protected void clockwiseDirectionSelected() {
        direction = Direction.CLOCKWISE;
    }
    
    protected void counterclockwiseDirectionSelected() {
        direction = Direction.COUNTERCLOCKWISE;
    }

    /* Change in whether or not to draw until reaching the center point */
    protected void infiniteChange (boolean newInfinite) {
        infinite = newInfinite;
    }

    /* Change in number of iterations (when in finite mode). */
    protected void depthChange (int newIterations) {
        iterations = newIterations;
    }
    
    enum PaletteType {
        LINE,
        FILL
    };

    /* Create a set of line or fill colors based on the color scheme. */
    private void initializeColors(PaletteType paletteType) {
        if (paletteType == PaletteType.LINE) {
            lineColors = null;
            switch (lineColorScheme) {
                case ONE_COLOR:
                    lineColors = new ArrayList(1);
                    lineColors.add(DefaultValues.LINE_COLOR_SINGLE);                   
                    break;
                case ONE_SIDE_ONE_COLOR:
                    lineColors = new ArrayList(numSides);
                    for (int c = 0; c < numSides; c++)
                        lineColors.add(DefaultValues.LINE_COLORS[c % DefaultValues.LINE_COLORS.length]);
                    break;
                case ONE_POLYGON_ONE_COLOR:
                case CUSTOM:
                    lineColors = new ArrayList(2);
                    lineColors.add(DefaultValues.LINE_COLORS[0]);
                    lineColors.add(DefaultValues.LINE_COLORS[1]);
                    break;                
                case NONE:
                    lineColors = new ArrayList(1);
                    lineColors.add(DefaultValues.LINE_COLOR_SINGLE); 
           } // line switch
        } else if (paletteType == PaletteType.FILL) {
            fillColors = null;
            switch (fillColorScheme) {
                case ONE_COLOR:
                    fillColors = new ArrayList(1);
                    fillColors.add(DefaultValues.FILL_COLOR_SINGLE);                   
                    break;
                case ONE_SIDE_ONE_COLOR:
                    fillColors = new ArrayList(numSides);
                    for (int c = 0; c < numSides; c++)
                        fillColors.add(DefaultValues.FILL_COLORS[c % DefaultValues.FILL_COLORS.length]);
                    break;
                case ONE_POLYGON_ONE_COLOR:
                case CUSTOM:
                    fillColors = new ArrayList(2);
                    fillColors.add(DefaultValues.FILL_COLORS[0]);
                    fillColors.add(DefaultValues.FILL_COLORS[1]);
                    break;
                case NONE:
                    fillColors = new ArrayList(1);
                    fillColors.add(DefaultValues.FILL_COLOR_SINGLE);                   
            } // fill switch
        } // palatteType
    }
    
    /* Add or remove colors when the number of sides is changed.
    Only the "One Side, One Color" scheme requires an update in this case:
    Either add from default colors (with modulus), or remove colors. */
    private void updateColorsNumSidesChange(PaletteType paletteType) {
        int numSidesDiff;
        if (paletteType == PaletteType.LINE) {
            numSidesDiff = numSides - lineColors.size();
            if (lineColorScheme == ColorScheme.ONE_SIDE_ONE_COLOR) {
                if (numSidesDiff > 0) { // add colors
                    for (int c = lineColors.size(); c < numSides; c++) {
                        lineColors.add(DefaultValues.LINE_COLORS[c % DefaultValues.LINE_COLORS.length]);
                    }
                } else { // remove colors
                    for (int c = lineColors.size()-1; c >= numSides; c--) {
                        lineColors.remove(c);
                    }
                }
            }
        } else if (paletteType == PaletteType.FILL) {
            numSidesDiff = numSides - fillColors.size();
            if (fillColorScheme == ColorScheme.ONE_SIDE_ONE_COLOR) {
                if (numSidesDiff > 0) {
                    for (int c = fillColors.size(); c < numSides; c++) {
                        fillColors.add(DefaultValues.FILL_COLORS[c % DefaultValues.FILL_COLORS.length]);
                    }
                } else {
                    for (int c = fillColors.size()-1; c >= numSides; c--) {
                        fillColors.remove(c);
                    }
                }
            }
        }
    }

    /* Change in the line or fill color scheme.  */
    protected void colorSchemeChange(ColorScheme newColorScheme, PaletteType paletteType) {
        ColorScheme colorScheme = newColorScheme;
        ArrayList colors = new ArrayList();
        Color [] defaultColors = {};
        Color defaultColorSingle = null;
        // Direct variable to line or fill palette, depending on which scheme is being changed.
        if (paletteType == paletteType.LINE) {
            lineColorScheme = null;
            lineColorScheme = colorScheme;
            colors = lineColors;
            defaultColors = DefaultValues.LINE_COLORS;
            defaultColorSingle = DefaultValues.LINE_COLOR_SINGLE;
        } else if (paletteType == paletteType.FILL) {
            fillColorScheme = null;
            fillColorScheme = colorScheme;
            colors = fillColors;
            defaultColors = DefaultValues.FILL_COLORS;
            defaultColorSingle = DefaultValues.FILL_COLOR_SINGLE;
        }
        int numColors = colors.size();
        if (numColors == 0) // if switching from "None" mode
            initializeColors(paletteType);
        else
            switch (colorScheme) {
                case ONE_COLOR:
                    colors.clear();
                    colors.add(defaultColorSingle);
                    break;
                case ONE_SIDE_ONE_COLOR:
                    colors.clear();
                    for (int c = 0; c < numSides; c++)
                        colors.add(defaultColors[c % defaultColors.length]);
                    break;
                case ONE_POLYGON_ONE_COLOR:
                case CUSTOM:
                    // these schemes need at least two colors, otherwise they are
                    // equivalent to "One Color".
                    if (numColors == 1)
                        colors.add(defaultColors[1]);
                    break;
                case NONE:
                    break;
            }
    }

    /* Change to one of the line colors. */
    protected void lineColorChange(int index, Color newLineColor) {
        lineColors.set(index - 1, newLineColor);
    }
    
    /* Add a color to the line palette. */
    protected void addLineColor() {
        lineColors.add(DefaultValues.LINE_COLORS[(lineColors.size()) % DefaultValues.LINE_COLORS.length]);
    }

    /* Remove a color from the line palette */
    protected void removeLineColor() {
        lineColors.remove(lineColors.size() - 1);
    }

    /* Add a color to the fill palette. */
    protected void addFillColor() {
        fillColors.add(DefaultValues.FILL_COLORS[(fillColors.size()) % DefaultValues.FILL_COLORS.length]);        
    }

    /* Remove a color from the fill palette */
    protected void removeFillColor() {
        fillColors.remove(fillColors.size() - 1);
    }
        
    /* Change the width of the line used to draw the pattern outlines. */
    protected void lineWidthChange (int newLineWidth) {
        lineWidth = newLineWidth;
    }
    
    /* Change to one of the fill colors. */
    protected void fillColorChange(int index, Color newFillColor) {
        fillColors.set(index - 1, newFillColor);
    }

    /* Change to the background color (outside the pattern), and possible inside the innermost polygon). */
    protected void backgroundColorChange(Color newBackgroundColor) {
        backgroundColor = newBackgroundColor;
    }

    /* Change to whether or not to use inner fill for the innermost polygon. */
    protected void innerFillChange (boolean newInnerFill) {
        innerFill = newInnerFill;
    }

    /* Change to the inner fill color (when inner fill is enabled). */
    protected void innerFillColorChange(Color newInnerFillColor) {
        innerFillColor = newInnerFillColor;
    }
}  // DrawingScheme

