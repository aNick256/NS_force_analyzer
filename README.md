# NS_Force_Analyzer ImageJ Plugin

## Overview

This ImageJ plugin implements automated analysis of nanostring extension in fluorescence microscopy images to calculate extension forces. It fits a Gaussian curve to intensity profiles along each line of kymograph length, determines the median position, and calculates the extension force based on the distal displacement relative to the median.

Key features:

- Opens TIFF images sequentially for analysis 
- Splits channels of multipanel images and select your channel of interest
- Crops images to a region of interest
- Fits Gaussian curves along segments perpendicular to the nanostring
- Determines median position from Gaussian curve centers  
- Calculates extension force based on distal displacement
- Allows changing ROI colors
- Saves analyzed images and calculated force data

## Usage

The plugin adds a "NS force calculation Plugin" window to ImageJ with the following controls:

- **Open Image**: Opens a TIFF image file for analysis
- **Split Channels**: Splits channels of a multipanel TIFF image
- **Crop**: Crops the image to the current ROI 
- **Next Kymo**: Opens the next TIFF file in the folder for analysis
- **Refresh program**: Clears all previous data and resets the plugin 
- **Segment length**: Length in pixels of segment to fit Gaussian  
- **Pixel Size**: Pixel size of the image in μm 
- **Fit Gaussian**: Fits Gaussian curves along image rows
- **Median Position**: Displays median of Gaussian centers   
- **Δx**: Displays max distal displacement of NS relative to median position
- **Calculate Extension Force**: Calculates force based on displacement  
- **ROI color**: Dropdown to select ROI color
- **Save**: Saves analyzed image and calculated forces to CSV

The process is:

1. Open an image
2. Set pixel size if needed
3. Set segment length (plugin scans for cumulative max signal along this segment and then do the gaussian fitting on each kymograph line in this region)
4. Click **Fit Gaussian** to fit curves and show ROIs
5. Note the median position
6. Click an ROI to select it (This should be the ROI of the position in which NS is at its most extended state)
7. Click **Calculate Extension Force** to show force
8. Change ROI color if desired
9. Click **Save** to save data. This will append the current measurement to the previous ones. The file is stored at kymograph_directory/traced/calculated_forces.csv
10. Click **Next Kymo** to analyze the next image



## References

- ImageJ documentation: https://imagej.nih.gov/ij/docs/
- Gaussian curve fitting: https://imagej.nih.gov/ij/developer/api/ij/measure/CurveFitter.html
- Swing Java GUI: https://docs.oracle.com/javase/tutorial/uiswing/

