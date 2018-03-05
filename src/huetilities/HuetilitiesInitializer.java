/*
 * The MIT License
 *
 * Copyright 2018 Nick Mullen
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

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import java.awt.Color;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

// Initializes the SDK and neccesary components for controlling the HUE lights and connects
// to a bridge if a bridge connection is not yet found

public final class HuetilitiesInitializer {
    
    PHHueSDK sdk = PHHueSDK.create();
    JFrame connectionFrame;
    JPanel connectionPanel;
    
    public HuetilitiesInitializer(){
        //If the user has never used MovieHue before, open connection GUI to connect to a bridge
        initializeProperties();
        //Succesful connection, open system tray
        if(connectToLastKnownAccessPoint()){
            try {
                //Sleep for 2s to connect to bridge (Fix this)
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            SystemTrayMenu stm = new SystemTrayMenu(sdk);
            //Add light controller initialization and open app
        //Open connection GUI
        } else {
            connectionFrame = new JFrame("Huetiltiies");
            connectionFrame.setSize(500, 300);
            connectionPanel = new JPanel();
            connectionPanel.setBackground(Color.WHITE);
            
            //Main logo
            JLabel movieHueLogo = new JLabel();
            Image movieImage = null;
            try {
                movieImage = ImageIO.read(getClass().getResource("Pushlink.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            movieImage = movieImage.getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            ImageIcon movieIcon = new ImageIcon(movieImage);
            movieHueLogo.setIcon(movieIcon);
            connectionPanel.add(movieHueLogo);
            
            JLabel connectionLabel = new JLabel("Push the button on your HUE bridge now");
            connectionPanel.add(connectionLabel);
            
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            connectionPanel.add(progressBar);
            
            findBridges();
            
            connectionFrame.add(connectionPanel);
            connectionFrame.show();
        }
    }
    
    //Loads the properties and initializes notification manager/listener
    public void initializeProperties(){
        HuetilitiesProperties.loadProperties();
        sdk.getNotificationManager().registerSDKListener(getListener());
    }
  
    //Scans for bridges, and will authenticate a bridge if the pushlink is pushed
    public void findBridges(){
        sdk = PHHueSDK.getInstance();
        PHBridgeSearchManager sm = (PHBridgeSearchManager) sdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }
    
    //Attempts to connect to saved access point in properties
    //@returns: true if succesful, false if not
    public boolean connectToLastKnownAccessPoint() {
        String username = HuetilitiesProperties.getUsername();
        String lastIpAddress =  HuetilitiesProperties.getLastConnectedIP();     
        
        if (username == null || lastIpAddress == null) {
            return false;
        }
        
        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(lastIpAddress);
        accessPoint.setUsername(username);
        sdk.connect(accessPoint);
        return true;
    }
    
    //Listener that updates whenever actions happen with the HUE bridge
    private PHSDKListener listener = new PHSDKListener() {
        @Override
        public void onCacheUpdated(java.util.List<Integer> list, PHBridge phb) {
            //
        }

        @Override
        public void onBridgeConnected(PHBridge phb, String username) {
            sdk.setSelectedBridge(phb);
            sdk.enableHeartbeat(phb, PHHueSDK.HB_INTERVAL);
            String lastIpAddress = phb.getResourceCache().getBridgeConfiguration().getIpAddress();  
                        
            connectionFrame.dispatchEvent(new WindowEvent(connectionFrame, WindowEvent.WINDOW_CLOSING));
            
            HuetilitiesProperties.storeUsername(username);
            HuetilitiesProperties.storeLastIPAddress(lastIpAddress);
            HuetilitiesProperties.saveProperties();
            
            SystemTrayMenu stm = new SystemTrayMenu(sdk);
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phap) {
            sdk.startPushlinkAuthentication(phap);
        }

        @Override
        public void onAccessPointsFound(java.util.List<PHAccessPoint> list) {
            if (list.size()== 1){
                PHAccessPoint accessPoint = list.get(0);
                sdk.connect(list.get(0));
            } else {
                System.out.println("Error PHSDKListener/onAccessPointsFound :: Multiple bridges found, operation not supported");
            }
        }

        @Override
        public void onError(int code, String string) {
            switch (code) {
                case PHHueError.BRIDGE_NOT_RESPONDING:
                    break;
                case PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED:
                    break;
                case PHMessageType.PUSHLINK_AUTHENTICATION_FAILED:
                    break;
                case PHMessageType.BRIDGE_NOT_FOUND:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onConnectionResumed(PHBridge phb) {
            //
        }

        @Override
        public void onConnectionLost(PHAccessPoint phap) {
            //
        }

        @Override
        public void onParsingErrors(java.util.List<PHHueParsingError> list) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }    
    };
    
    //Returns the listener
    public PHSDKListener getListener() {
        return listener;
    } 
    
}
