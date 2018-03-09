/*
 * The MIT License
 *
 * Copyright 2018 nick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package huetilities;

//Main menu for system tray

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class SystemTrayMenu {
    
    PHHueSDK sdk;
    ArrayList<HueScene> sceneList;
    ArrayList<MenuItem> sceneMenuItems;
    PopupMenu sceneMenu;
    MenuItem settings;
    MenuItem exit;
    boolean isSyncing = false;
    
    LightSyncController lc;
    List<PHLight> offLights;
    List<PHLight> primaryLights;
    List<PHLight> accentLights;
    boolean vibrant;
    
    boolean showLabels;
    boolean showRandom;
    MenuItem lightsLabel;
    MenuItem lightsLabel2;
    MenuItem randomLights;
    
    public SystemTrayMenu(PHHueSDK thisSDK){
        this.sdk = thisSDK;
        //Set tray image
        SystemTray tray = SystemTray.getSystemTray();
        Image trayImage = null;
        try {
            trayImage = ImageIO.read(getClass().getResource("HuetilitiesIconOutline.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        //Create tray menu
        PopupMenu menu = new PopupMenu();
        
        //Set tray icon and display tray menu
        TrayIcon trayIcon = new TrayIcon(trayImage, "Huetilities", menu);
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.err.println("Tray problem");
        }
        
        //Load general settings
        GeneralSettings.loadSettings();
        showLabels = GeneralSettings.showLabels;
        showRandom = GeneralSettings.showRandom;
        
        MenuItem lightsLabel1 = new MenuItem("Lights");
        lightsLabel1.setEnabled(false);
        menu.add(lightsLabel1);

        //Add submenu for each light
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();
        for(int i = 0; i < lights.size(); i++){
            PHLight light = lights.get(i);
            PHLightState lightState = light.getLastKnownLightState();

            PopupMenu lightMenu = new PopupMenu(light.getName());
            lightMenu.addActionListener((ActionEvent ev) -> {
                System.out.println(ev.getModifiers());
            });
            MenuItem toggleLight = new MenuItem();
            
            toggleLight.setLabel(light.getName());

            toggleLight.addActionListener((ActionEvent ev) -> {
                //If option-click
                if(ev.getModifiers() == 8){
                    if(lightState.isOn()){
                        lightState.setOn(false);
                        bridge.updateLightState(light, lightState);
                    } else {
                        lightState.setOn(true);
                        bridge.updateLightState(light, lightState);
                    }   
                } else {
                    LightSettings ls = new LightSettings(sdk, light);
                }
            });

            menu.add(toggleLight);
        }
        
        if(showLabels){
            lightsLabel = new MenuItem("Click to change color/brightness");
            lightsLabel.setEnabled(false);
            menu.add(lightsLabel);

            lightsLabel2 = new MenuItem("Option + Click to turn on/off");
            lightsLabel2.setEnabled(false);
            menu.add(lightsLabel2); 
        }
        
        
        menu.addSeparator();

        //Initilaize light controller and load light sync settings
        LightSyncSettings.loadSettings(sdk);
        vibrant = LightSyncSettings.getVibrant();
        offLights = LightSyncSettings.getOffLights();
        primaryLights = LightSyncSettings.getPrimaryLights();
        accentLights = LightSyncSettings.getAccentLights();
        lc = new LightSyncController(sdk);
        
        MenuItem startSync = new MenuItem("Start Light Sync");
        startSync.addActionListener((ActionEvent e) -> {
            if(isSyncing){
                isSyncing = false;
                startSync.setLabel("Start Light Sync");
                //Initilaize light controller and load light sync settings
                LightSyncSettings.loadSettings(sdk);
                vibrant = LightSyncSettings.getVibrant();
                offLights = LightSyncSettings.getOffLights();
                primaryLights = LightSyncSettings.getPrimaryLights();
                accentLights = LightSyncSettings.getAccentLights();
                lc.stop();
            } else {
                isSyncing = true;
                startSync.setLabel("Stop Light Sync");
                lc.start(vibrant, offLights, primaryLights, accentLights);
            }
        });
        menu.add(startSync);
        
        MenuItem syncSettings = new MenuItem("Light Sync Settings");
        syncSettings.addActionListener((ActionEvent e) -> {
            LightSyncSettingsFrame lsf = new LightSyncSettingsFrame(lc, sdk, isSyncing);
        });
        menu.add(syncSettings);
        menu.addSeparator();
        
        MenuItem saveScene = new MenuItem("Save Scene");
        saveScene.addActionListener((ActionEvent e) -> {
            //Get the scene name
            String sceneName = (String)JOptionPane.showInputDialog(null, "Enter a name for this scene", "Add Scene", JOptionPane.PLAIN_MESSAGE, null, null, null);
            while(sceneName.contains("/")){
                sceneName = (String)JOptionPane.showInputDialog(null, "Enter a name for this scene. Cannot contain /", "Add Scene", JOptionPane.PLAIN_MESSAGE, null, null, null);
            }
            if(sceneName != null){
               //Get the lights
                ArrayList<LightState> lightList = new ArrayList<LightState>();
                for(int i = 0; i < lights.size(); i++){
                    String name = lights.get(i).getName();
                    PHLightState lightState = lights.get(i).getLastKnownLightState();
                    int brightness = lightState.getBrightness();
                    float x = lightState.getX();
                    float y = lightState.getY();
                    LightState ls = new LightState(x, y, brightness, name);
                    lightList.add(ls);
                }
                HueScene scene = new HueScene(sceneName, lightList);
                LoadScenes.saveScene(scene);
                LoadScenes.loadScenes();
                sceneList = LoadScenes.scenes;
                
                //Add scene to menu
                MenuItem newScene = new MenuItem(scene.getName());
                newScene.addActionListener((ActionEvent ev) -> {
                    displayScene(scene);
                });
                sceneMenu.add(newScene);
            }
        });
        menu.add(saveScene);
        
        if(showRandom){
            randomLights = new MenuItem("Random Scene");
            randomLights.addActionListener((ActionEvent e) -> {
                RandomLights rl = new RandomLights(sdk);
            });
            menu.add(randomLights);
        }
        
        //Manages the scenes
        MenuItem manageScenes = new MenuItem("Manage Scenes");
        manageScenes.addActionListener((ActionEvent evv) -> {
            JFrame manageFrame = new JFrame("Manage Scenes");
            manageFrame.setSize(500, 300);
            JPanel managePanel = new JPanel(new GridBagLayout());
            managePanel.setBackground(Color.WHITE);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            
            manageFrame.setResizable(true);
            manageFrame.setLocationRelativeTo(null);
            
            JLabel mainTitle = new JLabel("Manage Scenes");
            mainTitle.setFont(new Font("Helvetica", Font.BOLD, 20));
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = FIRST_LINE_START;
            managePanel.add(mainTitle, c);
            
            JLabel titleLabel = new JLabel("Select the scene(s) you wish to delete then click OK");
            titleLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            c.gridx = 0;
            c.gridy = 1;
            managePanel.add(titleLabel, c);
            
            //Panel that loads each scene name and allows it to be deleted
            JPanel scenePanel = new JPanel(new WrapLayout());
            scenePanel.setBackground(Color.WHITE);
            ArrayList<HueScene> deletedScenes = new ArrayList<HueScene>();
            for(int i = 0; i < sceneList.size(); i++){
                String sceneName = sceneList.get(i).getName();
                HueScene thisScene = sceneList.get(i);
                JToggleButton lightButton = new JToggleButton(sceneName, false);
                lightButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
                Image lightImage = null;
                try {
                    lightImage = ImageIO.read(getClass().getResource("LightOutline.png"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lightImage = lightImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                ImageIcon lightIcon = new ImageIcon(lightImage);
                lightButton.setIcon(lightIcon);
                lightButton.addActionListener((ActionEvent e) -> {
                    if(lightButton.isSelected()){
                        deletedScenes.add(thisScene);
                    } else{
                        deletedScenes.remove(thisScene);
                    }
                });
                scenePanel.add(lightButton);
            }
            c.gridx = 0;
            c.gridy = 2;
            managePanel.add(scenePanel, c);
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);
            JButton okButton = new JButton("OK");
            okButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            okButton.addActionListener((ActionEvent e) -> {
                for(int i = 0; i < deletedScenes.size(); i++){
                    LoadScenes.deleteScene(deletedScenes.get(i));
                }
                LoadScenes.loadScenes();
                sceneList = LoadScenes.scenes;
                
                //Remove deleted scenes from scene menu
                for(int i = 0; i < sceneMenuItems.size(); i++){
                    for(int j = 0; j < deletedScenes.size(); j++){
                       if(sceneMenuItems.get(i).getName().equals(deletedScenes.get(j).getName())){
                            sceneMenu.remove(sceneMenuItems.get(i));
                            sceneMenuItems.remove(i);
                        } 
                    }
                }
                
                manageFrame.dispatchEvent(new WindowEvent(manageFrame, WindowEvent.WINDOW_CLOSING));
            });
            buttonPanel.add(okButton);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            cancelButton.addActionListener((ActionEvent e) -> {
                manageFrame.dispatchEvent(new WindowEvent(manageFrame, WindowEvent.WINDOW_CLOSING));
            });
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            c.gridx = 0;
            c.gridy = 3;
            managePanel.add(buttonPanel, c);
            
            manageFrame.add(managePanel);
            manageFrame.show();
        });
        menu.add(manageScenes);
        
        sceneMenu = new PopupMenu("Scenes");
        LoadScenes.loadScenes();
        sceneList = LoadScenes.scenes;
        sceneMenuItems = new ArrayList<MenuItem>();
        for(int i = 0; i < sceneList.size(); i++){
            MenuItem thisSceneMenu = new MenuItem(sceneList.get(i).getName());
            HueScene thisScene = sceneList.get(i);
            thisSceneMenu.addActionListener((ActionEvent ev) -> {
                displayScene(thisScene);
            });
            sceneMenu.add(thisSceneMenu);
            sceneMenuItems.add(thisSceneMenu);
        }
        menu.add(sceneMenu);
        
        menu.addSeparator();
         
        settings = new MenuItem("Settings");
        settings.addActionListener((ActionEvent e) -> {
            JFrame generalSettingsFrame = new JFrame("Huetilities Settings");
            generalSettingsFrame.setSize(500, 300);
            generalSettingsFrame.setResizable(true);
            generalSettingsFrame.setLocationRelativeTo(null);

            JPanel generalSettingsPanel = new JPanel(new GridBagLayout());
            generalSettingsPanel.setBackground(Color.WHITE);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;

            //Add header label
            JLabel headerLabel = new JLabel("Settings");
            headerLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = FIRST_LINE_START;
            generalSettingsPanel.add(headerLabel, c);

            JPanel blankPanel = new JPanel();
            blankPanel.setBackground(Color.WHITE);
            c.gridx = 0;
            c.gridy = 1;
            generalSettingsPanel.add(blankPanel, c);

            //Add vibrant setting
            JLabel optionLabel = new JLabel("Show Click/Option-Click Labels in Main Menu");
            optionLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = FIRST_LINE_START;
            generalSettingsPanel.add(optionLabel, c);

            JToggleButton showButton = new JToggleButton("Show");
            showButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            JToggleButton hideButton = new JToggleButton("Hide");
            hideButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            ButtonGroup showGroup = new ButtonGroup();
            showGroup.add(showButton);
            showGroup.add(hideButton);

            JPanel showButtonPanel = new JPanel();
            showButtonPanel.setBackground(Color.WHITE);
            showButtonPanel.add(showButton);
            showButtonPanel.add(hideButton);
            c.gridx = 0;
            c.gridy = 3;
            generalSettingsPanel.add(showButtonPanel, c);

            //Add random setting
            JLabel randomLabel = new JLabel("Show Random Lights Option in Main Menu");
            randomLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
            c.gridx = 0;
            c.gridy = 4;
            c.anchor = FIRST_LINE_START;
            generalSettingsPanel.add(randomLabel, c);

            JToggleButton randomButton = new JToggleButton("Show");
            randomButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            JToggleButton noRandomButton = new JToggleButton("Hide");
            noRandomButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            ButtonGroup randomGroup = new ButtonGroup();
            randomGroup.add(randomButton);
            randomGroup.add(noRandomButton);

            JPanel randomButtonPanel = new JPanel();
            randomButtonPanel.setBackground(Color.WHITE);
            randomButtonPanel.add(randomButton);
            randomButtonPanel.add(noRandomButton);
            c.gridx = 0;
            c.gridy = 5;
            generalSettingsPanel.add(randomButtonPanel, c);

            //Read in current settings
            GeneralSettings.loadSettings();
            boolean currentLabels = GeneralSettings.showLabels;
            boolean currentRandom = GeneralSettings.showRandom;
            if(currentLabels){
                showButton.setSelected(true);
            } else {
                hideButton.setSelected(true);
            }
            if(currentRandom){
                randomButton.setSelected(true);
            } else {
                noRandomButton.setSelected(true);
            }

            //Add blank/buffer space
            for(int i = 0; i < 4; i++){
                JPanel blankPanel3 = new JPanel();
                blankPanel3.setBackground(Color.WHITE);
                c.gridx = 0;
                c.gridy = 6 + i;
                generalSettingsPanel.add(blankPanel3, c);
            }

            //Add save, cancel, reset to defaults generalSettingsPanel
            JPanel savePanel = new JPanel();
            savePanel.setBackground(Color.WHITE);

            JButton saveButton = new JButton("Save");
            saveButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            savePanel.add(saveButton);
            saveButton.addActionListener((ActionEvent ev) -> {
                //Add listeners for remaining events
                showLabels = currentLabels;
                showRandom = currentRandom;

                if(showButton.isSelected()){
                    showLabels = true;
                } 

                if(hideButton.isSelected()){
                    showLabels = false;
                    menu.remove(lightsLabel);
                    menu.remove(lightsLabel2);
                }

                if(randomButton.isSelected()){
                    showRandom = true;
                }

                if(noRandomButton.isSelected()){
                    showRandom = false;
                    menu.remove(randomLights);
                }

                GeneralSettings.saveSettings(showLabels, showRandom);

                generalSettingsFrame.dispatchEvent(new WindowEvent(generalSettingsFrame, WindowEvent.WINDOW_CLOSING));
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            savePanel.add(cancelButton);
            cancelButton.addActionListener((ActionEvent ev) -> {
                generalSettingsFrame.dispatchEvent(new WindowEvent(generalSettingsFrame, WindowEvent.WINDOW_CLOSING));
            });

            JButton resetDefaultsButton = new JButton("Reset to Defaults");
            resetDefaultsButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
            savePanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent ev) -> {
                GeneralSettings.saveSettings(true, true);
                generalSettingsFrame.dispatchEvent(new WindowEvent(generalSettingsFrame, WindowEvent.WINDOW_CLOSING));
            });
            c.gridx = 0;
            c.gridy = 15;
            generalSettingsPanel.add(savePanel, c);

            generalSettingsFrame.add(generalSettingsPanel);
            generalSettingsFrame.show();
        });
        menu.add(settings);
        
        exit = new MenuItem("Exit");
        exit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        menu.add(exit);
    }
    
    public void displayScene(HueScene scene){
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();
        
        ArrayList<LightState> lightStates = scene.getLights();
        
        for(int i = 0; i < lights.size(); i++){
            for(int j = 0; j < lightStates.size(); j++){
                if(lightStates.get(j).getName().equals(lights.get(i).getName())){
                    float x = lightStates.get(j).getX();
                    float y = lightStates.get(j).getY();
                    int brightness = lightStates.get(j).getBrightness();
                    PHLightState lightState = new PHLightState();
                    lightState.setX(x);
                    lightState.setY(y);
                    lightState.setBrightness(brightness);
                    bridge.updateLightState(lights.get(i), lightState);
                }
            }
        }
    }

}
