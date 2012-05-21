/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.serializing.Serializer;

/**
 *
 * @author Jimmy
 */
public class SerializerHelper {
 
    public static void initializeClientClasses() {
        Serializer.registerClass(LoginMessage.class);
        Serializer.registerClass(LoginFailedMessage.class);
    }
    
}
