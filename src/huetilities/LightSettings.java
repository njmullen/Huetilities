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
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
    
    public LightSettings(PHHueSDK sdk){
        frame = new JFrame("Light Settings");
        frame.setSize(500, 200);
        frame.setResizable(true);
        panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Light Settings");
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        JLabel infoLabel = new JLabel("Click a light to change color/brightness");
        infoLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);
        
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();
        
        JPanel lightsPanel = new JPanel(new WrapLayout());
        lightsPanel.setBackground(Color.WHITE);
        for(int i = 0; i < lights.size(); i++){
            String lightName = lights.get(i).getName();
            PHLight thisLight = lights.get(i);
            JButton lightButton = new JButton(lightName);
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
                //Create a custom frame for managing this light
                JFrame lightFrame = new JFrame();
                lightFrame.setSize(600, 500);
                lightFrame.setLocationRelativeTo(null);
                lightFrame.setResizable(true);
                JPanel lightPanel = new JPanel();
                lightPanel.setBackground(Color.WHITE);
                
                JLabel lightTitle = new JLabel();
                lightTitle.setText(lightName);
                lightTitle.setFont(new Font("Helvetica", Font.BOLD, 20));
                lightPanel.add(lightTitle);
                
                JLabel brightnessTitle = new JLabel("Brightness");
                brightnessTitle.setFont(new Font("Helvetica", Font.BOLD, 14));
                lightPanel.add(brightnessTitle);
                
                PHLightState lightState = thisLight.getLastKnownLightState();
                int brightness = lightState.getBrightness();
                
                JLabel brightnessLabel = new JLabel();
                brightnessLabel.setText(Integer.toString(brightness));
                brightnessLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
                lightPanel.add(brightnessLabel);
                
                JSlider slider = new JSlider(0, 254, brightness);
                slider.addChangeListener((ChangeEvent event) -> {
                    lightState.setBrightness(slider.getValue());
                    bridge.updateLightState(thisLight, lightState);
                    brightnessLabel.setText(Integer.toString(slider.getValue()));
                });
                lightPanel.add(slider);
                
                //Color currentLightColor = convertToColor(lightState.getX(), lightState.getY(), brightness);
                
                JColorChooser colorChooser = new JColorChooser(Color.RED);
                colorChooser.setBackground(Color.WHITE);
                colorChooser.setPreferredSize(new Dimension(500, 400));
                colorChooser.setFont(new Font("Helvetica", Font.PLAIN, 14));
 
                ColorSelectionModel model = colorChooser.getSelectionModel();
                ChangeListener changeListener = (ChangeEvent changeEvent) -> {
                    Color newForegroundColor = colorChooser.getColor();
                    lightState.setX(getX(newForegroundColor));
                    lightState.setY(getY(newForegroundColor));
                    bridge.updateLightState(thisLight, lightState);
                };
                model.addChangeListener(changeListener);
                lightPanel.add(colorChooser);
                
                lightFrame.add(lightPanel);
                lightFrame.show();
            });
            lightsPanel.add(lightButton);
        }
        panel.add(lightsPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton exitButton = new JButton("Close");
        exitButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        exitButton.addActionListener((ActionEvent e) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        buttonPanel.add(exitButton);
        panel.add(buttonPanel);
        
        /*JColorChooser colorChooser = new JColorChooser(panel.getBackground());
        
        ColorSelectionModel model = colorChooser.getSelectionModel();
        ChangeListener changeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent changeEvent) {
            Color newForegroundColor = colorChooser.getColor();
            System.out.println(newForegroundColor);
          }
        };
        model.addChangeListener(changeListener);
        panel.add(colorChooser);*/
        
        frame.add(panel);
        frame.show();
    }
    
    public Color convertToColor(float x, float y, int brightness){
        float z = (float)1.0 - x - y; 
        float Y = brightness;
        float X = (Y / y) * x;  
        float Z = (Y / y) * z;
        
        float r = X * (float)1.612 - Y * (float)0.203 - Z * (float)0.302;
        float g = -X * (float)0.509 + Y * (float)1.412 + Z * (float)0.066;
        float b = X * (float)0.026 - Y * (float)0.072 + Z * (float)0.962;
        
        r = (float) (r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * Math.pow(r, (1.0f / 2.4f)) - 0.055f);
        g = (float) (g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * Math.pow(g, (1.0f / 2.4f)) - 0.055f);
        b = (float) (b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * Math.pow(b, (1.0f / 2.4f)) - 0.055f);
        
        Color color = new Color(r, g, b);
        return color;
    }
    
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
