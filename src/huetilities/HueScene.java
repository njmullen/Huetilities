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

//Custom class representing a scene

import java.io.Serializable;
import java.util.ArrayList;

public class HueScene implements Serializable {
    
    String name;
    ArrayList<LightState> lights;
    
    public HueScene(String name){
        this.name = name;
        lights = new ArrayList<LightState>();
    }
    
    public HueScene(String name, ArrayList<LightState> lights){
        this.name = name;
        this.lights = lights;
    }
    
    public void addLight(LightState light){
        lights.add(light);
    }
    
    public void addLights(ArrayList<LightState> lights){
        this.lights = lights;
    }
    
    public ArrayList<LightState> getLights(){
        return lights;
    }
    
    public String getName(){
        return name;
    }
    
}
