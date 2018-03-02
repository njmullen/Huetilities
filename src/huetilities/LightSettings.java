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

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Frame for individual light settings

public class LightSettings {
    
    JFrame frame;
    JPanel panel;
    
    public LightSettings(){
        frame = new JFrame("Light Settings");
        frame.setSize(800, 800);
        frame.setResizable(false);
        panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.WHITE);
        
        JColorChooser colorChooser = new JColorChooser(panel.getBackground());
        
        ColorSelectionModel model = colorChooser.getSelectionModel();
        ChangeListener changeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent changeEvent) {
            Color newForegroundColor = colorChooser.getColor();
            System.out.println(newForegroundColor);
          }
        };
        model.addChangeListener(changeListener);
        panel.add(colorChooser);

        frame.add(panel);
        frame.show();
    }
    
}
