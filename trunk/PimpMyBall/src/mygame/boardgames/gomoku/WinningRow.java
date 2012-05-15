/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import mygame.boardgames.Direction;
import mygame.boardgames.GridPoint;

/**
 *
 * @author Jimmy
 */
public class WinningRow {
    
    /**
     * Shouldn't be public! :)
     */
    protected CellColor winner = CellColor.NONE;
    protected GridPoint start = new GridPoint(), end = new GridPoint();
    protected Direction direction = Direction.NONE;
    
    
    public WinningRow() {
    }
    
    public CellColor getWinningColor() {
        return winner;
    }
    public void setWinningColor(CellColor color) {
        winner = color;
    }
    
    public GridPoint getStartPoint() {
        return new GridPoint(start);
    }
    public void setStartPoint(GridPoint p) {
        start.set(p);
    }
    
    public GridPoint getEndPoint() {
        return new GridPoint(end);
    }
    public void setEndPoint(GridPoint p) {
        end.set(p);
    }
    
    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction dir) {
        direction = dir;
    }
    
    public int getLength() {
        return Math.max(
                Math.abs(start.row - end.row), 
                Math.abs(start.col - end.col));
    }
}
