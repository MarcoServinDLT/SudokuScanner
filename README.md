# Sudoku Scanner

Android Aplication to solve sudoku by scanning them with the camera.

Current state of the app:

## Process explanation 📚
---
### Image Preprocessing 📷
The purpose of this module is to obtain an image with the least possible noise, the best way to do it is to binarize the image to obtain the contour of all the elements in it, and thus obtain the characteristics of the image and be able to recognize elements in it.

The process to correctly obtain the binarized image can be summarized in three steps:
1. Convert the image from RGB or RGBA to grayscale.
2. Blur the image to identify the edges.
3. Compare the grayscale image with the blurred image to obtain the binarized image.

#### Step 1.- Get grayscale image
...
#### Step 2.- Blur image
...
#### Step 3.- Binarize image
...
### Image Recognition 
#### **🚧 Working on it 🚧**
### Tensorflow Model
...
## Technoligies 🤖
---
- Lenguajes
    - Kotlin
    - Python
- Frameworks
    - Tensorflow / Keras

## Objetives 🎯
---

[x] Preprocess the image capture.
[ ] Recognize zones of interest.
[ ] Extract zones of interest.
[x] Create an acurate ML model to recognize numbers.
[ ] Implement tensorflow lite model.

## Features
---

## Road map 🛣
---
[ ] Android app 
- [x] Create a module to preprocess the image.
- [x] Module to image recognition.
- [ ] Extract the numbers from the boxes trought recognition module.
- [ ] Implement the tensorflow model as a tensorflow lite model.
- [ ] Show the solution on capture.

[ ] Tensorflow model
- [x] Get the fonts to create the dataset.
- [x] Generate the dataset.
- [x] Create Convolutional model to recognize numbers.
- [ ] Export the model as a tensorflow lite model.


## Build development enviroment ⚙
---
For the application to work correctly it was necessary to use some extern dependencies and libraries. 

For the Android application was necessary use the CameraX API to capture by the camera and get the campture frames, to include the CameraX to the project were included the following lines in the file build.gradle:
```gradle
def camerax_version = "1.1.0-beta01"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-video:${camerax_version}"

implementation "androidx.camera:camera-view:${camerax_version}"
implementation "androidx.camera:camera-extensions:${camerax_version}"
```
Also in the python notebook was necessary install a library that wasn't in the execution enviroment (google colab notebooks), that library is **fontTools**,however, it is likely that you do not have all the necessary libraries, so all the libraries are included in the following command to have the execution environment ready:
```bash
pip install tensorflow pandas numpy matplotlib funcTools
```