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

// Class for controlling the lights. Grabs the image from the display and sets the HUE lights

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LightSyncController {
    
    PHHueSDK sdk;
    Toolkit toolkit;
    Timer timer;
    boolean keepRunning = true;
    static Color[] knownColors;
    boolean darkenLights = false;
    List<PHLight> lights = null;
    
    List<PHLight> offLights = null;
    List<PHLight> primaryLights = null;
    List<PHLight> accentLights = null;
    
    public LightSyncController(PHHueSDK sdk){
        this.sdk = sdk;
    }
    
    public void start(boolean isVibrant, List<PHLight> offLights, List<PHLight> primaryLights, List<PHLight> accentLights){
        toolkit = Toolkit.getDefaultToolkit();
        timer = new Timer();
        keepRunning = true;
        
        this.offLights = offLights;
        this.primaryLights = primaryLights;
        this.accentLights = accentLights;
                
        if(isVibrant){
           timer.schedule(new LightUpdateVibrant(), 0, 500);
        } else {
           timer.schedule(new LightUpdateRealistic(), 0, 500); 
        }
    }
    
    public void stop(){
        keepRunning = false;
    }
    
    class LightUpdateVibrant extends TimerTask {
        @Override
        public void run() {
            if(!keepRunning){
                timer.cancel();
            }
                          
            Thread t = new Thread(() -> {
                try {
                    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    BufferedImage capture = new Robot().createScreenCapture(screenRect);
                    int[][] colors = null;

                    //Capture color palette
                    colors = getPalette(capture, 2);
                       /*
                         float[0][0] = x1;
                         float[0][1] = y1;
                         float[1][0] = x2;
                         float[1][1] = y2;
                       */
                    //Calculate xyvalues for each color into float array xyValues
                    float[][] xyValues = new float[2][2];
                    for(int i = 0; i < 2; i++){
                        float[] HSB = Color.RGBtoHSB(colors[i][0], colors[i][1], colors[i][2], null);
                        Color newColor = Color.getHSBColor(HSB[0], (float)0.55, HSB[2]);

                        colors[i][0] = newColor.getRed();
                        colors[i][1] = newColor.getGreen();
                        colors[i][2] = newColor.getBlue();

                        float red = (float) ((colors[i][0] > 0.04045f) ? Math.pow((colors[i][0] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][0] / 12.92f));
                        float green = (float) ((colors[i][1] > 0.04045f) ? Math.pow((colors[i][1] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][1] / 12.92f));
                        float blue = (float) ((colors[i][2] > 0.04045f) ? Math.pow((colors[i][2] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][2] / 12.92f));  

                        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
                        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
                        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

                        float x = X / (X + Y + Z);
                        float y = Y / (X + Y + Z);

                        xyValues[i][0] = x;
                        xyValues[i][1] = y;
                    }
                    
                    //Set lights based on xy values
                    PHBridge bridge = sdk.getSelectedBridge();
                    //Set primary lights
                    primaryLights.forEach((primaryLight) -> {
                        PHLightState lightState = new PHLightState();
                        lightState.setX(xyValues[0][0]);
                        lightState.setY(xyValues[0][1]);
                        bridge.updateLightState(primaryLight, lightState);
                    });
                    
                    //Set accent lights
                    accentLights.forEach((accentLight) -> {
                        PHLightState lightState = new PHLightState();
                        lightState.setX(xyValues[1][0]);
                        lightState.setY(xyValues[1][1]);
                        bridge.updateLightState(accentLight, lightState);
                    });
              } catch (Exception ex) {
                  ex.printStackTrace();
              }  
          });

          t.start();
        } 
    }
    
    class LightUpdateRealistic extends TimerTask {
        @Override
        public void run() {
            if(!keepRunning){
                timer.cancel();
            }
            
            Thread t = new Thread(() -> {
                try {
                    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    BufferedImage capture = new Robot().createScreenCapture(screenRect);
                    int[][] colors = null;

                    //Capture color palette
                    colors = getPalette(capture, 2);
                    /*
                      float[0][0] = x1;
                      float[0][1] = y1;
                      float[1][0] = x2;
                      float[1][1] = y2;
                    */
                    //Calculate xyvalues for each color into float array xyValues
                    float[][] xyValues = new float[2][2]; 
                    for(int i = 0; i < 2; i++){
                        float red = (float) ((colors[i][0] > 0.04045f) ? Math.pow((colors[i][0] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][0] / 12.92f));
                        float green = (float) ((colors[i][1] > 0.04045f) ? Math.pow((colors[i][1] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][1] / 12.92f));
                        float blue = (float) ((colors[i][2] > 0.04045f) ? Math.pow((colors[i][2] + 0.055f) / (1.0f + 0.055f), 2.4f) : (colors[i][2] / 12.92f));        

                        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
                        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
                        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

                        float x = X / (X + Y + Z);
                        float y = Y / (X + Y + Z);

                        xyValues[i][0] = x;
                        xyValues[i][1] = y;
                    }

                    //Set lights based on xy values
                    PHBridge bridge = sdk.getSelectedBridge();
                    //Set primary lights
                    primaryLights.forEach((primaryLight) -> {
                        PHLightState lightState = new PHLightState();
                        lightState.setX(xyValues[0][0]);
                        lightState.setY(xyValues[0][1]);
                        bridge.updateLightState(primaryLight, lightState);
                    });
                    
                    //Set accent lights
                    accentLights.forEach((accentLight) -> {
                        PHLightState lightState = new PHLightState();
                        lightState.setX(xyValues[1][0]);
                        lightState.setY(xyValues[1][1]);
                        bridge.updateLightState(accentLight, lightState);
                    });
              } catch (Exception ex) {
                  //
              }  
          });

          t.start();
        } 
    }
    
    /** 
     * 
     * The following code is copyrighted to Lokesh Dhakar from his ColorTheif project
     * 
     * The MIT License (MIT)

        Copyright (c) 2015 Lokesh Dhakar

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
     *
     */
    
    private static final int DEFAULT_QUALITY = 10;
    private static final boolean DEFAULT_IGNORE_WHITE = true;

    /**
     * Use the median cut algorithm to cluster similar colors and return the
     * base color from the largest cluster.
     *
     * @param sourceImage
     *            the source image
     *
     * @return the dominant color as RGB array
     */
    public static int[] getColor(BufferedImage image){
        int[][] palette = getPalette(image, 5);
        if (palette == null) return null;
        int[] dominantColor = palette[0];
        return dominantColor;
    }

    /**
     * Use the median cut algorithm to cluster similar colors and return the
     * base color from the largest cluster.
     *
     * @param sourceImage
     *            the source image
     * @param quality
     *            0 is the highest quality settings. 10 is the default. There is
     *            a trade-off between quality and speed. The bigger the number,
     *            the faster a color will be returned but the greater the
     *            likelihood that it will not be the visually most dominant
     *            color.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return the dominant color as RGB array
     */
    public static int[] getColor(BufferedImage sourceImage, int quality, boolean ignoreWhite){
        int[][] palette = getPalette(sourceImage, 5, quality, ignoreWhite);
        if (palette == null) return null;
        int[] dominantColor = palette[0];
        return dominantColor;
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * 
     * @return the palette as array of RGB arrays
     */
    public static int[][] getPalette(BufferedImage sourceImage, int colorCount){
        MMCQ.CMap cmap = getColorMap(sourceImage, colorCount);
        if (cmap == null) return null;
        return cmap.palette();
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * @param quality
     *            0 is the highest quality settings. 10 is the default. There is
     *            a trade-off between quality and speed. The bigger the number,
     *            the faster the palette generation but the greater the
     *            likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return the palette as array of RGB arrays
     */
    public static int[][] getPalette(BufferedImage sourceImage, int colorCount, int quality, boolean ignoreWhite){
        MMCQ.CMap cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
        if (cmap == null) return null;
        return cmap.palette();
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * 
     * @return the color map
     */
    public static MMCQ.CMap getColorMap(BufferedImage sourceImage, int colorCount){
        return getColorMap(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * @param quality
     *            0 is the highest quality settings. 10 is the default. There is
     *            a trade-off between quality and speed. The bigger the number,
     *            the faster the palette generation but the greater the
     *            likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return the color map
     */
    public static MMCQ.CMap getColorMap(BufferedImage sourceImage, int colorCount, int quality, boolean ignoreWhite) {
        int[][] pixelArray;

        switch (sourceImage.getType()){
        case BufferedImage.TYPE_3BYTE_BGR:
        case BufferedImage.TYPE_4BYTE_ABGR:
            pixelArray = getPixelsFast(sourceImage, quality, ignoreWhite);
            break;
        default:
            pixelArray = getPixelsSlow(sourceImage, quality, ignoreWhite);
        }

        // Send array to quantize function which clusters values using median
        // cut algorithm
        MMCQ.CMap cmap = MMCQ.quantize(pixelArray, colorCount);
        return cmap;
    }

    /**
     * Gets the image's pixels via BufferedImage.getRaster().getDataBuffer().
     * Fast, but doesn't work for all color models.
     * 
     * @param sourceImage
     *            the source image
     * @param quality
     *            0 is the highest quality settings. 10 is the default. There is
     *            a trade-off between quality and speed. The bigger the number,
     *            the faster the palette generation but the greater the
     *            likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return an array of pixels (each an RGB int array)
     */
    private static int[][] getPixelsFast(BufferedImage sourceImage, int quality, boolean ignoreWhite) {
        DataBufferByte imageData = (DataBufferByte) sourceImage.getRaster().getDataBuffer();
        byte[] pixels = imageData.getData();
        int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

        int colorDepth;
        int type = sourceImage.getType();
        
        switch (type){
        case BufferedImage.TYPE_3BYTE_BGR:
            colorDepth = 3;
            break;
        case BufferedImage.TYPE_4BYTE_ABGR:
            colorDepth = 4;
            break;
        default:
            throw new IllegalArgumentException("Unhandled type: " + type);
        }

        int expectedDataLength = pixelCount * colorDepth;
        if (expectedDataLength != pixels.length){
            throw new IllegalArgumentException("(expectedDataLength = "
                    + expectedDataLength + ") != (pixels.length = "
                    + pixels.length + ")");
        }

        // Store the RGB values in an array format suitable for quantize
        // function

        // numRegardedPixels must be rounded up to avoid an
        // ArrayIndexOutOfBoundsException if all pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;
        int[][] pixelArray = new int[numRegardedPixels][];
        int offset, r, g, b, a;

        // Do the switch outside of the loop, that's much faster
        switch (type){
        case BufferedImage.TYPE_3BYTE_BGR:
            for (int i = 0; i < pixelCount; i += quality){
                offset = i * 3;
                b = pixels[offset] & 0xFF;
                g = pixels[offset + 1] & 0xFF;
                r = pixels[offset + 2] & 0xFF;

                // If pixel is not white
                if (!(ignoreWhite && r > 250 && g > 250 && b > 250)){
                    pixelArray[numUsedPixels] = new int[] {r, g, b};
                    numUsedPixels++;
                }
            }
            break;

        case BufferedImage.TYPE_4BYTE_ABGR:
            for (int i = 0; i < pixelCount; i += quality){
                offset = i * 4;
                a = pixels[offset] & 0xFF;
                b = pixels[offset + 1] & 0xFF;
                g = pixels[offset + 2] & 0xFF;
                r = pixels[offset + 3] & 0xFF;

                // If pixel is mostly opaque and not white
                if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)){
                    pixelArray[numUsedPixels] = new int[] {r, g, b};
                    numUsedPixels++;
                }
            }
            break;

        default:
            throw new IllegalArgumentException("Unhandled type: " + type);
        }

        // Remove unused pixels from the array
        return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
    }

    /**
     * Gets the image's pixels via BufferedImage.getRGB(..). Slow, but the fast
     * method doesn't work for all color models.
     * 
     * @param sourceImage
     *            the source image
     * @param quality
     *            0 is the highest quality settings. 10 is the default. There is
     *            a trade-off between quality and speed. The bigger the number,
     *            the faster the palette generation but the greater the
     *            likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return an array of pixels (each an RGB int array)
     */
    private static int[][] getPixelsSlow(BufferedImage sourceImage, int quality, boolean ignoreWhite){
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        int pixelCount = width * height;

        // numRegardedPixels must be rounded up to avoid an
        // ArrayIndexOutOfBoundsException if all pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;

        for (int i = 0; i < pixelCount; i += quality){
            int row = i / width;
            int col = i % width;
            int rgb = sourceImage.getRGB(col, row);

            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = (rgb) & 0xFF;
            if (!(ignoreWhite && r > 250 && r > 250 && r > 250)){
                res[numUsedPixels] = new int[] {r, g, b};
                numUsedPixels++;
            }
        }

        return Arrays.copyOfRange(res, 0, numUsedPixels);
    }
    
}
