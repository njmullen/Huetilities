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

//Frame for the general settings

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

public class GeneralSettingsFrame {
    
    JFrame frame;
    JPanel panel;
    PHHueSDK sdk;
    
    public GeneralSettingsFrame(){ 
        frame = new JFrame("Huetilities Settings");
        frame.setSize(500, 300);
	frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        
        //Add header label
        JLabel headerLabel = new JLabel("Settings");
        headerLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = FIRST_LINE_START;
        panel.add(headerLabel, c);

        JPanel blankPanel = new JPanel();
        blankPanel.setBackground(Color.WHITE);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(blankPanel, c);
        
        //Add vibrant setting
        JLabel optionLabel = new JLabel("Show Click/Option-Click Labels in Main Menu");
        optionLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = FIRST_LINE_START;
        panel.add(optionLabel, c);
        
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
        panel.add(showButtonPanel, c);
        
        //Add random setting
        JLabel randomLabel = new JLabel("Show Random Lights Option in Main Menu");
        randomLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = FIRST_LINE_START;
        panel.add(randomLabel, c);
        
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
        panel.add(randomButtonPanel, c);
        
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
            panel.add(blankPanel3, c);
        }
        
        //Add save, cancel, reset to defaults panel
        JPanel savePanel = new JPanel();
        savePanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        savePanel.add(saveButton);
        saveButton.addActionListener((ActionEvent e) -> {
            //Add listeners for remaining events
            boolean showLabels = currentLabels;
            boolean showRandom = currentRandom;

            if(showButton.isSelected()){
                showLabels = true;
            } 
            
            if(hideButton.isSelected()){
                showLabels = false;
            }
            
            if(randomButton.isSelected()){
                showRandom = true;
            }
            
            if(noRandomButton.isSelected()){
                showRandom = false;
            }
            
            GeneralSettings.saveSettings(showLabels, showRandom);

            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        savePanel.add(cancelButton);
        cancelButton.addActionListener((ActionEvent e) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        
        JButton resetDefaultsButton = new JButton("Reset to Defaults");
        resetDefaultsButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        savePanel.add(resetDefaultsButton);
        resetDefaultsButton.addActionListener((ActionEvent e) -> {
            GeneralSettings.saveSettings(true, true);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        c.gridx = 0;
        c.gridy = 15;
        panel.add(savePanel, c);
        
        frame.add(panel);
        frame.show();
    }
    
}
