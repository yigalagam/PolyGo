package polygo;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * ControlPanel includes all the GUI components for generating patterns.
 */
public class ControlPanel extends JPanel {
    DrawingScheme drawingScheme;
    Pattern pattern;
    /* GUI components that require run-time access, so declared outside constructor.
    Containers of those components are also declared here, so components can be removed and re-added.
    Components are explained wherever they are initialized in the constructor. */
    JPanel mainPanel;
    JSpinner numSidesSpinner, rotationSpinner;
    JScrollPane anglesScrollPane;
    int anglesScrollBarValue;
    JPanel angleSpinnersPanel;
    JPanel aspectRatioLabelPanel;
    JLabel aspectRatioLabel;
    JPanel displacementPanel, displacementSpinnerPanel;
    JSpinner displacementSpinner;
    JSpinner depthSpinner;
    JLabel iterationsLabel;
    JComboBox lineColorSchemeComboBox, fillColorSchemeComboBox;
    JSpinner lineWidthSpinner;
    JScrollPane linePaletteScrollPane, fillPaletteScrollPane;
    JButton lineColorPlusButton, lineColorMinusButton, fillColorPlusButton, fillColorMinusButton;
    JPanel linePalettePanel, fillPalettePanel;
    JButton backgroundColorButton;
    JCheckBox innerFillCheckBox;
    JButton innerFillColorButton;
    // Preferred spinner width for all spinners in the GUI, will be measured in real time.
    int spinnerWidth;
    
    private static class Settings {
        /* Constants and real-time parameters for controlling the appearance of GUI components. */
        // margins for the subpanels
        static final int PANELS_SIDE_MARGIN = 5;
        // button height for several different controls, based on the measured size
        // of JButton objects using the Numbus look and feel.
        static final int BUTTON_HEIGHT = 27;
        // Spaceing between buttons and other GUI elements
        static final int BUTTON_SPACING = 4;
        // Size of the icon in the "about" dialog box.
        static final int ABOUT_ICON_WIDTH = 100;
        static final int ABOUT_ICON_HEIGHT = 100;
        // number of angle spinners per row in the angles scroll pane
        static final int ANGLE_SPINNERS_PER_ROW = 2;
        // number of color buttons per row in the line and fill palettes
        static final int PALETTE_COLORS_PER_ROW = 3;
    }
     
    /* The ControlPanel constructor takes in a drawing scheme, which describes the
    * pattern to draw, and a pattern object to control.
    * The appearance of some GUI components depends on information in the drawing scheme. */
    public ControlPanel(DrawingScheme drawingSchemeIn, Pattern patternIn) {
        super();
        drawingScheme = drawingSchemeIn;
        pattern = patternIn;
        initializeControlPanel();
    }
    
