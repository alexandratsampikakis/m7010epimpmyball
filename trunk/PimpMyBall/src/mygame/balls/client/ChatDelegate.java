/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

/**
 *
 * @author Jimmy
 */
public interface ChatDelegate {
    
    public void onIncomingMessage(String msg, long sender);
    public long getChatterId();
    
    // Should maybe also have
    // public void onOutgoingMessageChanged(...);
    // public void sendMessage(...);
}
