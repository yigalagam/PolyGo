package polygo;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.imageio.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileHandling {
    static final String POLYGO_FILE_EXTENSION = "polygo";
    
    /* Open a PolyGo file */
    public static DrawingScheme openFile() {
        DrawingScheme drawingScheme = null;
        // Create a file chooser.
        JFileChooser fileChooser = new JFileChooser(PolyGo.Info.programDir);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("PolyGo files", POLYGO_FILE_EXTENSION));
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileName = fileChooser.getSelectedFile();
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
                drawingScheme = (DrawingScheme)in.readObject();
            } catch (Exception e) {
                 JOptionPane.showMessageDialog(null, "Problem opening file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return(drawingScheme);
    }

    /* Save a PolyGo file as a drawing scheme or as an image. */
    public static void saveFile(Pattern pattern) {
        // Create a file chooser.
        JFileChooser fileChooser = new JFileChooser(PolyGo.Info.programDir);
        fileChooser.setAcceptAllFileFilterUsed(false);
        String[] formats = ImageIO.getReaderFileSuffixes();
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("PolyGo files", POLYGO_FILE_EXTENSION));
        for (String format : formats) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(format + " files", format);
            fileChooser.addChoosableFileFilter(filter);
        }
        int returnVal = fileChooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                javax.swing.filechooser.FileFilter fileFilter = fileChooser.getFileFilter();
                String fileExtension = ((FileNameExtensionFilter)(fileFilter)).getExtensions()[0];
                if (!file.getName().endsWith(fileExtension)) {
                    file = new File(file.getAbsolutePath() + "." + fileExtension);
                }
                if (file.exists()) {
                    int reply = JOptionPane.showConfirmDialog(null,
                        ("Overwrite " + file.getName() + "?"), "File Exists", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {
                        file.delete();
                        file.createNewFile();
                    }
                    else {
                        return;
                    }
                }
                if (fileExtension.equals(POLYGO_FILE_EXTENSION)) { // PolyGo file
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
                    out.writeObject(pattern.drawingScheme);
                } else { // image file
                    BufferedImage bufImage = new BufferedImage(pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_INT_RGB);  
                    pattern.paint(bufImage.createGraphics());
                    ImageIO.write(bufImage, fileExtension, file);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Problem saving file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

