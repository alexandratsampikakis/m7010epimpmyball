/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import java.util.ArrayList;
import mygame.boardgames.Direction;
import mygame.boardgames.GridPoint;
import mygame.boardgames.GridSize;
import mygame.boardgames.MoveListener;

/**
 *
 * @author Jimmy
 */
public class GomokuGrid {
    
    private int inRowToWin = 5;
    private GridSize size;
    private CellColor grid[][];
    private int numPlaced = 0;
    
    private ArrayList<MoveListener> listeners = null;
            
    public GomokuGrid(GridSize size) {
        this(size.rows, size.cols);
    };
    
    public GomokuGrid(int numRows, int numCols) {
        size = new GridSize(numRows, numCols);
        grid = new CellColor[numRows][numCols];
        reset();
    }
    
    public GomokuGrid(GomokuGrid g) {
        this(g.getRows(), g.getCols());
        
        GridPoint p = new GridPoint();
        CellColor color;
        
        for (int i = 0; i < size.rows; i++) {
            p.row = i;
            for (int j = 0; j < size.cols; j++) {
                p.col = j;
                color = g.getState(p);
                if (color != CellColor.NONE){
                    setState(p, color);
                    numPlaced++;
                }
            }
        }
    }
    
    public void addMoveListener(MoveListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<MoveListener>();
        }
        listeners.add(listener);
    }
            
    public int getNumInRowToWin() {
        return inRowToWin;
    }
    public void setNumInRowToWin(int inRow) {
        assert (inRow > 2 && inRow <= size.rows && inRow <= size.cols);
        inRowToWin = inRow;
    }
    
    public int getRows() {
        return size.rows;
    }
    public int getCols() {
        return size.cols;
    }
    public GridSize getSize() {
        return size;
    }
    
    public void reset() {
        for (int i = 0; i < size.rows; i++) {
            for (int j = 0; j < size.cols; j++) {
                grid[i][j] = CellColor.NONE;
            }
        }
        numPlaced = 0;
    }
    
    public boolean inBounds(GridPoint p) {
        return inBounds(p.row, p.col);
    }
    public boolean inBounds(int row, int col) {
        return (row < size.rows && row >= 0 &&
                col < size.cols && col >= 0);
    }
    
    public CellColor getState(GridPoint p) {
        return getState(p.row, p.col);
    }
    public CellColor getState(int row, int col) {
        if (inBounds(row, col))
            return grid[row][col];
        return CellColor.NONE;
    }
    
    public void setState(GridPoint p, CellColor color) {
        setState(p.row, p.col, color);
    }
    public void setState(int row, int col, CellColor color) {
        if (inBounds(row, col)) {
            grid[row][col] = color;
        }
    }
    
    public boolean isFull() {
        return numPlaced >= (size.rows * size.cols);
    }
    public boolean isEmpty() {
        return numPlaced == 0;
    }
    
    public boolean tryMove(GridPoint p, CellColor player) {
        if (getState(p) != CellColor.NONE) {
            return false;
        }
        setState(p, player);
        numPlaced++;
        
        // Notify listeners
        if (listeners != null) {
            for (MoveListener listener : listeners) {
                listener.onMove(player, p);
            }
        }
        
        return true;
    }
    
    
    public boolean isWinner(CellColor player) {
        return player == getWinner();
    }
    
    public WinningRow getWinningRow() {
        
        CellColor piece;
        WinningRow wr = new WinningRow();
        GridPoint p = new GridPoint();
        
        Direction[] checkDirs = {
            Direction.EAST, 
            Direction.SOUTH, 
            Direction.SOUTH_EAST, 
            Direction.SOUTH_WEST,
        };
        
        for (int i = 0; i < size.rows; i++) {
            p.row = i;
            for (int j = 0; j < size.cols; j++) {
                p.col = j;
                piece = getState(p);
                if (piece != CellColor.NONE) {
                    
                    for (Direction dir : checkDirs) {
                        int inRow = countRow(p, dir, piece);
                        if (inRow >= inRowToWin) {
                            wr.start.set(p);
                            wr.end.row = p.row + dir.dr * inRow;
                            wr.end.col = p.col + dir.dc * inRow;
                            wr.direction = dir;
                            wr.winner = piece;
                            
                            /*
                             wr.setStartPoint(p);
                             wr.setEndPoint(new GridPoint(
                                    p.row + dir.dr * inRow,
                                    p.col + dir.dc * inRow));
                             wr.setDirection(dir);
                             wr.setWinningColor(piece);
                             */
                            
                            return wr;
                        }
                    }
                }
            }
        }
        return wr;
    }
    
    public CellColor getWinner() {
        CellColor piece;
        GridPoint p = new GridPoint();
        for (int i = 0; i < size.rows; i++) {
            p.row = i;
            for (int j = 0; j < size.cols; j++) {
                p.col = j;
                piece = getState(p);
                if (piece != CellColor.NONE) {
                    if (isWinningRow(p, Direction.EAST, piece)
                            || isWinningRow(p, Direction.SOUTH, piece)
                            || isWinningRow(p, Direction.SOUTH_EAST, piece)
                            || isWinningRow(p, Direction.SOUTH_WEST, piece)) {
                        return piece;
                    }
                }
            }
        }
        return CellColor.NONE;
    }

    private boolean isWinningRow(GridPoint p, Direction dir, CellColor color) {
        return countRow(p, dir, color) >= inRowToWin;
    }
    
    public int countRow(GridPoint start, Direction dir, CellColor color) {
        
        if (color == CellColor.NONE) {
            return 0;
        }
        
        int inRow = 0;
        GridPoint p = new GridPoint(start);
        CellColor chk = getState(p);
        
        while (chk == color) {
            p.move(dir);
            chk = getState(p);
            inRow++;
        }
        
        return inRow;
    }
    
    public int getInARowsCRAP(CellColor color, int numInRow, int maxCap) {
        CellColor piece;
        GridPoint p = new GridPoint();
        int count = 0;
        for (int i = 0; i < size.rows; i++) {
            p.row = i;
            for (int j = 0; j < size.cols; j++) {
                p.col = j;
                piece = getState(p);
                if (piece == color) {
                    if (countCapped(p, Direction.EAST, piece, numInRow, maxCap)
                            || countCapped(p, Direction.SOUTH, piece, numInRow, maxCap)
                            || countCapped(p, Direction.SOUTH_EAST, piece, numInRow, maxCap)
                            || countCapped(p, Direction.SOUTH_WEST, piece, numInRow, maxCap)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    private boolean countCapped(GridPoint start, 
            Direction dir, CellColor color, int numInRow, int maxCap) {
        
        if (getState(start) != color) {
            return false;
        }
        
        GridPoint p = new GridPoint(start, dir);
        CellColor chk = getState(p);

        while (numInRow > 1 && chk == color) {
            p.move(dir);
            chk = getState(p);
            numInRow--;
        }
        
        if (numInRow > 1) {
            return false;
        }
        
        GridPoint test = new GridPoint(start, dir.opposite());
        if (getState(test) != color) {
            maxCap--;
        }
        
        // p.move(dir);
        if (chk != color) {
            maxCap--;
        }
        
        return (maxCap >= 0);
        
    }
    
    public int countRowExcl(GridPoint start, Direction dir, CellColor color) {
        
        if (color == CellColor.NONE) {
            return 0;
        }
        
        int inRow = 0;
        GridPoint p = new GridPoint(start, dir);
        CellColor chk = getState(p);
        
        while (chk == color) {
            inRow++;
            p.move(dir);
            chk = getState(p);
        }
        
        return inRow;
    }
    
    public int countMaxRow(GridPoint start, Direction dir, CellColor color) {
        
        if (color == CellColor.NONE) {
            return 0;
        }
        
        CellColor wrongColor = color.opponent();
        
        int inRow = 0;
        GridPoint p = new GridPoint(start, dir);
        CellColor chk = getState(p);
        
        while (inBounds(p) && chk != wrongColor) {
            p.move(dir);
            chk = getState(p);
            inRow++;
        }
        
        return inRow;
    }

    boolean hasAdjacentPieces(GridPoint p) {
        for (int i = p.row - 1; i <= p.row + 1; i++) {
            for (int j = p.col - 1; j <= p.col + 1; j++) {
                if (i == p.row && j == p.col)
                    continue;
                if (getState(i, j) != CellColor.NONE)
                    return true;
            }
        }
        return false;
    }
}
