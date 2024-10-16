// Ali Nick Maleki, MDo Lab, TU DELFT
// 25/09/2023

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;
import ij.measure.Calibration;
import ij.io.FileInfo;
import ij.plugin.ChannelSplitter;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.gui.PointRoi;
import ij.gui.ImageCanvas;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.Label;
import java.awt.TextField;
import javax.swing.BoxLayout;
import java.awt.Panel;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ij.plugin.frame.PlugInFrame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.Color;
import javax.swing.JComboBox;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.*;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import java.awt.Font;





public class NS_Force_Analyzer extends PlugInFrame implements PlugIn, ActionListener {
    private ImagePlus image;
    private JPanel controlPanel;
    private JPanel imageToolsPanel;
    private JPanel fittingPanel;
    private TextField segmentLengthField;
    private int segmentLength;
    private Button fitGaussianButton;
    private Button openImageButton;
    private Button nextButton;
    private Button splitChannelsButton;
    private Button cropButton;
    private Button refreshButton;
    private List<String> tiffFilePaths = new ArrayList<>();
    private TextField medianPositionField;
    private TextField pixelSizeField;
    private double defaultPixelSize = 0.107; // Default pixel size in um
    private Button calculateForceButton;
    private TextField forceResultField;
    private Button saveButton;
    private String imageDirectory;
    private JPanel analyzedFilesPanel; 
    private JComboBox<String> colorComboBox;
    JScrollPane FAscrollPane;
    List<String> tifFilesList;
    List<String> analyzed_tifFilesList;
    // Define maxButtonWidth and maxTextFieldWidth as private fields in the class
    private static final int maxButtonWidth = 150;
    private static final int maxTextFieldWidth = 100;
    private double[] X_coordinates; // Add this field to store the center positions
    private double[] centerPositions; // Add this field to store the center positions
    private TextField delta_x_Field;

    // Define an array of color names and corresponding Color objects
    private String[] colorNames = {"Magenta", "Cyan", "Red", "Green", "Blue", "Yellow",  "White", "Black" };
    private Color[] colors = { Color.MAGENTA, Color.CYAN,  Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,  Color.WHITE, Color.BLACK };





    public NS_Force_Analyzer() {
        super("NS force calculation Plugin");
    }

    // Main method to run the plugin as a standalone application
    public static void main(String[] args) {
        
        new ij.ImageJ();
        NS_Force_Analyzer plugin = new NS_Force_Analyzer();
        plugin.run(null);
    }

