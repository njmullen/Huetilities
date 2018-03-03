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
import com.philips.lighting.model.PHLightState;
import java.util.List;
import java.util.Random;

//Randomizes the lights

public class RandomLights {
    
    public RandomLights(PHHueSDK sdk){
        //Get the lights
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = cache.getAllLights();

        //Generate x random colors
        for (int i = 0; i < lights.size(); i++) {
            Random rand = new Random();
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            
            float red = (float) ((r > 0.04045f) ? Math.pow((r + 0.055f) / (1.0f + 0.055f), 2.4f) : (r / 12.92f));
            float green = (float) ((g > 0.04045f) ? Math.pow((g + 0.055f) / (1.0f + 0.055f), 2.4f) : (g / 12.92f));
            float blue = (float) ((b > 0.04045f) ? Math.pow((b + 0.055f) / (1.0f + 0.055f), 2.4f) : (b / 12.92f));        

            float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
            float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
            float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

            float x = X / (X + Y + Z);
            float y = Y / (X + Y + Z);
            
            PHLightState lightState = new PHLightState();
            lightState.setX(x);
            lightState.setY(y);
            bridge.updateLightState(lights.get(i), lightState);
        }

    }
    
}
