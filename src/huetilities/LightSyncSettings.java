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

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Loads and manages settings for the LightSync functionality

public class LightSyncSettings implements Serializable {
    
    static boolean vibrant = false;
    static List<PHLight> offLights;
    static List<PHLight> primaryLights;
    static List<PHLight> accentLights;
    static PHHueSDK sdk;
    
    public static void saveSettings(boolean vibrant, List<PHLight> offLights, List<PHLight> primaryLights, List<PHLight> accentLights){
        try {
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/Huetilities");
            file.mkdir();
            FileOutputStream save = new FileOutputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/LightSyncSettings.ser");
            ObjectOutputStream saveStream = new ObjectOutputStream(save);
            List<String> offString = convertToStringArray(offLights);
            List<String> primaryString = convertToStringArray(primaryLights);
            List<String> accentString = convertToStringArray(accentLights);
            saveStream.writeObject(vibrant);
            saveStream.writeObject(offString);
            saveStream.writeObject(primaryString);
            saveStream.writeObject(accentString);
            saveStream.close();
        } catch (IOException ex) {
            System.err.println("Save error");
            ex.printStackTrace();
        }
    }
    
    public static void loadSettings(PHHueSDK sdk){
        try {
            FileInputStream load = new FileInputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/LightSyncSettings.ser");
            ObjectInputStream loadStream = new ObjectInputStream(load);
            vibrant = (boolean)loadStream.readObject();
            List<String> offLightsString = (List<String>)loadStream.readObject();
            List<String> primaryLightsString = (List<String>)loadStream.readObject();
            List<String> accentLightsString = (List<String>)loadStream.readObject();
            loadStream.close();
            
            offLights = convertToLightArray(offLightsString, sdk);
            primaryLights = convertToLightArray(primaryLightsString , sdk);
            accentLights = convertToLightArray(accentLightsString, sdk);  
        } catch (FileNotFoundException ex) {
            List<PHLight> blankList = new ArrayList<PHLight>();
            PHBridge bridge = sdk.getSelectedBridge();
            PHBridgeResourcesCache cache = bridge.getResourceCache();
            List<PHLight> allLights = cache.getAllLights();
            
            saveSettings(false, blankList, allLights, blankList);
            primaryLights = allLights;
        } catch (IOException exx){
            exx.printStackTrace();
        } catch (ClassNotFoundException exxxx){
            exxxx.printStackTrace();
        }
    }
    
    public static List<String> convertToStringArray(List<PHLight> lights){
        List<String> stringList = new ArrayList<String>();
        for(int i = 0; i < lights.size(); i++){
            stringList.add(lights.get(i).getName());
        }
        return stringList;
    }
    
    public static List<PHLight> convertToLightArray(List<String> strings, PHHueSDK sdk){
        List<PHLight> lights = new ArrayList<PHLight>();
        
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> allLights = cache.getAllLights();
                
        for(int i = 0; i < allLights.size(); i++){
            if(strings.contains(allLights.get(i).getName())){
                lights.add(allLights.get(i));
            }
        }
        
        return lights;
    }
    
    public static boolean getVibrant(){
        return vibrant;
    }
    
    public static List<PHLight> getOffLights(){
        return offLights;
    }
    
    public static List<PHLight> getPrimaryLights(){
        return primaryLights;
    }
    
    public static List<PHLight> getAccentLights(){
        return accentLights;
    }  
}

