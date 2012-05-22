/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.network.serializing.Serializable;
import java.util.Random;

/**
 *
 * @author Jimmy
 */
@Serializable
public enum CellColor {

    RED,
    BLUE,
    NONE;
    
    public CellColor opponent() {
        switch (this) {
            case RED:
                return BLUE;
            case BLUE:
                return RED;
            default:
                return NONE;
        }
    }
    
    public ColorRGBA getColorRGBA() {
        switch (this) {
            case RED:
                return ColorRGBA.Red;
            case BLUE:
                return ColorRGBA.Blue;
            default:
                return ColorRGBA.White;
        }
    }
    
    private static Random rand = new Random();
    public static CellColor randomColor() {
        if ((rand.nextInt() % 2) == 0)
            return BLUE;
        return RED;
    }
}
