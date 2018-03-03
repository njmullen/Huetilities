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
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JSlider;

public class SystemTrayMenu {
    
    PHHueSDK sdk;
    
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
        
        //Scene submenu
        PopupMenu scenes = new PopupMenu("Scenes");
        
        scenes.addSeparator();
        MenuItem editScenes = new MenuItem("Manage Scenes");
        editScenes.addActionListener((ActionEvent e) -> {
            //
        });
        scenes.add(editScenes);
        
        menu.add(scenes);
        
        MenuItem saveScene = new MenuItem("Save Scene");
        saveScene.addActionListener((ActionEvent e) -> {
            //
        });
        menu.add(saveScene);
                
        MenuItem randomLights = new MenuItem("Random Scene");
        randomLights.addActionListener((ActionEvent e) -> {
            RandomLights rl = new RandomLights(sdk);
        });
        menu.add(randomLights);
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
        
        MenuItem settings = new MenuItem("Settings");
        settings.addActionListener((ActionEvent e) -> {
            //
        });
        menu.add(settings);
        
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        menu.add(exit);
       
    }

}
