/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mygame.boardgames.Direction;
import mygame.boardgames.GridPoint;

/**
 *
 * @author Jimmy
 */
public class GomokuAI {

    private GomokuGrid grid;
    private ArrayList<GridPoint> allPoints = new ArrayList<GridPoint>();
    
    public GomokuAI(GomokuGrid grid) {
        
        this.grid = grid;
        
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                allPoints.add(new GridPoint(i, j));
            }
        }
    }
    
    public void reset() {
        
        allPoints = new ArrayList<GridPoint>();
        
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                allPoints.add(new GridPoint(i, j));
            }
        }
    }

    private int countRow(GridPoint start, Direction dir, CellColor color) {

        int inRow = 0;
        GridPoint p = new GridPoint(start, dir);
        CellColor chk = grid.getState(p);
        
        while (chk == color) {
            p.move(dir);
            chk = grid.getState(p);
            inRow++;
        }
        return inRow;
    }
    
    private int getScore(GridPoint location, CellColor color) {

        int[] counts = new int[] 
        {
            countRow(location, Direction.NORTH, color)
            + countRow(location, Direction.SOUTH, color),
        
            countRow(location, Direction.WEST, color)
            + countRow(location, Direction.EAST, color),
            
            countRow(location, Direction.NORTH_WEST, color)
            + countRow(location, Direction.SOUTH_EAST, color),
            
            countRow(location, Direction.SOUTH_WEST, color)
            + countRow(location, Direction.NORTH_EAST, color),
        };

        int result = 0;
        for (int i = 0; i < counts.length; i++) {
            result = Math.max(result, counts[i]);
        }
        return result;
    }

    
    public GridPoint nextMove(CellColor color, GridPoint opponentMove) {
        
        // System.out.println("Before " + allPoints.size());
        if (opponentMove != null)
            allPoints.remove(opponentMove);
        // System.out.println("After  " + allPoints.size());
        
        GridPoint pick;
        List<GridPoint> candidates = filterCellsStageThree(color);
        
        if (candidates.isEmpty()) {
            assert(false);
            // return new GridPoint(-1, -1);
        }

        if (candidates.size() == 1) {
            pick = candidates.get(0);
        }

        Random rand = new Random();
        int index = rand.nextInt(candidates.size());
        pick = candidates.get(index);
        
        allPoints.remove(pick);
        
        return pick;
    }

    private List<GridPoint> filterCellsStageZero(CellColor color) {
        return filterCellsCore(allPoints, estimateForStageZero, color);
    }
    
    private List<GridPoint> filterCellsStageOne(CellColor color) {
        return filterCellsCore(filterCellsStageZero(color), estimateForStageOne, color);
    }

    private List<GridPoint> filterCellsStageTwo(CellColor color) {
        return filterCellsCore(filterCellsStageOne(color), estimateForStageTwo, color);
    }

    private List<GridPoint> filterCellsStageThree(CellColor color) {
        return filterCellsCore(filterCellsStageTwo(color), estimateForStageThree, color);
    }

    private List<GridPoint> filterCellsCore(
            List<GridPoint> source, Estimator estimator, CellColor color) {

        ArrayList<GridPoint> result = null;
        double bestEstimate = 0;
        
        for (GridPoint location : source) {
            
            if (grid.getState(location) != CellColor.NONE) {
                continue;
            }
            
            double estimate = estimator.estimate(location, color);

            if (result == null) {
                result = new ArrayList<GridPoint>();
                bestEstimate = estimate;
            
            } else if (estimate > bestEstimate) {
                result.clear();
                bestEstimate = estimate;
            
            } else if (estimate < bestEstimate) {
                continue;
            }
            
            result.add(location);
        }

        return result;
    }

    private interface Estimator {
        public double estimate(GridPoint p, CellColor color);
    }
    
    private Estimator estimateForStageZero = new Estimator() {

        @Override
        public double estimate(GridPoint p, CellColor color) {
            
            int toWin = grid.getNumInRowToWin();
            int selfScore = 1 + getScore(p, color);
            int opponentScore = 1 + getScore(p, color.opponent());
            
            boolean selfWin = selfScore >= toWin;
            boolean opponentWin = opponentScore >= toWin;

            return selfWin ? 2 : opponentWin ? 1 : 0;
        }
    };
    
    private Estimator estimateForStageOne = new Estimator() {

        @Override
        public double estimate(GridPoint p, CellColor color) {
            
            Direction[] dirs = {
                Direction.EAST,
                Direction.NORTH_EAST,
                Direction.NORTH,
                Direction.NORTH_WEST,
            };
            
            // R√§kna p√• hur m√•nga st√§llen det ger rader ocks√•, 
            // typ 3 i rad p√• tv√• st√§llen
            
            int selfScore, opponentScore, score = 0;
            
            for (Direction dir : dirs) {
                selfScore = (new RowData(p, dir, color)).getValue();
                opponentScore = (new RowData(p, dir, color.opponent())).getValue();
                score = Math.max(score, Math.max(selfScore, opponentScore));
            }
            
            return score;
        }
    };
    
    private Estimator estimateForStageTwo = new Estimator() {
        @Override
        public double estimate(GridPoint p, CellColor color) {

            int selfCount = 0;
            int opponentCount = 0;

            for (int i = p.row - 1; i <= p.row + 1; i++) {
                for (int j = p.col - 1; j <= p.col + 1; j++) {
                    if (grid.getState(i, j) == color) {
                        selfCount++;
                    }
                    if (grid.getState(i, j) == color.opponent()) {
                        opponentCount++;
                    }
                }
            }
            return 2 * selfCount + opponentCount;
        }
    };
    
    private Estimator estimateForStageThree = new Estimator() {
        @Override
        public double estimate(GridPoint p, CellColor color) {
            int dRow = p.row - grid.getRows() / 2;
            int dCol = p.col - grid.getCols() / 2;
            return -Math.sqrt(dRow * dRow + dCol * dCol);
        }
    };
    
    private class RowData {
        
        CellColor color;
        Direction dir;
        int inRow = 1, maxRow = 1;
        int stopCount = 0;
        
        RowData(GridPoint start, Direction dir, CellColor color) {
            
            this.dir = dir;
            
            GridPoint p = new GridPoint(start, dir);
            CellColor chk = grid.getState(p);
            CellColor wrongColor = color.opponent();
            
            while (chk == color) {
                p.move(dir);
                chk = grid.getState(p);
                inRow++;
                maxRow++;
            }
            if (!grid.inBounds(p) || chk == wrongColor) {
                stopCount++;
            } else {
                
                while (grid.inBounds(p) && chk != wrongColor) {
                    p.move(dir);
                    chk = grid.getState(p);
                    maxRow++;
                }
            }
            

            dir = dir.opposite();
            p = new GridPoint(start, dir);
            chk = grid.getState(p);

            while (chk == color) {
                p.move(dir);
                chk = grid.getState(p);
                inRow++;
                maxRow++;
            }
            if (!grid.inBounds(p) || chk == wrongColor) {
                stopCount++;
            } else {
                
                while (grid.inBounds(p) && chk != wrongColor) {
                    p.move(dir);
                    chk = grid.getState(p);
                    maxRow++;
                }
            }
        }
        
        int getValue() {
            int toWin = grid.getNumInRowToWin();
            return (maxRow < toWin || stopCount >= 2) ? 0 : inRow - stopCount;
        }
    }
    
    
    
}
