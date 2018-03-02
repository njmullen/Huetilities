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
import java.util.Properties;

//Manages the bridge connection properties (username, last connected IP)

public final class HuetilitiesProperties {
    private static final String LAST_CONNECTED_IP   = "LastIPAddress";
    private static final String USER_NAME           = "WhiteListUsername";
    private static Properties props = null;

    private HuetilitiesProperties() {
        //
    }
    
    public static void storeLastIPAddress(String ipAddress) {
        props.setProperty(LAST_CONNECTED_IP, ipAddress);
        saveProperties();
    }

    /**
     * Stores the Username (for Whitelist usage). This is generated as a random 16 character string.
     */
    public static void storeUsername(String username) {
        props.setProperty(USER_NAME, username);
        saveProperties();
    }

    /**
     * Returns the stored Whitelist username.  If it doesn't exist we generate a 16 character random string and store this in the properties file.
     */
    public static String getUsername() {
        String username = props.getProperty(USER_NAME);        
        return username;
    }

    public static String getLastConnectedIP() {
        return props.getProperty(LAST_CONNECTED_IP);
    }
    
    public static void loadProperties() {
        if (props == null) {
            props = new Properties();
            FileInputStream in;
            
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/Huetilities");
            file.mkdir();
            
            try {
                in = new FileInputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/Huetilities.properties");
                props.load(in);
                in.close();
            } catch (FileNotFoundException ex) {
                saveProperties();
            } catch (IOException e) {
                // Handle the IOException.
            }
        }
    }

    public static void saveProperties() {
        try {
            File file = new File(System.getProperty("user.home") + "/Library/Application Support/MovieHue");
            file.mkdir();
            
            FileOutputStream out = new FileOutputStream(System.getProperty("user.home") + "/Library/Application Support/Huetilities/Huetilities.properties");
            props.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            // Handle the FileNotFoundException.
        } catch (IOException e) {
            // Handle the IOException.
        }
    } 
}


