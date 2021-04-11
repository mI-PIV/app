# mI-PIV
Mobile Instructional Particle Image Velocimetry (mI-PIV) is an educational Android application
that teaches users about fluid mechanics through real-time experiments and curricular modules.

The goal of mI-PIV is to promote and cultivate interest, active engagement, and 21st century
skill development within STEM areas through the development, implementation, and dissemination of
a low cost, mobile learning tool based on particle image velocimetry.

#### Status
![Gradle Build](https://github.com/mI-PIV/app/workflows/Gradle%20Build/badge.svg)
![Auto Release APK](https://github.com/mI-PIV/app/workflows/Auto%20Release%20APK/badge.svg?branch=main&event=schedule)

### Investigators
Dr. Angela Minichiello, P.E., Assistant Professor, Dept. of Engineering Education, Utah State University

Dr. Vladimir Kulyukin, Associate Professor, Dept. of Computer Science, Utah State University

Dr. Tadd Truscott, Associate Professor, Dept. of Mechanical and Aerospace Engineering, Utah State University

### Graduate Research Assistants
Aditya Bhouraskar, Lori Caldwell, Jack Elliott, Sarbajit Mukherjee, Kristoffer Price

### Undergraduate Research Assistants
Kevin Roberts

### Publications
TODO need a list of publication links stemming from this project

### Funding
Office of Naval Research, United States Department of the Navy

### License
Does our funding constrict licensing?

## APK Installation
TODO sign our app?

A video walk-through is available to download [here](https://github.com/mI-PIV/app/raw/readme/resources/installGithubAPK.mp4).

The easiest method of installing the APK package is to use the installation target-phone's browser:

- Go to github.com/mI-PIV/app/releases
- Click the Assets drop-down menu from the top (most recent) release
- Click on the file that ends with *{filename}*.apk (example *mIPIV_0.03.apk*)
- "Do you want to keep *{filename}*.apk anyway?" Select OK
- When the APK is done downloading, click "Open"
- If you see "Your phone is not allowed to install unknown apps..."
  
    - Click "Settings"
    - Toggle "Allow from this source"
    - Go back

- If you are updating mI-PIV, you will see a prompt asking if you want to update

    - Click "Install"
    
- Blocked by Play Protect
    
    - Click "Install Anyway"

## User Guide
### Experiment Pipeline
#### Video
##### Recording a video
###### Spatial Calibration
When mI-PIV process the PIV, it will search for a triangle in the top right quadrant of both selected frames.
If the triangle is found, then mI-PIV can calculate the particle velocities in centimeters per second.
You can run PIV with frames that don't have the calibration triangle, but the particle velocities will be in pixels per second.

To add the calibration triangle to your PIV experiments:
1. Print out the [calibration triangle](/resources/triangleCalibration.png) found on a 8 x 11 inch paper.
2. Cut out the triangle from the paper, leaving some whitespace around the edges.
3. (Optional) Laminate the triangle if you want to place inside the liquid.
4. Position the triangle in your experiments so that the full triangle will appear anywhere in the top right quadrant of your videos.

##### Selecting a video
##### Frame Extraction
mI-PIV saves all extracted video frames in the user phone's 'Pictures' directory.
To view the frames of a specific video, the path will look something like ...Pictures/miPIV_{username}/Extracted_Frames/Frames_{number}/.
#### Selecting frames
#### PIV parameters
#### Saved experiment data
mI-PIV saves all experiment data in the user phone's 'Pictures' directory. 
To get to a specific experiment, the path will look something like ...Pictures/miPIV_{username}/Experiments/Experiment_{number}/.
### Curricular Modules

## Documentation
### Project Structure
### Code Examples
### Shared Resources

## How to contribute

## How to cite mI-PIV
