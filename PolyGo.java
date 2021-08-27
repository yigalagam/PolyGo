package polygo;
//import com.apple.eawt.Application;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class PolyGo {
    protected static class Info {
        static final String NAME = "PolyGo";
        static final String VERSION = "1.0";
        static String programDir;
    }
    
    private static class Settings {
        static final int PATTERN_WIDTH = 500;
        static final int PATTERN_HEIGHT = 500;
    }
    
    public PolyGo () {
        Info.programDir = System.getProperty("user.dir");
        DrawingScheme drawingScheme = new DrawingScheme();
        Pattern pattern = new Pattern(drawingScheme);
        ControlPanel controlPanel = new ControlPanel(drawingScheme, pattern);
        JFrame controlPanelFrame = new JFrame("PolyGo");
        controlPanelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlPanelFrame.setLocation(0, 0);
        controlPanelFrame.setResizable(false);
        controlPanelFrame.setVisible(true);
        controlPanelFrame.add(controlPanel);
        controlPanelFrame.pack();
        JFrame patternFrame = new JFrame();
        patternFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);      
        int controlPanelHeight = controlPanelFrame.getHeight();
        patternFrame.setMinimumSize(new Dimension(100, 100));
        patternFrame.add(pattern);
        patternFrame.setVisible(true);
        int patternVertPos = (int)Math.round(1 * (double)controlPanelHeight);
        int titleBarHeight = patternFrame.getHeight() - patternFrame.getContentPane().getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int patternWidth = Settings.PATTERN_WIDTH;
        int patternHeight = Settings.PATTERN_HEIGHT;
        if (screenSize.width < Settings.PATTERN_WIDTH) {
            patternWidth = screenSize.width;
        }
        if (screenSize.height < Settings.PATTERN_HEIGHT + patternVertPos) {
            // Subtracting title bar height to account for the OS X menu bar height
            // (I could not find a way to get it directly, so assuming they're the same).
            patternHeight = screenSize.height - patternVertPos - titleBarHeight;
        }
        patternFrame.setSize(patternWidth, patternHeight);
        patternFrame.setLocation(0, patternVertPos + titleBarHeight);
    }
    
    public static void main(String[] args) {
        try 
        { 
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); 
        } catch(Exception exc){ 
        }
        new PolyGo();
    }
}
