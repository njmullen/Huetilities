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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

public class SystemTrayMenu {
    
    PHHueSDK sdk;
    ArrayList<HueScene> sceneList;
    ArrayList<MenuItem> sceneMenuItems;
    PopupMenu sceneMenu;
    MenuItem settings;
    MenuItem exit;
    
    public SystemTrayMenu(PHHueSDK thisSDK){
        this.sdk = thisSDK;
        //Set tray image
        SystemTray tray = SystemTray.getSystemTray();
        Image trayImage = null;
        try {
            trayImage = ImageIO.read(getClass().getResource("HueIcon.png"));
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
        
        MenuItem lightsLabel = new MenuItem("Click each light to turn on/off");
        lightsLabel.setEnabled(false);
        menu.add(lightsLabel);
       
        //Add submenu for each light
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();
        for(int i = 0; i < lights.size(); i++){
            MenuItem lightLabel = new MenuItem(lights.get(i).getName());
            PHLight light = lights.get(i);
            PHLightState lightState = light.getLastKnownLightState();
            lightLabel.addActionListener((ActionEvent e) -> {
                if(lightState.isOn()){
                    lightState.setOn(false);
                    bridge.updateLightState(light, lightState);
                } else {
                    lightState.setOn(true);
                    bridge.updateLightState(light, lightState);
                }
            });
            menu.add(lightLabel);
        }
        menu.addSeparator();
        
        MenuItem changeLights = new MenuItem("Change Lights Color/Brightness");
        changeLights.addActionListener((ActionEvent e) -> {
            LightSettings ls = new LightSettings();
        });
        menu.add(changeLights);
        
        menu.addSeparator();
        
        MenuItem startSync = new MenuItem("Start Light Sync");
        startSync.addActionListener((ActionEvent e) -> {
            //
        });
        menu.add(startSync);
        
        MenuItem syncSettings = new MenuItem("Light Sync Settings");
        syncSettings.addActionListener((ActionEvent e) -> {
            //
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
                
        MenuItem randomLights = new MenuItem("Random Scene");
        randomLights.addActionListener((ActionEvent e) -> {
            RandomLights rl = new RandomLights(sdk);
        });
        menu.add(randomLights);
        
        //Manages the scenes
        MenuItem manageScenes = new MenuItem("Manage Scenes");
        manageScenes.addActionListener((ActionEvent evv) -> {
            JFrame manageFrame = new JFrame("Manage Scenes");
            JPanel managePanel = new JPanel();
            manageFrame.setSize(500, 500);
            managePanel.setBackground(Color.WHITE);
            
            manageFrame.setResizable(true);
            manageFrame.setLocationRelativeTo(null);
            
            JLabel titleLabel = new JLabel("Select the scene(s) you wish to delete then click OK");
            managePanel.add(titleLabel);
            
            //Panel that loads each scene name and allows it to be deleted
            JPanel scenePanel = new JPanel(new WrapLayout());
            scenePanel.setSize(500, 500);
            scenePanel.setBackground(Color.WHITE);
            ArrayList<HueScene> deletedScenes = new ArrayList<HueScene>();
            for(int i = 0; i < sceneList.size(); i++){
                String sceneName = sceneList.get(i).getName();
                HueScene thisScene = sceneList.get(i);
                JToggleButton lightButton = new JToggleButton(sceneName, false);
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
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);
            JButton okButton = new JButton("OK");
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
            cancelButton.addActionListener((ActionEvent e) -> {
                manageFrame.dispatchEvent(new WindowEvent(manageFrame, WindowEvent.WINDOW_CLOSING));
            });
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            
            managePanel.add(scenePanel);
            managePanel.add(buttonPanel);
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
            //
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