    public void run(String arg) {
        // Create the control panel and buttons
        createControlPanel();
        
    }


private void createControlPanel() {
    controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

    // Create the Image Tools panel
    imageToolsPanel = new JPanel();
    imageToolsPanel.setBorder(BorderFactory.createTitledBorder("Image tools"));
    openImageButton = new Button("Open Image");
    openImageButton.addActionListener(this);
    imageToolsPanel.add(openImageButton);

    splitChannelsButton = new Button("Split Channels");
    splitChannelsButton.addActionListener(this);
    imageToolsPanel.add(splitChannelsButton);

    cropButton = new Button("Crop");
    cropButton.addActionListener(this);
    imageToolsPanel.add(cropButton);

    nextButton = new Button("Next Kymo");
    nextButton.addActionListener(this);
    imageToolsPanel.add(nextButton);

    // Add a new button for refreshing everything
    refreshButton = new Button("Refresh program"); // Use the class-level refreshButton instance
    refreshButton.addActionListener(this); // Add the ActionListener to the class-level refreshButton
    imageToolsPanel.add(refreshButton);

    imageToolsPanel.setLayout(new BoxLayout(imageToolsPanel, BoxLayout.Y_AXIS));
    controlPanel.add(imageToolsPanel);

    // Create the Fitting Panel
    fittingPanel = new JPanel();
    fittingPanel.setBorder(BorderFactory.createTitledBorder("Fitting tools"));
    fittingPanel.setLayout(new BoxLayout(fittingPanel, BoxLayout.Y_AXIS));

    Label segmentLengthLabel = new Label("  Segment length (pixels):");
    segmentLengthField = new TextField("20", 5);
    JPanel segPanel = new JPanel();
    segPanel.setLayout(new BoxLayout(segPanel, BoxLayout.X_AXIS));
    segPanel.add(segmentLengthLabel);
    segPanel.add(segmentLengthField);
    fittingPanel.add(segPanel);

    Label pixelsizeLabel = new Label("  Pixel Size (µm):");
    pixelSizeField = new TextField(10);
    pixelSizeField.setEditable(true);
    JPanel pixPanel = new JPanel();
    pixPanel.setLayout(new BoxLayout(pixPanel, BoxLayout.X_AXIS));
    pixPanel.add(pixelsizeLabel);
    pixPanel.add(pixelSizeField);
    fittingPanel.add(pixPanel);

    JPanel FGPanel = new JPanel();
    fitGaussianButton = new Button("Fit Gaussian");
    fitGaussianButton.addActionListener(this);
    fitGaussianButton.setMaximumSize(new Dimension(maxButtonWidth, fitGaussianButton.getPreferredSize().height));


    Label MedianLabel = new Label("  Median Position:");
    medianPositionField = new TextField(10);
    medianPositionField.setEditable(true);
    JPanel MedPanel = new JPanel();
    MedPanel.setLayout(new BoxLayout(MedPanel, BoxLayout.X_AXIS));
    MedPanel.add(MedianLabel);
    MedPanel.add(medianPositionField);
    fittingPanel.add(MedPanel);

    Label delta_x_Label = new Label("  Δx (pixels):");
    delta_x_Field = new TextField(10);
    delta_x_Field.setEditable(true);
    JPanel delta_x_Panel = new JPanel();
    delta_x_Panel.setLayout(new BoxLayout(delta_x_Panel, BoxLayout.X_AXIS));
    delta_x_Panel.add(delta_x_Label);
    delta_x_Panel.add(delta_x_Field);
    fittingPanel.add(delta_x_Panel);

    calculateForceButton = new Button("Calculate Extension Force");
    calculateForceButton.addActionListener(this);

    forceResultField = new TextField(10);
    forceResultField.setEditable(false);
    Label pNLabel = new Label("(pN)");
    JPanel forcePanel = new JPanel();
    forcePanel.setLayout(new BoxLayout(forcePanel, BoxLayout.X_AXIS));
    forcePanel.add(calculateForceButton);
    forcePanel.add(forceResultField);
    forcePanel.add(pNLabel);

    // Create the color selection dropdown menu
    Label RoiColorLabel = new Label("  ROI color");
    colorComboBox = new JComboBox<>(colorNames);
    colorComboBox.addActionListener(this);
    fittingPanel.add(colorComboBox);
    JPanel roiColorPanel = new JPanel();
    roiColorPanel.setLayout(new BoxLayout(roiColorPanel, BoxLayout.X_AXIS));
    roiColorPanel.add(RoiColorLabel);
    roiColorPanel.add(colorComboBox);
    fittingPanel.add(roiColorPanel);

    // Create the Save Button
    saveButton = new Button("Save");
    saveButton.addActionListener(this);

    FGPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    FGPanel.add(fitGaussianButton);
    fittingPanel.add(FGPanel);
    fittingPanel.add(forcePanel);
    JPanel savePanel = new JPanel();
    savePanel.add(saveButton);
    savePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    fittingPanel.add(savePanel);

        // Create a label for displaying the formula using HTML formatting
    String formulaHTML = "<html><font color='red'>* Forces are calculated by this formula: F(x) = 0.182 e<sup>3.3x</sup></font></html>";
    JLabel formulaLabel = new JLabel(formulaHTML);
    formulaLabel.setFont(new Font("Serif", Font.PLAIN, 11));
    formulaLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    fittingPanel.add(formulaLabel);

    for (Component component : fittingPanel.getComponents()) {
        if (component instanceof JComponent) {
            ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    controlPanel.add(fittingPanel);

    add(controlPanel);
    pack();
    setVisible(true);
}
    // Method to create the analyzed files panel
private void createAnalyzedFilesPanel() {
             if (FAscrollPane != null) {
        controlPanel.remove(FAscrollPane);
        }
    analyzedFilesPanel = new JPanel();
    analyzedFilesPanel.setBorder(BorderFactory.createTitledBorder("Analyzed Files"));
    analyzedFilesPanel.setLayout(new BoxLayout(analyzedFilesPanel, BoxLayout.Y_AXIS));

    // Retrieve the list of TIF files in the tracedDirectory
    List<String> analyzedFilesList = getAnalyzedTifFiles();

    // Create a new label for each TIF file (only show the name) and add it to the panel
    for (String filePath : analyzedFilesList) {
        File file = new File(filePath);
        JLabel fileLabel = new JLabel(file.getName());
        analyzedFilesPanel.add(fileLabel);
    }

    // Add the analyzedFilesPanel to a JScrollPane
    FAscrollPane = new JScrollPane(analyzedFilesPanel);
    FAscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    controlPanel.add(FAscrollPane);
}
    // Method to get the list of TIF files in the tracedDirectory
private List<String> getAnalyzedTifFiles() {
        analyzed_tifFilesList = new ArrayList<>();
        File tracedDir = new File(imageDirectory + File.separator + "traced");
        if (tracedDir.exists() && tracedDir.isDirectory()) {
            File[] filesInTracedDir = tracedDir.listFiles();
            if (filesInTracedDir != null) {
                for (File file : filesInTracedDir) {
                    if (file.getName().toLowerCase().endsWith(".tif")) {
                       analyzed_tifFilesList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return analyzed_tifFilesList;
    }

private void openImage() {
        
    
         if (FAscrollPane != null) {
        controlPanel.remove(FAscrollPane);
        }
    closePreviousImages();
    closeRoiManager();
    Opener opener = new Opener();
    String path = IJ.getFilePath("Open an image to process");
      if (path != null) {
        image = opener.openImage(path);
        if (image == null) {
            IJ.showMessage("Failed to open the image.");
        } else {
            imageDirectory = new File(path).getParent();
            image.show(); // Show the image after opening
            image = WindowManager.getCurrentImage();
            File directory = new File(path).getParentFile();
            if (directory != null && directory.isDirectory()) {
                File[] filesInDirectory = directory.listFiles();
                if (filesInDirectory != null) {
                    for (File file : filesInDirectory) {
                        if (file.getName().toLowerCase().endsWith(".tif") && !file.getAbsolutePath().equals(path)) {
                            tiffFilePaths.add(file.getAbsolutePath());
                        }
                    }
                }
            }

            // Check if the opened file is already in the analyzed list
            String openedFileName = new File(path).getName();
            if (isInAnalyzedList(openedFileName)) {
                IJ.showMessage("The opened file is already in the analyzed list.");
            }
            
    double pixelSize = getImageScale();

    // If image scale is not available, try to read from metadata
    if (pixelSize <= 0) {
        pixelSize = getPixelSizeFromMetadata();
    }

    // Set the default pixel size if no scale or metadata available
    if (pixelSize <= 0) {
        pixelSize = defaultPixelSize;
    }

    // Display the pixel size in the Pixel Size Textbox
    pixelSizeField.setText(Double.toString(pixelSize));

        }
    }
}

private void openNextTiff() {
        // Close all previous images and ROI manager
    closePreviousImages();
    closeRoiManager();
    if (!tiffFilePaths.isEmpty()) {
        String nextFilePath = tiffFilePaths.remove(0);
        Opener opener = new Opener();
        ImagePlus nextImage = opener.openImage(nextFilePath);
        if (nextImage == null) {
            IJ.showMessage("Failed to open the next TIFF file: " + nextFilePath);
        } else {
            if (image != null) {
                image.close(); // Close the previous image
            }
            image = nextImage;
            image.show();

            // Check if the opened file is already in the analyzed list
            String openedFileName = new File(nextFilePath).getName();
            if (isInAnalyzedList(openedFileName)) {
                IJ.showMessage("The opened file is already in the analyzed list.");
            }
        }
    } else {
        IJ.showMessage("No more TIFF files to open in this directory.");
    }
}
// Helper method to check if the given file name is already in the analyzed list
private boolean isInAnalyzedList(String fileName) {
    analyzed_tifFilesList = getAnalyzedTifFiles();
    for (String analyzedFile : analyzed_tifFilesList) {
        File analyzedFileObj = new File(analyzedFile);
        String analyzedFileName = analyzedFileObj.getName();
        if (analyzedFileName.equals(fileName) || analyzedFileName.startsWith("C2-" + fileName)) {
            return true;
        }
    }
    return false;
}



private void refreshEverything() {
    // Clear all previous ROIs
    RoiManager roiManager = RoiManager.getInstance();
    if (roiManager != null) {
        roiManager.close();
    }

    // Clear the active image and close all other images
    


    closePreviousImages();
    if (FAscrollPane != null) {
        controlPanel.remove(FAscrollPane);
        }

    // Reset any other variables or states that need to be refreshed
    X_coordinates = null;
    centerPositions = null;
    tiffFilePaths.clear();
    controlPanel.remove(analyzedFilesPanel);
    pixelSizeField.setText(Double.toString(defaultPixelSize));
    medianPositionField.setText("");
    delta_x_Field.setText("");
    forceResultField.setText("");
    tifFilesList = null;
    analyzed_tifFilesList = null;
    controlPanel.revalidate();

}

private void closePreviousImages() {
    // Close all previous images except the active one
    ImagePlus activeImage = WindowManager.getCurrentImage();
    if (activeImage != null) {
        int[] windowList = WindowManager.getIDList();
        if (windowList != null) {
            for (int windowId : windowList) {
                ImagePlus image = WindowManager.getImage(windowId);
                if (image != null) {
                    image.close();
                }
            }
        }
    }
}

private void closeRoiManager() {
    RoiManager roiManager = RoiManager.getInstance();
    if (roiManager != null) {
        roiManager.close();
    }
}

@Override
public void actionPerformed(ActionEvent e) {
    
    if (e.getSource() == fitGaussianButton) {
        if (image == null) {
            IJ.showMessage("Please open an image first.");
        } else {
            // Get the segment length from the text field
            String segmentLengthStr = segmentLengthField.getText();
            try {
                segmentLength = Integer.parseInt(segmentLengthStr);
            } catch (NumberFormatException ex) {
                IJ.showMessage("Invalid segment length. Please enter a valid integer.");
                return;
            }

            if (segmentLength <= 0) {
                IJ.showMessage("Segment length must be greater than zero.");
                return;
            }

            processImage();
        }
    } else if (e.getSource() == openImageButton) {
        openImage();
        createAnalyzedFilesPanel();
        controlPanel.revalidate(); // Revalidate the panel to refresh the UI
    } else if (e.getSource() == nextButton) {
        openNextTiff();
    } else if (e.getSource() == splitChannelsButton) {
        splitChannels();
    } else if (e.getSource() == cropButton) {
        cropImage();
    } else if (e.getSource() == calculateForceButton) {
        calculateExtensionForce();
    } else if (e.getSource() == saveButton) {
        saveImageAndForceData();
        saveROICoordinates(); 
        createAnalyzedFilesPanel(); // Refresh the list of analyzed files
        FAscrollPane.revalidate(); // Revalidate the panel to refresh the UI
        controlPanel.revalidate(); // Revalidate the panel to refresh the UI
    } else if (e.getSource() == colorComboBox) {
        changeRoiColor();
    } else if (e.getSource() == refreshButton) {
        refreshEverything();
    }
}



    private void saveROICoordinates() {
    if (image == null) {
        IJ.showMessage("Please open an image first.");
        return;
    }

    // Use the stored image directory to save the ROI coordinates CSV file
        String roiCsvPath = imageDirectory + File.separator + "traced" + File.separator + image.getTitle() + "_ROI_Coordinates.csv";
        try {
            FileWriter csvWriter = new FileWriter(roiCsvPath);
            csvWriter.append("X,Y\n"); // Write the header line
            RoiManager roiManager = RoiManager.getInstance();
            for (int y = 0; y < image.getHeight(); y++) {
                double center = 0 ;
                Roi roi = roiManager.getRoi(y);
                if (roi != null) {
                    double xPosition = roi.getXBase(); // Get x position of the ROI
                    center = xPosition;
                } 
            

                // Write the X, Y coordinates, and MSD of the ROI to the CSV file
                csvWriter.append(center + "," + y + "\n");
            }
            csvWriter.flush();
            csvWriter.close();
            IJ.showMessage("ROI coordinates saved successfully.");
        } catch (IOException ex) {
            IJ.showMessage("Failed to save ROI coordinates: " + ex.getMessage());
        }
    }





private void changeRoiColor() {
    RoiManager roiManager = RoiManager.getInstance();
    if (roiManager == null) {
        IJ.showMessage("Please add ROIs using the 'Fit Gaussian' button.");
        return;
    }

    int selectedColorIndex = colorComboBox.getSelectedIndex();
    if (selectedColorIndex >= 0 && selectedColorIndex < colors.length) {
        Color selectedColor = colors[selectedColorIndex];

        int roiCount = roiManager.getCount();
        if (roiCount == 0) {
            IJ.showMessage("No ROIs to change color.");
            return;
        }

        // Change the color of all ROIs to the selected color
        for (int i = 0; i < roiCount; i++) {
            Roi roi = roiManager.getRoi(i);
            roi.setStrokeColor(selectedColor);
        }

        // Update the display to show the new ROI colors
        ImagePlus activeImage = WindowManager.getCurrentImage();
        activeImage.updateAndDraw();
    }
}

private void saveImageAndForceData() {
    if (image == null) {
        IJ.showMessage("Please open an image first.");
        return;
    }

    // Use the stored image directory to save the cropped image and CSV file
    // Get the original file name without extension
    String originalFileName = image.getTitle();
    int dotIndex = originalFileName.lastIndexOf(".");
    if (dotIndex > 0) {
        originalFileName = originalFileName.substring(0, dotIndex);
    }
    String tracedDirectory = imageDirectory + File.separator + "traced";
    File tracedDir = new File(tracedDirectory);
    if (!tracedDir.exists()) {
        tracedDir.mkdirs(); // Create the "traced" directory if it doesn't exist
    }

    String imageExtension = ".tif";
    String croppedFileName = originalFileName ;
    String croppedFilePath = tracedDirectory + File.separator + croppedFileName + imageExtension;

 
    
    // Save the image using IJ.saveAs()
    IJ.saveAs(image, "Tiff", croppedFilePath);
    // Append image name and extension force to Calculated_forces.csv
    String forcesCsvPath = tracedDirectory + File.separator + "Calculated_forces.csv";
    try {
        FileWriter csvWriter = new FileWriter(forcesCsvPath, true);
        csvWriter.append(originalFileName);
        csvWriter.append(",");
        csvWriter.append(forceResultField.getText());
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();
        IJ.showMessage("Saved");
    } catch (IOException ex) {
        IJ.showMessage("Failed to append data to Calculated_forces.csv: " + ex.getMessage());
    }
}

private void calculateExtensionForce() {
    RoiManager roiManager = RoiManager.getInstance();
    if (roiManager == null) {
        IJ.showMessage("Please add ROIs using the 'Fit Gaussian' button.");
        return;
    }

    int roiCount = roiManager.getCount();
    if (roiCount == 0) {
        IJ.showMessage("Please add ROIs using the 'Fit Gaussian' button.");
        return;
    }

    // Find the selected ROI, if any
    Roi selectedRoi = null;
    for (int i = 0; i < roiCount; i++) {
        if (roiManager.isSelected(i)) {
            selectedRoi = roiManager.getRoi(i);
            break; // Found the selected ROI, no need to search further
        }
    }

    if (selectedRoi == null) {
        IJ.showMessage("Please select an ROI to calculate the extension force.");
        return;
    }

    // Get the x-position of the selected ROI in pixels
    double selectedRoiX = selectedRoi.getXBase();

    // Get the median position in pixels
    double medianPositionPixels = Double.parseDouble(medianPositionField.getText());

    // Get the pixel size in microns from the pixelSizeField (textbox)
    double pixelSizeMicrons = Double.parseDouble(pixelSizeField.getText());

    double distanceToMedian = Math.abs(selectedRoiX - medianPositionPixels);
    // Calculate the distance to median in microns using pixel size
    double distanceToMedianMicrons = Math.abs(distanceToMedian * pixelSizeMicrons);

    // Calculate the force F using the given formula
    double force = 0.182 * Math.exp(3.3 * distanceToMedianMicrons);

    // Display the calculated force in the uneditable textbox
    delta_x_Field.setText(String.format("%.2f", distanceToMedian));
    forceResultField.setText(String.format("%.2f", force));
}

private void cropImage() {
    image = WindowManager.getCurrentImage();
    Roi roi = image.getRoi();
    if (roi != null) {
        String originalFileName = image.getTitle();
        ImageProcessor ip = image.getProcessor();
        ip.setRoi(roi);
        ImagePlus croppedImage = new ImagePlus(originalFileName + "_Cropped", ip.crop());
        croppedImage.show();
        // Maximize the cropped window
        
        image = WindowManager.getCurrentImage();// Update the reference to the active image
    } else {
        IJ.showMessage("Please make a selection to crop.");
    }
}

private void splitChannels() {
    if (image == null) {
        IJ.showMessage("Please open an image first.");
        return;
    }

    // Give the original image a unique name
    String originalName = image.getTitle();
    String uniqueName = originalName + "_original";
    image.setTitle(uniqueName);

    ImagePlus[] channels = ChannelSplitter.split(image);
    if (channels != null && channels.length > 0) {
        for (ImagePlus channel : channels) {
            channel.show();
        }
        image = WindowManager.getCurrentImage();// Update the reference to the active image

        // Close the original multichannel image by its unique name
        ImagePlus originalImage = WindowManager.getImage(uniqueName);
        if (originalImage != null) {
            originalImage.close();
        }
    } else {
        IJ.showMessage("Failed to split channels.");
    }
}

private void maximizeWindow(ImagePlus img) {
    ImageWindow win = img.getWindow();
    if (win != null) {
        WindowManager.setCurrentWindow(win); // Set the cropped window as the current window
        IJ.run("Maximize"); // Maximize the current window using ImageJ's "Maximize" command
    }
}

private void processImage() {
    X_coordinates = null;
    centerPositions = null;
    RoiManager roiManager = RoiManager.getInstance();
    if (roiManager == null) {
        roiManager = new RoiManager();
    }

    // Get the active image (the one in front of others)
    ImagePlus activeImage = WindowManager.getCurrentImage();
    if (activeImage == null) {
        IJ.showMessage("There is no active image.");
        return;
    }

    int width = activeImage.getWidth();
    int height = activeImage.getHeight();
    centerPositions = new double[height];


    for (int y = 0; y < height; y++) {
        // Calculate the average intensity and segment Roi as before
        int ref_x  = calculateAverageForRow(y, width, segmentLength);
        int startX = 1;
        int endX = width;

        // Fit a Gaussian function on the segment of the active image
        double[] xValues = new double[segmentLength];
        double[] yValues = new double[segmentLength];
        for (int i = 0; i < segmentLength; i++) {
            xValues[i] = i;
            yValues[i] = activeImage.getProcessor().getPixelValue(ref_x + i, y);
        }

        // Perform the Gaussian fit
        CurveFitter fitter = new CurveFitter(xValues, yValues);
        fitter.doFit(CurveFitter.GAUSSIAN);

        // Get the Gaussian fit parameters
        double[] params = fitter.getParams();
        double center = params[2]  ;// The center of the Gaussian
        if (center < 0 || center > segmentLength ) {
        center = segmentLength/2 ;
        }

        center = center + ref_x ;
        // Create an Roi at the center of maximum
        Roi maxCenterRoi = new Roi(center, y, 1, 1);
        roiManager.addRoi(maxCenterRoi);
        centerPositions[y] = center;
    }


        maximizeWindow(activeImage);
        roiManager.runCommand(activeImage,"Show All");
            // Loop through all the ROIs and set their color to magenta
    for (Roi roi : roiManager.getRoisAsArray()) {
        roi.setStrokeColor(Color.MAGENTA);
    }
        // Loop through all the ROIs and refill x-positions from ROI x position
 

    X_coordinates = Arrays.copyOf(centerPositions, centerPositions.length);
    Arrays.sort(centerPositions); // Sort the array first
    double medianCenter = centerPositions[height / 2]; // Get the middle value (or average of middle two values for even length)

    // Display the median in the Median Position Textbox
    medianPositionField.setText(Double.toString(medianCenter));

        // Find the most distal x-point relative to the median
    double distalX = X_coordinates[0]; // Initialize with the first x-coordinate
    int distalXRoiIndex = 0; // Initialize the index of the most distal x-point ROI
    for (int i = 0; i < X_coordinates.length; i++) {
        double center = X_coordinates[i];
        if (Math.abs(center - medianCenter) > Math.abs(distalX - medianCenter)) {
            distalX = center;
            distalXRoiIndex = i;
        }
    }

    // Get the list of ROIs from the ROI Manager
    Roi[] rois = roiManager.getRoisAsArray();

    // Ensure the index is within the range of ROIs
    if (distalXRoiIndex >= 0 && distalXRoiIndex < rois.length) {
        // Select the ROI corresponding to the most distal x-point
        roiManager.select(distalXRoiIndex);

    }

}

private double getImageScale() {
    if (image != null) {
        Calibration cal = image.getCalibration();

        // Check if calibration is available and scaled
        if (cal.scaled()) {
            return cal.pixelWidth; // Calibration is set, use it as the pixel size
        }

        // If calibration is not set, try to read from image metadata
        FileInfo fileInfo = image.getOriginalFileInfo();
        if (fileInfo != null && fileInfo.pixelWidth != 0.0) {
            return fileInfo.pixelWidth;
        }
    }

    return 0; // Return 0 if image scale is not available
}
    
private double getPixelSizeFromMetadata() {
    if (image != null) {
        FileInfo fileInfo = image.getOriginalFileInfo();
        if (fileInfo != null && fileInfo.pixelWidth != 0.0) {
            double pixelSize = fileInfo.pixelWidth;
            IJ.log("Found pixel size from metadata: " + pixelSize);
            return pixelSize;
        }
    }
    IJ.log("No pixel size found in metadata. Using default pixel size: " + defaultPixelSize);
    return defaultPixelSize; // Default pixel size if no calibration information is available
}


private int calculateAverageForRow(int row, int width, int segmentLength) {
        int startX = 1;
        int endX = width - segmentLength;

        
        double max = 0 ;
        int max_position = 0 ;
        for (int x = 1; x <= endX; x++) {
            double sum = 0;
            for (int ref=0; ref<segmentLength; ref++){
                sum += image.getProcessor().getPixelValue(x + ref, row);
            }
            if (sum > max){
                max = sum ;
                max_position = x ;
            }
        }
        return max_position;
    }
}
