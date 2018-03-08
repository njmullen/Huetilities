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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

//Loads and manages settings for general functionality 

public class GeneralSettings implements Serializable {
    
    static boolean showLabels = true;
    static boolean showRandom = true;
    
    public static void saveSettings(boolean labels, boolean random){
        try {
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/Huetilities");
            file.mkdir();
            FileOutputStream save = new FileOutputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/HuetilitiesSettings.ser");
            ObjectOutputStream saveStream = new ObjectOutputStream(save);
            saveStream.writeObject(labels);
            saveStream.writeObject(random);
            saveStream.close();
        } catch (IOException ex) {
            System.err.println("Save error");
            ex.printStackTrace();
        }
    }
    
    public static void loadSettings(){
        try {
            FileInputStream load = new FileInputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/HuetilitiesSettings.ser");
            ObjectInputStream loadStream = new ObjectInputStream(load);
            showLabels = (boolean)loadStream.readObject();
            showRandom = (boolean)loadStream.readObject();
            loadStream.close(); 
        } catch (FileNotFoundException ex) {
            saveSettings(true, true);
        } catch (IOException exx){
            exx.printStackTrace();
        } catch (ClassNotFoundException exxxx){
            exxxx.printStackTrace();
        }
    }
}

