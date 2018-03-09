# Huetilities
Control your Phillips HUE lights right from the menu bar: turn lights on/off, change color and brightness, set and save scenes, and sync your lights with the color on your display

## Features

### LightSync
Sync your HUE lights with the color of your display. Perfect for showing photos, watching movies, and settings the ambience. LightSync grabs the color average of the pixels on your display and sets your lights to that color. Control which lights are apart of the show, and set primary and accent colors. 

<img src="https://github.com/njmullen/Huetilities/blob/master/img/LightSyncSettings.png" width="522" height="422">

### Control your Lights
Turn your lights on and off, set the brightness, and choose a color all with a few simple clicks. Huetilites features your lights settings right at the top of the menu, and they can be adjusted quickly.

### Manage Scenes
Save and manage your favorite scenes with just a few clicks. You can also let Huetilities do the work of finding scenes with the Random Scene funcitonality.

## Setup
Huetilties only needs to be setup once and then it launches and is ready to go with just a click. When you first run Huetilities, it will begin the setup procedure which is as simple as pressing the button on your HUE bridge to give Huetilities permission to control your lights.

## Download & Compatibility
Huetilities is compatiable with macOS 10.8 and higher

## Troubleshooting
There are two known issues with the current release (beta): when you delete a scene with the Manage Scenes tool, the scene is not deleted until the application is relaunched, and when you toggle either the Random Scenes or Option+Click labels on (after they were already off), they won't reappear until relaunch.

Huetilities stores several files in the User Library's Application Support folder `/Users/<user>/Library/Application Support/Huetilities` that are neccesary for using the application. 
* `Huetilities.properties` manages the connection between the bridge and the application. If you're having issues connecting, or wish to connect to a different bridge, delete this file and re-launch the application, which will initiate the pairing process again.
* `HuetilitiesSettings.ser` manages the general settings for the application in the main Settings menu
* `LightSyncSettings.ser` manages the settings for the LightSync functionality
* `/Scenes/` is where all of the saved scenes are stored. Scenes are not tied to a specific bridge or set of lights, but if there is a different number of lights then settings the scene will fail
