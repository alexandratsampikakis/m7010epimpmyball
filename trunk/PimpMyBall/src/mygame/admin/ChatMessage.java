/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Jimmy
 */
@Serializable
public class ChatMessage extends AbstractMessage {
    
    private long senderId;
    private String text;
    
    public ChatMessage() {
    }
    
    public ChatMessage(String text, long senderId) {
        this.text = text;
        this.senderId = senderId;
    }
    
    public long getSenderId() {
        return senderId;
    }
    public String getText() {
        return text;
    }
}
