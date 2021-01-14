# Android_IOT
Android controller with custom module

This repository contains my Bachelors's degree thesis project codes.
This project consisted of two parts:
1- A hardware to replace conventional switches
2- An android program to turn these switches on and off.

For the first part, we simulated the circuit on Proteus Design Suite before buying any parts. The program to run the ATMEGA8 was written using CodeVision and the HEX files were obtained from CodeVision. The files for CodeVision are located in the "Microcontroller files" folder. The HEX file was uploaded into Proteus Design Suite and the simulation was successful. The files for Proteus Design Suite are located in the "proteus" folder. The schematic of the circuit is shown below:

![alt text](https://github.com/javad-sheikh/Android_IOT/blob/main/Images/proteusfinal.jpg)

After that, we designed a PCB using Altium designer. The Altium designer project files are in the Altium folder. I made this PCB as small as possible to fit it in a conventional switch place. The final schematic used in Altium designer is shown below:

![alt text](https://github.com/javad-sheikh/Android_IOT/blob/main/Images/Schematic%20Prints.jpg)

This is the finished circuit after soldering the parts:

![alt text](https://github.com/javad-sheikh/Android_IOT/blob/main/Images/finished.jpg)

As seen by the image above the final circuit was very compact, and it fitted in the switch place.
The next step was to write an android program to control the circuit by Bluetooth. I wrote the program in Android Studio. The files to replicate the program is in the "Final_Project_App-master" folder.
