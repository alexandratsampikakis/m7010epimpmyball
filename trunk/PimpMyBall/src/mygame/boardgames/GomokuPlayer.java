/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

/**
 *
 * @author Jimmy
 */
public class GomokuPlayer {
    
    long id;
    CellColor color;
    
    public GomokuPlayer(long id, CellColor color) {
        this.id = id;
        this.color = color;
    }
    
    public long getID() {
        return id;
    }
    public CellColor getColor() {
        return color;
    }
}
