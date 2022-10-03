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

### Funding
Office of Naval Research, United States Department of the Navy

## APK Installation
### Google Play Store
The app is now available on Google Play Store! You can install it [here](https://play.google.com/store/apps/details?id=com.onrpiv.uploadmedia).

### Manual Installation
A video walk-through is available to download [here](https://github.com/mI-PIV/app/raw/main/resources/installGithubAPK.mp4).

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
##### Selecting a video
##### Frame Extraction
mI-PIV saves all extracted video frames in the App's data directory.
To view the frames of a specific video, the path will look something like ...Android/data/com.onrpiv.uploadmedia/files/miPIV_{username}/Extracted_Frames/Frames_{number}/.
#### Selecting frames
#### PIV parameters
#### Saved experiment data
mI-PIV saves all experiment data and experiment parameters in the App's data directory. 
To get to a specific experiment, the path will look something like ...Android/data/com.onrpiv.uploadmedia/files/miPIV_{username}/Experiments/Experiment_{number}/.
### Curricular Modules