    public void initializeControlPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.X_AXIS));
        // Create a dummy spinner to get preferred width based on text size.
        JSpinner tempSpinner = new JSpinner(new SpinnerNumberModel(999, -999, 999, 1));
        // Measure the preferred spinner width to accommodate 4 digits.
        spinnerWidth = tempSpinner.getPreferredSize().width;
        
        /* File operation and help commands panel. */
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
         // Measure the height of a generic text label for adjustment of margins.
        JLabel tempLabel = new JLabel("X");
        int labelHeight = tempLabel.getPreferredSize().height;
        filePanel.setBorder(BorderFactory.createEmptyBorder(
            labelHeight, Settings.PANELS_SIDE_MARGIN, 0, Settings.PANELS_SIDE_MARGIN));
        JButton newButton = new JButton("New");    // new pattern
        newButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                newButtonClick();
            }});
        filePanel.add(newButton);
        addVerticalSpace(filePanel, Settings.BUTTON_SPACING);
        JButton openButton = new JButton("Open");  // load a pattern from file
        openButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                openButtonClick();
            }});
        filePanel.add(openButton);
        addVerticalSpace(filePanel, Settings.BUTTON_SPACING);
        JButton saveButton = new JButton("Save");   // save pattern to file
        saveButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                saveButtonClick();
            }});
        filePanel.add(saveButton);
        addVerticalSpace(filePanel, Settings.BUTTON_SPACING);
        JButton copyButton = new JButton("Copy");   // copy pattern to clipboard
        copyButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                copyButtonClick();
            }});
        filePanel.add(copyButton);
        addVerticalSpace(filePanel, Settings.BUTTON_SPACING);
        JButton helpButton = new JButton("Help");   // show help
        helpButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                helpButtonClick();
            }});
        filePanel.add(helpButton);
        addVerticalSpace(filePanel, Settings.BUTTON_SPACING);
        JButton aboutButton = new JButton("About");   // show "about" dialog box
        aboutButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                aboutButtonClick();
            }});
        filePanel.add(aboutButton);
        mainPanel.add(filePanel);
        // Make all the buttons the width of the "About" button (widest), so that all buttons
        // are the same width and all the text can fit inside the buttons.
        int buttonWidth = aboutButton.getPreferredSize().width;
        adjustSize(newButton, buttonWidth, Settings.BUTTON_HEIGHT);
        adjustSize(openButton, buttonWidth, Settings.BUTTON_HEIGHT);
        adjustSize(saveButton, buttonWidth, Settings.BUTTON_HEIGHT);
        adjustSize(copyButton, buttonWidth, Settings.BUTTON_HEIGHT);
        adjustSize(helpButton, buttonWidth, Settings.BUTTON_HEIGHT);
        adjustSize(aboutButton, buttonWidth, Settings.BUTTON_HEIGHT);
        
        /* Attributes of the base polygon used to generate the pattern. */
        JPanel basePolygonPanel = new JPanel();
        basePolygonPanel.setLayout(new  BoxLayout(basePolygonPanel, BoxLayout.Y_AXIS));
        basePolygonPanel.setBorder(BorderFactory.createCompoundBorder
            (new EmptyBorder(0, Settings.PANELS_SIDE_MARGIN, 0,
            Settings.PANELS_SIDE_MARGIN), new TitledBorder("Polygon")));
        // Number of sides in the base polygon.
        JPanel numSidesPanel = new JPanel();
        numSidesPanel.setLayout(new BoxLayout(numSidesPanel, BoxLayout.X_AXIS));
        numSidesPanel.add(Box.createHorizontalStrut(Settings.PANELS_SIDE_MARGIN));
        numSidesPanel.add(new JLabel("Sides:"));
        numSidesPanel.add(Box.createHorizontalGlue());
        numSidesSpinner = new JSpinner(new SpinnerNumberModel(
            drawingScheme.numSides, 3, DrawingScheme.Limits.MAX_NUM_SIDES, 1));
        numSidesSpinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent evt) {
                numSidesSpinnerChange();
            }}); 
        adjustSize(numSidesSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
        numSidesSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));
        numSidesPanel.add(numSidesSpinner);
        numSidesPanel.add(Box.createHorizontalStrut(Settings.PANELS_SIDE_MARGIN));
        basePolygonPanel.add(numSidesPanel);
        addVerticalSpace(basePolygonPanel, Settings.BUTTON_SPACING);        
        // Rotation of the initial (most exterior) polygon.
        JPanel rotationPanel = new JPanel();
        rotationPanel.setLayout(new BoxLayout(rotationPanel, BoxLayout.X_AXIS));
        rotationPanel.add(Box.createHorizontalStrut(Settings.PANELS_SIDE_MARGIN));
        rotationPanel.add(new JLabel("Rotation (deg): "));
        rotationPanel.add(Box.createHorizontalGlue());
        rotationSpinner = new JSpinner(new SpinnerNumberModel(
                0, DrawingScheme.Limits.MIN_ROTATION, DrawingScheme.Limits.MAX_ROTATION, 1));
        rotationSpinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent evt) {
                rotationSpinnerChange();
            }}); 
        adjustSize(rotationSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
        rotationSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));
        rotationPanel.add(rotationSpinner);
        rotationPanel.add(Box.createHorizontalStrut(Settings.PANELS_SIDE_MARGIN));
        basePolygonPanel.add(rotationPanel);
        addVerticalSpace(basePolygonPanel, 3 * Settings.BUTTON_SPACING);
        // Stretch panel with arrow buttons for horizontal and vertical scaling
        // (left horizontal button leads to vertical stretching).
        JPanel stretchPanel = new JPanel();
        stretchPanel.setLayout(new BoxLayout(stretchPanel, BoxLayout.Y_AXIS));
        stretchPanel.add(new JLabel("Stretch"));
        JPanel stretchButtonsPanel = new JPanel();
        stretchButtonsPanel.setLayout(new BoxLayout(stretchButtonsPanel, BoxLayout.X_AXIS));
        JButton leftStretchButton = new JButton("<");
        leftStretchButton.setName("left");
        leftStretchButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                stretchButtonClick((JButton)evt.getSource());
            }});
        adjustSize(leftStretchButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        stretchButtonsPanel.add(leftStretchButton);
        JButton rightStretchButton = new JButton(">");
        rightStretchButton.setName("right");
        rightStretchButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                stretchButtonClick((JButton)evt.getSource());
            }});
        adjustSize(rightStretchButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        stretchButtonsPanel.add(rightStretchButton);
        stretchPanel.add(stretchButtonsPanel);
        setComponentAlignment(stretchPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        basePolygonPanel.add(stretchPanel);
        addVerticalSpace(basePolygonPanel, 3 * Settings.BUTTON_SPACING);
        // Controls for the angles of the polygon.
        JPanel anglesPanel = new JPanel();
        anglesPanel.setLayout(new BoxLayout(anglesPanel, BoxLayout.Y_AXIS));
        anglesPanel.add(new JLabel("Angles"));
        createAngleSpinnersPanel(0);
        anglesScrollPane = new JScrollPane(angleSpinnersPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        anglesScrollPane.setPreferredSize(new Dimension(
            angleSpinnersPanel.getPreferredSize().width + (int)Math.round(1.5 * anglesScrollPane.getVerticalScrollBar().getPreferredSize().width),
            (int)Math.round(4 * Settings.BUTTON_HEIGHT)));
        anglesScrollBarValue = 0;
        JPanel anglesScrollPanePanel = new JPanel();
        anglesScrollPanePanel.setLayout(new BoxLayout(anglesScrollPanePanel, BoxLayout.Y_AXIS));
        anglesScrollPanePanel.add(anglesScrollPane);
        adjustSize(anglesScrollPanePanel, anglesScrollPane.getPreferredSize());
        anglesPanel.add(anglesScrollPanePanel);
        setComponentAlignment(anglesPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        basePolygonPanel.add(anglesPanel);
        setComponentAlignment(basePolygonPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        mainPanel.add(basePolygonPanel);
        
        /* Nesting parameters panel: How internal polygons are generated relative to the base polygon. */
        JPanel nestingPanel = new JPanel();
        nestingPanel.setLayout(new BoxLayout(nestingPanel, BoxLayout.Y_AXIS));
        nestingPanel.setBorder(BorderFactory.createEmptyBorder(0, Settings.PANELS_SIDE_MARGIN, 0, Settings.PANELS_SIDE_MARGIN));
        // Displacement: How much to shift each polygon relative to its parent,
        // and by a relative or fixed amount.
        displacementPanel = new JPanel();
        displacementPanel.setLayout(new BoxLayout(displacementPanel, BoxLayout.Y_AXIS));
        displacementPanel.setBorder(BorderFactory.createTitledBorder("Displacement"));
        ButtonGroup displacementTypeGroup = new ButtonGroup();    // make sure only "Relative" or "Fixed" can be selected
        // Relative displacement in percent of side length.
        JRadioButton displacementRelativeButton = new JRadioButton("Relative", true);
        displacementRelativeButton.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                if (((JRadioButton)evt.getSource()).isSelected())
                    relativeDisplacementSelected();
            }});
        displacementTypeGroup.add(displacementRelativeButton);
        displacementPanel.add(displacementRelativeButton);
       // Fixed displacement in pixels.
        JRadioButton displacementFixedButton = new JRadioButton("Fixed", false);
        displacementFixedButton.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                if (((JRadioButton)evt.getSource()).isSelected())
                    fixedDisplacementSelected();
           }});
        displacementTypeGroup.add(displacementFixedButton);
        displacementPanel.add(displacementFixedButton);
        displacementSpinnerPanel = createDisplacementSpinnerPanel();
        displacementPanel.add(displacementSpinnerPanel);
        setComponentAlignment(displacementPanel, AlignmentDirection.X, LEFT_ALIGNMENT);
        nestingPanel.add(displacementPanel);
        // Clockwise or counterclockwise nesting.
        JPanel directionPanel = new JPanel();       // clockwise vs. counterclockwise nesting
        directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.Y_AXIS));
        directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));
        ButtonGroup directionGroup = new ButtonGroup();    // make sure only "CW" or "CCW" can be selected
        JRadioButton directionClockwiseButton = new JRadioButton("Clockwise", true);
        directionClockwiseButton.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                JRadioButton source = (JRadioButton)evt.getSource();
                if (source.isSelected())
                    clockwiseDirectionSelected();
            }});        
        directionGroup.add(directionClockwiseButton);
        directionPanel.add(directionClockwiseButton);
        JRadioButton directionCounterclockwiseButton = new JRadioButton("Counterclockwise", false);
        directionCounterclockwiseButton.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                JRadioButton source = (JRadioButton)evt.getSource();
                if (source.isSelected())
                    counterclockwiseDirectionSelected();
            }});        
        directionGroup.add(directionCounterclockwiseButton);
        directionPanel.add(directionCounterclockwiseButton);
        setComponentAlignment(directionPanel, AlignmentDirection.X, LEFT_ALIGNMENT);       
        nestingPanel.add(directionPanel);
        // Next sub-panel determines how many recursive steps to draw (or an infinite black hole).
        // Depth of nesting (how many iterations).
        JPanel depthPanel = new JPanel();
        depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.Y_AXIS)); 
        depthPanel.setBorder(BorderFactory.createTitledBorder("Depth"));
        JPanel depthIterationsPanel = new JPanel();
        depthIterationsPanel.setLayout(new BoxLayout(depthIterationsPanel, BoxLayout.X_AXIS));
         // Number of iterations, if not infinite.
        depthSpinner = new JSpinner(new SpinnerNumberModel(
            drawingScheme.iterations, 1, DrawingScheme.Limits.MAX_ITERATIONS, 1));
        depthSpinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent evt) {
                depthSpinnerChange();
            }});
        adjustSize(depthSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
        depthSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));
        depthIterationsPanel.add(depthSpinner);
        depthIterationsPanel.add(Box.createHorizontalGlue());
        iterationsLabel = new JLabel(" iterations");
        depthIterationsPanel.add(iterationsLabel);
        depthPanel.add(depthIterationsPanel);
        // infinite decsent into the abyss
        JCheckBox infiniteCheckBox = new JCheckBox("Infinite", drawingScheme.infinite);
        infiniteCheckBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                infiniteCheckBoxChange(((JCheckBox)evt.getSource()).isSelected());
            }});                
        depthPanel.add(infiniteCheckBox);
        setComponentAlignment(depthPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        nestingPanel.add(depthPanel);
        setComponentAlignment(nestingPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        mainPanel.add(nestingPanel);
        
        /* Line attributes panel - color and width. */
        JPanel linePanel = new JPanel();
        linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.Y_AXIS));
        linePanel.setBorder(BorderFactory.createCompoundBorder
            (new EmptyBorder(0, Settings.PANELS_SIDE_MARGIN, 0, Settings.PANELS_SIDE_MARGIN), new TitledBorder("Line")));
        JPanel lineColorPanel = new JPanel();
        lineColorPanel.setLayout(new BoxLayout(lineColorPanel, BoxLayout.Y_AXIS));
        JPanel lineColorSchemeComboBoxPanel = new JPanel();
        lineColorSchemeComboBoxPanel.setLayout(new BoxLayout(lineColorSchemeComboBoxPanel, BoxLayout.Y_AXIS));
        lineColorSchemeComboBoxPanel.add(new JLabel("Color Scheme"));
        lineColorSchemeComboBox = new JComboBox(DrawingScheme.ColorScheme.values());   // coloring scheme for lines in the pattern
        lineColorSchemeComboBox.setSelectedItem(drawingScheme.lineColorScheme);
        lineColorSchemeComboBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorSchemeComboBoxChange(DrawingScheme.PaletteType.LINE);
            }});                
        adjustSize(lineColorSchemeComboBox, new Dimension(lineColorSchemeComboBox.getPreferredSize().width, Settings.BUTTON_HEIGHT));
        lineColorSchemeComboBoxPanel.add(lineColorSchemeComboBox);
        setComponentAlignment(lineColorSchemeComboBoxPanel, AlignmentDirection.X, LEFT_ALIGNMENT);
        lineColorPanel.add(lineColorSchemeComboBoxPanel);
        addVerticalSpace(lineColorPanel, Settings.BUTTON_SPACING);
        // Create an array of buttons for line colors, array length is based on the color scheme.
        linePalettePanel = createColorButtonPanel(DrawingScheme.PaletteType.LINE);
        // Place palette inside a scrollable pane.
        linePaletteScrollPane = new JScrollPane(linePalettePanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        linePaletteScrollPane.setPreferredSize(new Dimension(
            linePalettePanel.getPreferredSize().width + (int)Math.round(
            1.5 * linePaletteScrollPane.getVerticalScrollBar().getPreferredSize().width),
            (int)Math.round(3 * Settings.BUTTON_HEIGHT)));
        // The line palette scroll pane goes on another panel,
        // because otherwise it resizes the palette buttons for some reason
        JPanel linePaletteScrollPanePanel = new JPanel();
        linePaletteScrollPanePanel.add(linePaletteScrollPane);
        adjustSize(linePaletteScrollPanePanel, linePaletteScrollPane.getPreferredSize().width,
            linePaletteScrollPane.getPreferredSize().height + Settings.BUTTON_SPACING);
        lineColorPanel.add(linePaletteScrollPanePanel);
        addVerticalSpace(lineColorPanel, Settings.BUTTON_SPACING);
        JPanel linePalettePlusMinusPanel = new JPanel();
        linePalettePlusMinusPanel.setLayout((new BoxLayout(linePalettePlusMinusPanel, BoxLayout.X_AXIS)));
        lineColorPlusButton = new JButton("+");
        lineColorPlusButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorPlusButtonClick(DrawingScheme.PaletteType.LINE);
            }});
        adjustSize(lineColorPlusButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        linePalettePlusMinusPanel.add(lineColorPlusButton);
        lineColorMinusButton = new JButton("-");
        lineColorMinusButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorMinusButtonClick(DrawingScheme.PaletteType.LINE);
            }});
        adjustSize(lineColorMinusButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        linePalettePlusMinusPanel.add(lineColorMinusButton);
        updatePlusMinusButtons(DrawingScheme.PaletteType.LINE);
        lineColorPanel.add(linePalettePlusMinusPanel);
        setComponentAlignment(lineColorPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        linePanel.add(lineColorPanel);
        addVerticalSpace(linePanel, 3 * Settings.BUTTON_SPACING);
        // Line width.
        JPanel lineWidthPanel = new JPanel();
        lineWidthPanel.setLayout(new BoxLayout(lineWidthPanel, BoxLayout.X_AXIS));
        lineWidthPanel.add(new JLabel("Width: "));
        lineWidthSpinner = new JSpinner(new SpinnerNumberModel(drawingScheme.lineWidth, 1, DrawingScheme.Limits.MAX_LINE_WIDTH, 1));
        lineWidthSpinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent evt) {
                lineWidthSpinnerChange();
            }});
        adjustSize(lineWidthSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
        lineWidthSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));
        lineWidthSpinner.setEnabled((drawingScheme.lineColorScheme != DrawingScheme.ColorScheme.NONE));
        lineWidthPanel.add(lineWidthSpinner);
        lineWidthPanel.add(new JLabel(" pixels"));
        linePanel.add(lineWidthPanel);
        setComponentAlignment(linePanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        mainPanel.add(linePanel);
        
         /* Fill color panel - space between polygons, inner space, and image background. */
        JPanel fillPanel = new JPanel();
        fillPanel.setLayout(new BoxLayout(fillPanel, BoxLayout.Y_AXIS));
        fillPanel.setBorder(BorderFactory.createCompoundBorder
            (new EmptyBorder(0, Settings.PANELS_SIDE_MARGIN, 0, Settings.PANELS_SIDE_MARGIN), new TitledBorder("Fill")));       
        JPanel fillColorPanel = new JPanel();
        fillColorPanel.setLayout(new BoxLayout(fillColorPanel, BoxLayout.Y_AXIS));
        JPanel fillColorSchemeComboBoxPanel = new JPanel();
        fillColorSchemeComboBoxPanel.setLayout(new BoxLayout(fillColorSchemeComboBoxPanel, BoxLayout.Y_AXIS));
        fillColorSchemeComboBoxPanel.add(new JLabel("Color Scheme"));
        fillColorSchemeComboBox = new JComboBox(DrawingScheme.ColorScheme.values());   // coloring scheme for fill between polygons in the pattern
        fillColorSchemeComboBox.setSelectedItem(drawingScheme.fillColorScheme);
        fillColorSchemeComboBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                colorSchemeComboBoxChange(DrawingScheme.PaletteType.FILL);
            }});                
        adjustSize(fillColorSchemeComboBox, new Dimension(
            fillColorSchemeComboBox.getPreferredSize().width, Settings.BUTTON_HEIGHT));
        fillColorSchemeComboBoxPanel.add(fillColorSchemeComboBox);
        setComponentAlignment(fillColorSchemeComboBoxPanel, AlignmentDirection.X, LEFT_ALIGNMENT);
        fillColorPanel.add(fillColorSchemeComboBoxPanel);
        addVerticalSpace(fillColorPanel, Settings.BUTTON_SPACING);
        fillPalettePanel = createColorButtonPanel(DrawingScheme.PaletteType.FILL);
        fillPaletteScrollPane = new JScrollPane(fillPalettePanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fillPaletteScrollPane.setPreferredSize(new Dimension(
            fillPalettePanel.getPreferredSize().width + (int)Math.round(
            1.5 * fillPaletteScrollPane.getVerticalScrollBar().getPreferredSize().width),
            (int)Math.round(3 * Settings.BUTTON_HEIGHT)));
        JPanel fillPaletteScrollPanePanel = new JPanel();
        fillPaletteScrollPanePanel.add(fillPaletteScrollPane);
        adjustSize(fillPaletteScrollPanePanel, fillPaletteScrollPane.getPreferredSize().width,
            fillPaletteScrollPane.getPreferredSize().height + Settings.BUTTON_SPACING);
        fillColorPanel.add(fillPaletteScrollPanePanel);
        addVerticalSpace(fillColorPanel, Settings.BUTTON_SPACING);
        JPanel fillPalettePlusMinusPanel = new JPanel();
        fillPalettePlusMinusPanel.setLayout(new BoxLayout(fillPalettePlusMinusPanel, BoxLayout.X_AXIS));
        fillColorPlusButton = new JButton("+");
        fillColorPlusButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorPlusButtonClick(DrawingScheme.PaletteType.FILL);
            }});
        adjustSize(fillColorPlusButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        fillPalettePlusMinusPanel.add(fillColorPlusButton);
        fillColorMinusButton = new JButton("-");
        fillColorMinusButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorMinusButtonClick(DrawingScheme.PaletteType.FILL);
            }});
        adjustSize(fillColorMinusButton, 2 * Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        fillPalettePlusMinusPanel.add(fillColorMinusButton);
        updatePlusMinusButtons(DrawingScheme.PaletteType.FILL);
        fillColorPanel.add(fillPalettePlusMinusPanel);
        setComponentAlignment(fillColorPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        fillPanel.add(fillColorPanel);        
        addVerticalSpace(fillPanel, 3 * Settings.BUTTON_SPACING);
        // Background color (outside of the first polygon).
        JPanel backgroundColorPanel = new JPanel();
        backgroundColorPanel.setLayout(new BoxLayout(backgroundColorPanel, BoxLayout.X_AXIS));
        backgroundColorPanel.add(new JLabel("Background: "));
        backgroundColorButton = new JButton("");
        backgroundColorButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorButtonClick(ColorButtonType.BACKGROUND, (JButton)evt.getSource());
            }});
        adjustSize(backgroundColorButton, Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        backgroundColorButton.setPreferredSize(new Dimension(
            Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT));
        backgroundColorButton.setBackground(drawingScheme.backgroundColor);
        backgroundColorPanel.add(backgroundColorButton);
        fillPanel.add(backgroundColorPanel);
        addVerticalSpace(fillPanel, 3 * Settings.BUTTON_SPACING);
        // Inner fill (if there's a space inside the last polygon, when depth is finite).
        JPanel innerFillPanel = new JPanel();
        innerFillPanel.setLayout(new BoxLayout(innerFillPanel, BoxLayout.X_AXIS));
        innerFillCheckBox = new JCheckBox("Inner Fill: ", drawingScheme.innerFill);
        innerFillCheckBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent evt) {
                innerFillCheckBoxChange(((JCheckBox)evt.getSource()).isSelected());
            }});
        innerFillPanel.add(innerFillCheckBox);
        innerFillColorButton = new JButton("");
        innerFillColorButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                colorButtonClick(ColorButtonType.INNER_FILL, (JButton)evt.getSource());
            }});
        adjustSize(innerFillColorButton, Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
        innerFillColorButton.setPreferredSize(new Dimension(
            Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT));
        innerFillColorButton.setBackground(drawingScheme.innerFillColor);        
        innerFillPanel.add(innerFillColorButton);
        fillPanel.add(innerFillPanel);
        setComponentAlignment(fillPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        mainPanel.add(fillPanel);
        updateInfiniteCheckBox();
        setComponentAlignment(mainPanel, AlignmentDirection.Y, TOP_ALIGNMENT);
        add(mainPanel);
    } // constructor

    /* New pattern. */
    private void newButtonClick() {
        drawingScheme.newDrawingScheme();
        pattern.newPattern(drawingScheme);
        remove(mainPanel);
        mainPanel = null;
        initializeControlPanel();
        revalidate();
        repaint();
    }
    
    private void openButtonClick() {
        DrawingScheme newDrawingScheme = FileHandling.openFile();
        if (newDrawingScheme != null) {
            drawingScheme.copyDrawingScheme(newDrawingScheme);
            pattern.newPattern(drawingScheme);
            remove(mainPanel);
            mainPanel = null;
            initializeControlPanel();
            revalidate();
            repaint();
            updatePattern();
        }
    }
    
    private void saveButtonClick() {
        FileHandling.saveFile(pattern);
    }
          
    private void copyButtonClick() {
        pattern.copyToClipboard();
    }
    
    private void helpButtonClick() {
        File helpFile = new File(PolyGo.Info.programDir + File.separator + "PolyGo Help.pdf");
        try {
            Desktop.getDesktop().open(helpFile);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Problem opening help file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void aboutButtonClick() {
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
        addVerticalSpace(aboutPanel, 20);
        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent (Graphics g) {
                super.paintComponent(g);
                try {
                    BufferedImage icon = ImageIO.read(new File(
                        PolyGo.Info.programDir + File.separator + "PolyGo.png"));
                    Image iconImage = icon.getScaledInstance(
                        Settings.ABOUT_ICON_WIDTH, Settings.ABOUT_ICON_HEIGHT, BufferedImage.SCALE_SMOOTH);
                    g.drawImage(iconImage, 0, 0, null);
                } catch (Exception e) {
                }
            }
        };
        adjustSize(iconPanel, 100, 100);
        aboutPanel.add(iconPanel);
        addVerticalSpace(aboutPanel, 10);
        JLabel programText = new JLabel(PolyGo.Info.NAME);
        Font font = new Font(UIManager.getFont("Label.font").toString(), Font.BOLD,
            (int)Math.round(programText.getFont().getSize() * 1.5));
        programText.setFont(font);
        aboutPanel.add(programText);
        addVerticalSpace(aboutPanel, 5);
        aboutPanel.add(new JLabel("Version " + PolyGo.Info.VERSION));
        addVerticalSpace(aboutPanel, 20);
        aboutPanel.add(new JLabel("Copyright (c) 2014 Yigal Agam"));
        addVerticalSpace(aboutPanel, 5);
        aboutPanel.add(new JLabel("yigalagam@gmail.com"));
        addVerticalSpace(aboutPanel, 20);        
        setComponentAlignment(aboutPanel, AlignmentDirection.X, CENTER_ALIGNMENT);
        JDialog aboutDialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "About " + PolyGo.Info.NAME, true);
        aboutDialog.setResizable(false);
        aboutDialog.add(aboutPanel);
        aboutDialog.setSize(240, 270);
        aboutDialog.setLocationRelativeTo(null);
        aboutDialog.setVisible(true);
    }
    
    /* Number of sides change event handler. */
    private void numSidesSpinnerChange() {
        drawingScheme.numSidesChange(((Integer)numSidesSpinner.getValue()).intValue());
        rotationSpinner.setValue(DrawingScheme.DefaultValues.ROTATION);
        updateAngleSpinnerPanel(0);
        updateColorButtonPanel(DrawingScheme.PaletteType.LINE);
        updateColorButtonPanel(DrawingScheme.PaletteType.FILL);
        updatePattern();
    }
    
    /* Rotation change event handler. */
    private void rotationSpinnerChange() {
        drawingScheme.rotationChange(((Integer)rotationSpinner.getValue()).intValue());
        updatePattern();
    }
    
    /* Stretch button event handler. */
    private void stretchButtonClick(JButton source) {
        String buttonStr = source.getName();
        if (buttonStr.equals("left"))
            drawingScheme.stretch(false); // increase vertical
        else if (buttonStr.equals("right"))
            drawingScheme.stretch(true); // increase horizontal
        updateAngleSpinnerPanel(0);
        updatePattern();
    }
    
    /* Change in one of the polygon angles event handler. */
    private void angleSpinnerChange(int angleIndex, int angleValue) {
        drawingScheme.angleChange(angleIndex-1, (double)angleValue);
        // other angles will change as a result of this change, or the change may be rejected,
        // so need to update the angles panel
        updateAngleSpinnerPanel(angleIndex - 1);        
        updatePattern();
    }

    /* Refresh angle spinners when the number of sides changes */
    private void updateAngleSpinnerPanel(int firstAngleToUpdate) {
        anglesScrollPane.getViewport().remove(angleSpinnersPanel);
        createAngleSpinnersPanel(firstAngleToUpdate);
        anglesScrollPane.getViewport().add(angleSpinnersPanel);
        anglesScrollPane.getVerticalScrollBar().setValue(anglesScrollBarValue);
    }

    /* create a new set of angle controls */
    private void createAngleSpinnersPanel(int firstAngleToUpdate) {
        // create a set of spinners to control the angles of the polygon
        int numAngles = drawingScheme.numSides;
        JPanel[] anglePanels = new JPanel[numAngles];
        int[] angleInt = new int[numAngles];
        int angleSum = 0;
        JSpinner angleSpinner;
        for (int a = 0; a < numAngles; a++) {
            angleInt[a] = (int)Math.round(drawingScheme.angles[a]);
            angleSum += angleInt[a];
        }
        if (angleSum != (numAngles - 2) * 180)
            angleInt[(firstAngleToUpdate + 1) % numAngles] += (numAngles - 2) * 180 - angleSum;
        for (int a = 0; a < numAngles; a++) {
            anglePanels[a] = new JPanel();
            anglePanels[a].setLayout(new BoxLayout(anglePanels[a], BoxLayout.Y_AXIS));
            anglePanels[a].add(new JLabel(Integer.toString(a + 1)));
            angleSpinner = new JSpinner(new SpinnerNumberModel(angleInt[a],
                (int)DrawingScheme.Limits.MIN_ANGLE, (int)DrawingScheme.Limits.MAX_ANGLE, 1));
            angleSpinner.setName(Integer.toString(a + 1));
            ((JSpinner.DefaultEditor)angleSpinner.getEditor()).getTextField().setEditable(false);
            angleSpinner.addChangeListener(new ChangeListener() {
                @Override public void stateChanged(ChangeEvent evt) {
                    anglesScrollBarValue = anglesScrollPane.getVerticalScrollBar().getValue();
                    JSpinner source = (JSpinner)evt.getSource();
                    angleSpinnerChange(Integer.parseInt(source.getName()), ((Integer)source.getValue()).intValue());
                }});            
            adjustSize(angleSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
            angleSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));           
            anglePanels[a].add(angleSpinner);
            setComponentAlignment(anglePanels[a], AlignmentDirection.X, CENTER_ALIGNMENT);
        }
        angleSpinnersPanel = null;
        angleSpinnersPanel = populatePanel(anglePanels,
            Settings.ANGLE_SPINNERS_PER_ROW, Settings.BUTTON_SPACING);
    }
    
    /* Displacement mode change event handlers. */
    private void relativeDisplacementSelected() {
        drawingScheme.relativeDisplacementSelected();
        updateDisplacementPanel();
        updatePattern();
    }

    private void fixedDisplacementSelected () {
        drawingScheme.fixedDisplacementSelected();
        updateDisplacementPanel();
        updatePattern();
    }

    private void updateDisplacementPanel() {
        displacementPanel.remove(displacementSpinnerPanel);
        displacementSpinnerPanel = null;
        displacementSpinnerPanel = createDisplacementSpinnerPanel();
        displacementPanel.add(displacementSpinnerPanel);
    }
    
    /* Create an updated displacement panel. */
    private JPanel createDisplacementSpinnerPanel() {
        displacementSpinnerPanel = new JPanel();
        displacementSpinnerPanel.setLayout(new BoxLayout(displacementSpinnerPanel, BoxLayout.X_AXIS));
        displacementSpinner = null;
        displacementSpinner = new JSpinner();
        // relative displacement (in percent of side length)
        if (drawingScheme.displacementType == DrawingScheme.DisplacementType.RELATIVE) {
            displacementSpinner = new JSpinner(new SpinnerNumberModel(
                drawingScheme.displacement, 1, DrawingScheme.Limits.MAX_DISPLACEMENT_PERCENT, 1));
            displacementSpinnerPanel.add(displacementSpinner);
            displacementSpinnerPanel.add(new JLabel(" percent"));
        // absolute displacement (in pixels)
        } else if (drawingScheme.displacementType == DrawingScheme.DisplacementType.FIXED) {
            displacementSpinner = new JSpinner(new SpinnerNumberModel(
                drawingScheme.displacement, 1, DrawingScheme.Limits.MAX_DISPLACEMENT_PIXELS, 1));
            displacementSpinnerPanel.add(displacementSpinner);
            displacementSpinnerPanel.add(new JLabel(" pixels"));
        }
        displacementSpinner.setPreferredSize(new Dimension(spinnerWidth, Settings.BUTTON_HEIGHT));
        adjustSize(displacementSpinner, spinnerWidth, Settings.BUTTON_HEIGHT);
        displacementSpinnerPanel.add(Box.createHorizontalGlue());
        displacementSpinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent evt) {
                displacementSpinnerChange();
            }});
        displacementSpinnerPanel.setAlignmentX(LEFT_ALIGNMENT);
        return(displacementSpinnerPanel);
    }
    
    /* Displacement change event handlers. */
    private void displacementSpinnerChange() {
        drawingScheme.displacementChange(((Integer)displacementSpinner.getValue()).intValue());
        updatePattern();
    }
    
    /* Direction change event handlers. */
    private void clockwiseDirectionSelected() {
        drawingScheme.clockwiseDirectionSelected();
        updatePattern();
    }

    private void counterclockwiseDirectionSelected () {
        drawingScheme.counterclockwiseDirectionSelected();
        updatePattern();
    }

    /* Depth change event handlers. */
    private void depthSpinnerChange() {
        drawingScheme.depthChange(((Integer)depthSpinner.getValue()).intValue());
        updatePattern();
    }
    
    /* Infinite or finite depth event handler. */
    private void infiniteCheckBoxChange(boolean selected) {
        drawingScheme.infiniteChange(selected);
        updateInfiniteCheckBox();
        updatePattern();
    }

    /* Update GUI components that are applicable only when depth is finite. */
    private void updateInfiniteCheckBox() {
        depthSpinner.setEnabled(!drawingScheme.infinite);
        iterationsLabel.setEnabled(!drawingScheme.infinite);
        innerFillCheckBox.setEnabled(!drawingScheme.infinite);
        innerFillColorButton.setEnabled((!drawingScheme.infinite) && drawingScheme.innerFill);
    }
    
    /* Line width change event handler. */
    private void lineWidthSpinnerChange () {
        drawingScheme.lineWidthChange(((Integer)lineWidthSpinner.getValue()).intValue());
        updatePattern();
    }
    
    /* Color scheme change event handler for either line or fill colors. */
    private void colorSchemeComboBoxChange(DrawingScheme.PaletteType paletteType) {
        if (paletteType == DrawingScheme.PaletteType.LINE)
            drawingScheme.colorSchemeChange((DrawingScheme.ColorScheme)lineColorSchemeComboBox.getSelectedItem(), paletteType);
        else if (paletteType == DrawingScheme.PaletteType.FILL)
            drawingScheme.colorSchemeChange((DrawingScheme.ColorScheme)fillColorSchemeComboBox.getSelectedItem(), paletteType);
        updateColorButtonPanel(paletteType);
        updatePlusMinusButtons(paletteType);
        updatePattern();
    }
   
    /* Color button Types enum for handling events related to color changes */
    enum ColorButtonType {
        LINE { @Override public String toString() {
            return("Line Color"); } },
        FILL { @Override public String toString() {
            return("Fill Color"); } },
        BACKGROUND { @Override public String toString() {
            return("Background Color"); } },
        INNER_FILL { @Override public String toString() {
            return("Inner Fill Color"); } },
    }
    
    /* refresh line or fill color buttons. */
    private void updateColorButtonPanel(DrawingScheme.PaletteType paletteType) {
        JPanel colorButtonPanel = createColorButtonPanel(paletteType);
        if (paletteType == DrawingScheme.PaletteType.LINE) {
            linePaletteScrollPane.getViewport().remove(linePalettePanel);
            linePalettePanel = null;
            linePalettePanel = colorButtonPanel;
            linePaletteScrollPane.getViewport().add(linePalettePanel);
            lineWidthSpinner.setEnabled((drawingScheme.lineColorScheme != DrawingScheme.ColorScheme.NONE));
        } else if (paletteType == DrawingScheme.PaletteType.FILL) {
            fillPaletteScrollPane.getViewport().remove(fillPalettePanel);
            fillPalettePanel = null;
            fillPalettePanel = colorButtonPanel;
            fillPaletteScrollPane.getViewport().add(fillPalettePanel);
        }
    }
    
    /* Create a new panel of line or fill color buttons. */
    private JPanel createColorButtonPanel(DrawingScheme.PaletteType paletteType) {
        JPanel colorButtonPanel = new JPanel();
        ArrayList<Color> buttonColors = new ArrayList();        
        DrawingScheme.ColorScheme colorScheme = DrawingScheme.ColorScheme.NONE;
        if (paletteType == DrawingScheme.PaletteType.LINE) {
            buttonColors = drawingScheme.lineColors;
            colorScheme = drawingScheme.lineColorScheme;
        } else if (paletteType == DrawingScheme.PaletteType.FILL) {
            buttonColors = drawingScheme.fillColors;
            colorScheme = drawingScheme.fillColorScheme;
        }
        int numColors = buttonColors.size();
        JButton[] colorButtons = new JButton[numColors];
        for (int c = 0; c < numColors; c++) {
            colorButtons[c] = new JButton("");
            colorButtons[c].setName(Integer.toString(c+1));
            if (paletteType == DrawingScheme.PaletteType.LINE)
                colorButtons[c].addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent evt) {
                        colorButtonClick(ColorButtonType.LINE, (JButton)evt.getSource());
                    }});
            else if (paletteType == DrawingScheme.PaletteType.FILL)
                colorButtons[c].addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent evt) {
                        colorButtonClick(ColorButtonType.FILL, (JButton)evt.getSource());
                    }});
            adjustSize(colorButtons[c], Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT);
            colorButtons[c].setPreferredSize(new Dimension(Settings.BUTTON_HEIGHT, Settings.BUTTON_HEIGHT));
            colorButtons[c].setBackground(buttonColors.get(c));
            colorButtons[c].setEnabled(colorScheme != DrawingScheme.ColorScheme.NONE);
        }
       // place color buttons inside a panel (palette)
        colorButtonPanel = populatePanel(colorButtons, Settings.PALETTE_COLORS_PER_ROW, Settings.BUTTON_SPACING);
        return(colorButtonPanel);
    }

    /* Color button click event handler. */
    private void colorButtonClick(ColorButtonType colorButtonType, JButton source) {
        Color color = source.getBackground();
        String colorChooserString = colorButtonType.toString();
        if ((colorButtonType == ColorButtonType.LINE) || (colorButtonType == ColorButtonType.FILL))
            colorChooserString += (" #" + source.getName());
        color = JColorChooser.showDialog(null, colorChooserString, color);
        if (color != null) {
            switch (colorButtonType) {
                case LINE:
                    drawingScheme.lineColorChange(Integer.parseInt(source.getName()), color);
                    break;
                case FILL:
                    drawingScheme.fillColorChange(Integer.parseInt(source.getName()), color);
                    break;
                case BACKGROUND:
                    drawingScheme.backgroundColorChange(color);
                    break;
                case INNER_FILL:
                    drawingScheme.innerFillColorChange(color);
                    break;
            }
            source.setBackground(color);
            updatePattern();
        }
    }
    
    /* Plus or minus button clicks event handlers for line or fill colors. */
    private void colorPlusButtonClick(DrawingScheme.PaletteType paletteType) {
        if (paletteType == DrawingScheme.PaletteType.LINE)
            drawingScheme.addLineColor();
        else if  (paletteType == DrawingScheme.PaletteType.FILL)
            drawingScheme.addFillColor();
        updateColorButtonPanel(paletteType);
        updatePlusMinusButtons(paletteType);
        updatePattern();
    }
    private void colorMinusButtonClick(DrawingScheme.PaletteType paletteType) {
        if (paletteType == DrawingScheme.PaletteType.LINE)
            drawingScheme.removeLineColor();
        else if  (paletteType == DrawingScheme.PaletteType.FILL)
            drawingScheme.removeFillColor();
        updateColorButtonPanel(paletteType);
        updatePlusMinusButtons(paletteType);
        updatePattern();
    }
    /* Enable or disable the plus and minus color buttons
     based on the color scheme and the current number of colors */ 
    private void updatePlusMinusButtons(DrawingScheme.PaletteType paletteType) {
        if (paletteType == DrawingScheme.PaletteType.LINE) {
            boolean flexiblePalette = ((drawingScheme.lineColorScheme == DrawingScheme.ColorScheme.CUSTOM) ||
                (drawingScheme.lineColorScheme == DrawingScheme.ColorScheme.ONE_POLYGON_ONE_COLOR));
            lineColorPlusButton.setEnabled(flexiblePalette);
            lineColorMinusButton.setEnabled((flexiblePalette && (drawingScheme.lineColors.size()>2)));
        } else if (paletteType == DrawingScheme.PaletteType.FILL) {
            boolean flexiblePalette = ((drawingScheme.fillColorScheme == DrawingScheme.ColorScheme.CUSTOM) ||
                (drawingScheme.fillColorScheme == DrawingScheme.ColorScheme.ONE_POLYGON_ONE_COLOR));
            fillColorPlusButton.setEnabled(flexiblePalette);
            fillColorMinusButton.setEnabled((flexiblePalette && (drawingScheme.fillColors.size()>2)));
        }
    }

    /* Inner fill event handler. */
    private void innerFillCheckBoxChange(boolean selected) {
        drawingScheme.innerFillChange(selected);
        innerFillColorButton.setEnabled((!drawingScheme.infinite) && drawingScheme.innerFill);
        updatePattern();
    }

    /* Update the pattern, typically after some GUI action */
    private void updatePattern() {
        pattern.update();
    }

    /* Various methods controlling the appearance of GUI elements. */
    
    static enum AlignmentDirection {
        X,
        Y,
    }
    
     /* setComponentAlignment sets all the components in a parent component
     into the same X or Y alignment (typically useful for box layouts) */
    static void setComponentAlignment(JPanel parentPanel, AlignmentDirection mode, float alignment) {
        JComponent component;
        for (int c = 0; c < parentPanel.getComponentCount(); c++) {
            component = (JComponent)parentPanel.getComponent(c);
            if (mode == AlignmentDirection.X)
                component.setAlignmentX(alignment);
            else if (mode == AlignmentDirection.Y)
                component.setAlignmentY(alignment);                
        }
    }

     /* Set minimum and maximum size (useful with box layout, because it ignores preferred size). */ 
    static void adjustSize (Component component, Dimension size) {
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }
    
    static void adjustSize (Component component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
    }

    /* Add blank space between vertical components. */
    static void addVerticalSpace (Container container, int verticalPixels) {
        container.add(Box.createVerticalStrut(verticalPixels));
    }

    /* Populate a panel with an set of components contained in an array list,
    and arrange them in rows as specified. */
    static JPanel populatePanel(JComponent[] components, int componentsPerRow, int buttonSpace) {
        JPanel populatedPanel = new JPanel();
        populatedPanel.setLayout(new BoxLayout(populatedPanel, BoxLayout.Y_AXIS));
        JPanel rowPanel;
        int numComponents = components.length;
        int counter = 0;
        while (counter < numComponents) {
            rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.add(Box.createHorizontalStrut(buttonSpace));
            for (int c = 0; c < componentsPerRow; c++)
                if (counter < numComponents) {   // if reached end of array, don't populate an entire row
                    rowPanel.add(components[counter++]);
                    rowPanel.add(Box.createHorizontalStrut(buttonSpace));
                }
            // If not even filling one row (e.g., only one color), fill with empty space to make sure
            // the scroll pane looks similar in case colors are added later.
            while (counter < componentsPerRow) {
                rowPanel.add(Box.createHorizontalStrut(components[0].getPreferredSize().width + buttonSpace));
                counter++;
            }
            populatedPanel.add(rowPanel);
            addVerticalSpace(populatedPanel, Settings.BUTTON_SPACING);
        }
        addVerticalSpace(populatedPanel, Settings.BUTTON_SPACING);
        setComponentAlignment(populatedPanel, AlignmentDirection.X, LEFT_ALIGNMENT);
        return(populatedPanel);
    }    
} // ControlPanel