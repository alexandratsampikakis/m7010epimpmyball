/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import mygame.util.Direction;
import mygame.util.GridPoint;

/**
 *
 * @author Jimmy
 */
public class WinningRow {
 
    private final CellColor winner;
    private final GridPoint start, end;
    private final Direction direction;

    public WinningRow(GomokuGrid grid) {
        
        Direction[] checkDirs = {
            Direction.EAST, 
            Direction.SOUTH, 
            Direction.SOUTH_EAST, 
            Direction.SOUTH_WEST,
        };
        
        int rows = grid.getRows();
        int cols = grid.getCols();
        int inRowToWin = grid.getNumInRowToWin();

        CellColor tempWinner = CellColor.NONE;
        GridPoint tempStart = new GridPoint();
        GridPoint tempEnd = new GridPoint();
        Direction tempDir = Direction.NONE;
        
        CellColor color;
        GridPoint p = new GridPoint();
        
        for (p.row = 0; p.row < rows; p.row++) {
            for (p.col = 0; p.col < cols; p.col++) {
                
                color = grid.getState(p);
                
                if (color != CellColor.NONE) {
                    
                    for (Direction dir : checkDirs) {
                        int inRow = grid.countRow(p, dir, color);

                        if (inRow >= inRowToWin) {
                            tempStart.set(p);
                            tempEnd.row = p.row + dir.dr * inRow;
                            tempEnd.col = p.col + dir.dc * inRow;
                            tempDir = dir;
                            tempWinner = color;
                            
                            // Break all loops
                            p.set(rows, cols);
                            break;
                        }
                    }
                }
            }
        }
        
        this.winner = tempWinner;
        this.start = tempStart;
        this.end = tempEnd;
        this.direction = tempDir;
    }
    
    public CellColor getWinningColor() {
        return winner;
    }
    
    public GridPoint getStartPoint() {
        // Return new point to keep this object immutable
        return new GridPoint(start);
    }
    
    public GridPoint getEndPoint() {
        // Return new point to keep this object immutable
        return new GridPoint(end);
    }
    
    public Direction getDirection() {
        return direction;
    }

    public int getRowLength() {
        return (winner == CellColor.NONE) ?
                0 :
                Math.max(
                Math.abs(start.row - end.row), 
                Math.abs(start.col - end.col));
    }
}





