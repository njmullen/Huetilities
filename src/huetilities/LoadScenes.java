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

//Loads/saves scenes

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//Class for managing saved scenes

public class LoadScenes implements Serializable {
    
    static ArrayList<HueScene> scenes;
    
    public static void saveScene(HueScene newScene){
        try {
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/Huetilities/Scenes");
            file.mkdir();
            String sceneName = newScene.getName();
            String path = "/Library/Application Support/Huetilities/Scenes/" + sceneName + ".ser";
            FileOutputStream save = new FileOutputStream(System.getProperty("user.home") + path);
            ObjectOutputStream saveStream = new ObjectOutputStream(save);
            saveStream.writeObject(newScene);
            saveStream.close();
            loadScenes();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    public static void loadScenes(){
        try {
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/Huetilities/Scenes");
            File[] fileList = file.listFiles();
            scenes = new ArrayList<HueScene>();
            for(int i = 0; i < fileList.length; i++){
                FileInputStream load = new FileInputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/Scenes/" + fileList[i].getName());
                ObjectInputStream loadStream = new ObjectInputStream(load);
                HueScene scene = (HueScene)loadStream.readObject();
                scenes.add(scene);
                loadStream.close();
            }
            
            for(int i = 0; i < scenes.size(); i++){
                System.out.println(scenes.get(i).getName());
            }
            
        } catch (FileNotFoundException e){
            //
        } catch (IOException ex){
            ex.printStackTrace();
        } catch (ClassNotFoundException exx){
            exx.printStackTrace();
        } catch (NullPointerException npex){
            scenes = new ArrayList<HueScene>();
        }
    }
    
    public static void deleteScene(HueScene sceneToDelete){
        
    }
    
    public static ArrayList<HueScene> getScenes(){
        return scenes;
    }
    
}
