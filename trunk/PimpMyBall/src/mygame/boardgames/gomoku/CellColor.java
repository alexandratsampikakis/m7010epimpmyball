/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import com.jme3.material.Material;
import java.util.Random;

/**
 *
 * @author Jimmy
 */
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
    
    public int getIndex() {
        switch (this) {
            case RED:
                return 0;
            case BLUE:
                return 1;
            default:
                return -1;
        }
    }
    
    private static Random rand = new Random();
    public static CellColor randomColor() {
        if ((rand.nextInt() % 2) == 0)
            return BLUE;
        return RED;
    }
}
