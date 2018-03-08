/*
 * The MIT License
 *
 * Copyright 2018 Nick Mullen.
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

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Frame for individual light settings

public class LightSettings {
    
    JFrame frame;
    JPanel panel;
    Timer timer;
    Color chosenColor;
    Color lastColor;
    boolean startChanging = false;
    
    public LightSettings(PHHueSDK sdk, PHLight light){
        //Create a custom frame for managing this light
        JFrame lightFrame = new JFrame();
        lightFrame.setSize(700, 500);
        lightFrame.setLocationRelativeTo(null);
        lightFrame.setResizable(true);
        JPanel lightPanel = new JPanel();
        lightPanel.setBackground(Color.WHITE);
        
        PHBridge bridge = sdk.getSelectedBridge();
        timer = new Timer();

        JLabel lightTitle = new JLabel();
        lightTitle.setText(light.getName());
        lightTitle.setFont(new Font("Helvetica", Font.BOLD, 20));
        lightPanel.add(lightTitle);

        JLabel brightnessTitle = new JLabel("Brightness");
        brightnessTitle.setFont(new Font("Helvetica", Font.BOLD, 14));
        lightPanel.add(brightnessTitle);

        PHLightState lightState = light.getLastKnownLightState();
        int brightness = lightState.getBrightness();
        double brightnessPercentage = (((double)(brightness)/(double)254) * 100.0);
        String brightnessDisplay = Integer.toString((int)brightnessPercentage);

        JLabel brightnessLabel = new JLabel();
        brightnessLabel.setText(brightnessDisplay);
        brightnessLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        lightPanel.add(brightnessLabel);
        
        //Set brightness slider
        JSlider slider = new JSlider(0, 254, brightness);
        slider.addChangeListener((ChangeEvent event) -> {
            lightState.setBrightness(slider.getValue());
            bridge.updateLightState(light, lightState);
            int currentBrightness = slider.getValue();
            double currentBrightnessPercentage = (((double)(currentBrightness)/(double)254) * 100.0);
            String brightnessToDisplay = Integer.toString((int)currentBrightnessPercentage);
            brightnessLabel.setText(brightnessToDisplay);
        });
        lightPanel.add(slider);
        
        //Add color chooser
        JColorChooser colorChooser = new JColorChooser(Color.RED);
        colorChooser.setBackground(Color.WHITE);
        JPanel blankPanel = new JPanel();
        blankPanel.setBackground(Color.WHITE);
        colorChooser.setPreviewPanel(blankPanel);
        colorChooser.setPreferredSize(new Dimension(700, 400));
        colorChooser.setFont(new Font("Helvetica", Font.PLAIN, 14));
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
           if(accp.getDisplayName().equals("HSB") || accp.getDisplayName().equals("CMYK") || accp.getDisplayName().equals("HSL")) {
              colorChooser.removeChooserPanel(accp);
           } 
        }
        
        //If the color has changed from last time it checked, update color
        //Checks for new color every 200ms
        class UpdateColor extends TimerTask {
            @Override
            public void run() {
               if(chosenColor != lastColor){
                  lightState.setX(getX(chosenColor));
                  lightState.setY(getY(chosenColor));
                  bridge.updateLightState(light, lightState); 
                  lastColor = chosenColor;
               } 
            }
        }
        
        //Listen for color changes and schedule the timer to listen for color changes
        //after the user first starts changing color until timer is cancelled on window closing
        ColorSelectionModel model = colorChooser.getSelectionModel();
        ChangeListener changeListener = (ChangeEvent changeEvent) -> {
            Color newColor = colorChooser.getColor();
            chosenColor = newColor;
            if(!startChanging){
                timer.schedule(new UpdateColor(), 0, 200);
                startChanging = true;
            }
        };

        model.addChangeListener(changeListener);
        lightPanel.add(colorChooser);
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        lightPanel.add(closeButton);
        closeButton.addActionListener((ActionEvent e) -> {
            lightFrame.dispatchEvent(new WindowEvent(lightFrame, WindowEvent.WINDOW_CLOSING));
        });

        lightFrame.add(lightPanel);
        lightFrame.show();
        
        //Cancel timer upon window close
        lightFrame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                timer.cancel();
            }
        });
    }
   
    //Get x and y values for RGB color
    public float getX(Color c){
        float red = (float) ((c.getRed() > 0.04045f) ? Math.pow((c.getRed() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getRed() / 12.92f));
        float green = (float) ((c.getGreen() > 0.04045f) ? Math.pow((c.getGreen() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getGreen() / 12.92f));
        float blue = (float) ((c.getBlue() > 0.04045f) ? Math.pow((c.getBlue() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getBlue() / 12.92f));        

        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

        float x = X / (X + Y + Z);
        
        return x;
    }
    
    public float getY(Color c){
        float red = (float) ((c.getRed() > 0.04045f) ? Math.pow((c.getRed() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getRed() / 12.92f));
        float green = (float) ((c.getGreen() > 0.04045f) ? Math.pow((c.getGreen() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getGreen() / 12.92f));
        float blue = (float) ((c.getBlue() > 0.04045f) ? Math.pow((c.getBlue() + 0.055f) / (1.0f + 0.055f), 2.4f) : (c.getBlue() / 12.92f));        

        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

        float y = Y / (X + Y + Z);
        
        return y;
    }
}