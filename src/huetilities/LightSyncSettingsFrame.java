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

//Frame for displaying LightSync settings to the user

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class LightSyncSettingsFrame {
    
    JFrame frame;
    JPanel panel;
    PHHueSDK sdk;
    
    public LightSyncSettingsFrame(LightSyncController lc, PHHueSDK sdk, boolean isOn){
        //Stop lights while changing settings
        if(isOn){
            lc.stop();
        }
        this.sdk = sdk;
        
        frame = new JFrame("Light Sync Settings");
        //frame.setSize(500, 300);
	frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        
        //Add vibrant setting
        JLabel vibrantLabel = new JLabel("     Color Saturation     ");
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = FIRST_LINE_START;
        panel.add(vibrantLabel, c);

        JLabel vibrantDesc = new JLabel("     Colors can appear more realistically or enhanced to be more vibrant     ");
        c.gridx = 0;
        c.gridy = 4;
        panel.add(vibrantDesc, c);
        
        JToggleButton vibrantButton = new JToggleButton("Vibrant");
        JToggleButton realisticButton = new JToggleButton("Realistic");
        ButtonGroup vibrantGroup = new ButtonGroup();
        vibrantGroup.add(vibrantButton);
        vibrantGroup.add(realisticButton);

        JPanel vibrantButtonPanel = new JPanel();
        vibrantButtonPanel.setBackground(Color.WHITE);
        vibrantButtonPanel.add(vibrantButton);
        vibrantButtonPanel.add(realisticButton);
        c.gridx = 0;
        c.gridy = 5;
        panel.add(vibrantButtonPanel, c);
        
        //Add light selection
        JLabel lightLabel = new JLabel("     Select how you want your lights to display color     ");
        c.gridx = 0;
        c.gridy = 9;
        panel.add(lightLabel, c);

        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();
        
        List<PHLight> offLights = new ArrayList<PHLight>();
        List<PHLight> primaryLights = new ArrayList<PHLight>();
        List<PHLight> accentLights = new ArrayList<PHLight>();
        
        JPanel lightSelectionPanel = new JPanel(new GridBagLayout());
        lightSelectionPanel.setBackground(Color.WHITE);
        GridBagConstraints liC = new GridBagConstraints();
        liC.fill = GridBagConstraints.VERTICAL;
        
        //Read in current settings
        LightSyncSettings.loadSettings(sdk);
        boolean currentVibrant = LightSyncSettings.getVibrant();
        List<PHLight> currentOff = LightSyncSettings.offLights;
        List<PHLight> currentPrimary = LightSyncSettings.primaryLights;
        List<PHLight> currentAccent = LightSyncSettings.accentLights;

        if(currentVibrant){
            vibrantButton.setSelected(true);
        } else {
            realisticButton.setSelected(true);
        }
        
        for(int i = 0; i < lights.size(); i++){
            JPanel thisLightPanel = new JPanel();
            String lightName = lights.get(i).getName();
            PHLight light = lights.get(i);  
            JLabel thisLightLabel = new JLabel(lightName);
            thisLightPanel.add(thisLightLabel);
            thisLightPanel.setBackground(Color.WHITE);
            
            JToggleButton offButton = new JToggleButton("Off");
            JToggleButton primaryButton = new JToggleButton("Primary");
            JToggleButton accentButton = new JToggleButton("Accent");
            ButtonGroup lightSettingGroup = new ButtonGroup();
            lightSettingGroup.add(offButton);
            lightSettingGroup.add(primaryButton);
            lightSettingGroup.add(accentButton);
            
            thisLightPanel.add(offButton);
            thisLightPanel.add(accentButton);
            thisLightPanel.add(primaryButton);
            
            //Determine current setting
            if(currentOff.contains(light)){
                offButton.setSelected(true);
            } else if(currentAccent.contains(light)){
                accentButton.setSelected(true);
            } else if(currentPrimary.contains(light)){
                primaryButton.setSelected(true);
            }
            
            //Add action listeners
            offButton.addActionListener((ActionEvent e) -> {
               if(offButton.isSelected()){
                   offLights.add(light);
                   accentLights.remove(light);
                   primaryLights.remove(light);
               } 
            });
            
            primaryButton.addActionListener((ActionEvent e) -> {
               if(primaryButton.isSelected()){
                   offLights.remove(light);
                   accentLights.remove(light);
                   primaryLights.add(light);
               } 
            });
            
            accentButton.addActionListener((ActionEvent e) -> {
               if(accentButton.isSelected()){
                   offLights.remove(light);
                   accentLights.add(light);
                   primaryLights.remove(light);
               } 
            });
            
            liC.gridx = 0;
            liC.gridy = i;
            lightSelectionPanel.add(thisLightPanel, liC);
        } 
        c.gridx = 0;
        c.gridy = 10;
        panel.add(lightSelectionPanel, c);
        
        //Add blank/buffer space
        for(int i = 0; i < 4; i++){
            JPanel blankPanel = new JPanel();
            blankPanel.setBackground(Color.WHITE);
            c.gridx = 0;
            c.gridy = 10 + i;
            panel.add(blankPanel, c);
        }
        
        //Add save, cancel, reset to defaults panel
        JPanel savePanel = new JPanel();
        savePanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("MyriadProReg", Font.PLAIN, 14));
        savePanel.add(saveButton);
        saveButton.addActionListener((ActionEvent e) -> {
            //Add listeners for remaining events
            boolean startAutomatically = true;
            boolean vibrant = false;
            boolean trayIsDefault = true;

            if(vibrantButton.isSelected()){
                vibrant = true;
            }
            
            LightSyncSettings.saveSettings(vibrant, offLights, primaryLights, accentLights);
            if(isOn){
              lc.start(vibrant, offLights, primaryLights, accentLights);
            }

            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("MyriadProReg", Font.PLAIN, 14));
        savePanel.add(cancelButton);
        cancelButton.addActionListener((ActionEvent e) -> {
            LightSyncSettings.loadSettings(sdk);
            
            boolean vibrant = LightSyncSettings.getVibrant();
            List<PHLight> lightsOff = LightSyncSettings.getOffLights();
            List<PHLight> lightsPrim = LightSyncSettings.getPrimaryLights();
            List<PHLight> lightsAcc = LightSyncSettings.getAccentLights();
            
            if(isOn){
              lc.start(vibrant, lightsOff, lightsPrim, lightsAcc); 
            }

            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        
        JButton resetDefaultsButton = new JButton("Reset to Defaults");
        resetDefaultsButton.setFont(new Font("MyriadProReg", Font.PLAIN, 14));
        savePanel.add(resetDefaultsButton);
        resetDefaultsButton.addActionListener((ActionEvent e) -> {
            List<PHLight> allLights = cache.getAllLights();
            LightSyncSettings.saveSettings(false, null, allLights, null);
            if(isOn){
              lc.start(false, null, allLights, null);  
            }
            
            
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        c.gridx = 0;
        c.gridy = 15;
        panel.add(savePanel, c);
        
        frame.add(panel);
        frame.pack();
        frame.show();
        //Resume lights with new settings after user clicks "Save" or "Cancel"
        //lc.start();
    }
    
}
