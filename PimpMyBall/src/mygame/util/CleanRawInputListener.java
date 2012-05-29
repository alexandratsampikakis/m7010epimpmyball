/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;

/**
 *
 * @author Jimmy
 */
public abstract class CleanRawInputListener implements RawInputListener {

    @Override
    public void beginInput() {
    }
    
    @Override
    public void endInput() {
    }
    
    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }
    
    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }
    
    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
    }
    
    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
    }
    
    @Override
    public void onKeyEvent(KeyInputEvent evt) {
    }
    
    @Override
    public void onTouchEvent(TouchEvent evt) {
    }
}
