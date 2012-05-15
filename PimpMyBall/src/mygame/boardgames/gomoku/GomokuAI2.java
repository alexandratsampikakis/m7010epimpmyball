/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import mygame.boardgames.GridPoint;

/**
 *
 * @author Jimmy
 */
public class GomokuAI2 {

    private boolean alphaBetaPruningEnabled = true;
    private int searchDepth = 2;
    private int numPositionsEvaluated;
    private GomokuGrid grid;
    private CellColor computerPiece, playerPiece;
    private static final int MAXWIN = 10000;
    private static final int MINWIN = -10000;

    public GomokuAI2(GomokuGrid grid, CellColor piece) {
        this.grid = grid;
        this.computerPiece = piece;
        this.playerPiece = piece.opponent();
        numPositionsEvaluated = 0;
    }

    public CellColor getPiece() {
        return computerPiece;
    }

    public GridPoint nextMove() {
        
        if (grid.isEmpty()) {
            return new GridPoint(grid.getRows() / 2, grid.getCols() / 2);
        }
        
        GridPoint computerMove = new GridPoint(-1, -1);

        // Do a computer makeMove.
        int alpha = MINWIN - 1;
        int beta = MAXWIN + 1;
        numPositionsEvaluated = 0;
        move(0, searchDepth, grid, computerMove, computerPiece, alpha, beta);

        return computerMove;
    }

    private int move(int curdepth, int maxdepth, GomokuGrid board,
            GridPoint out, CellColor turn, int alpha, int beta) {

        if (curdepth == maxdepth) {
            numPositionsEvaluated++;
            return eval(board, computerPiece);
        }

        int max = MINWIN - 1;
        int min = MAXWIN + 1;

        GridPoint potentialPos = new GridPoint(-1, -1), 
                p = new GridPoint(-1, -1);

        int moveVal;

        while (getNextPossibleMove(p, board)) {

            GomokuGrid g = new GomokuGrid(board);
            g.setState(p, turn);
            CellColor winner = g.getWinner();
            
            if (winner == playerPiece) {
                moveVal = MINWIN;
            } else if (winner == computerPiece) {
                moveVal = MAXWIN;
            } else {
                moveVal = move(curdepth + 1, maxdepth, g, potentialPos,
                                turn.opponent(), alpha, beta);
            }
       
            if (turn == computerPiece) {
                if (moveVal > max) {
                    out.set(p);
                    max = moveVal;
                }
                if (alphaBetaPruningEnabled) {
                    alpha = alpha > moveVal ? alpha : moveVal;
                    if (alpha >= beta) {
                        return beta;
                    }
                }
                
            } else {
                
                if (moveVal < min) {
                    out.set(p);
                    min = moveVal;
                }
                if (alphaBetaPruningEnabled) {
                    beta = beta < moveVal ? beta : moveVal;
                    if (beta <= alpha) {
                        return alpha;
                    }
                }
            }
        }

        if (alphaBetaPruningEnabled) {
            return (turn == computerPiece) ? alpha : beta;
        }

        return (turn == computerPiece) ? max : min;
    }

    private static boolean getNextPossibleMove(GridPoint p, GomokuGrid g) {

        GridPoint pn = new GridPoint(p);
        
        if (pn.row == -1 || pn.col == -1) {
            pn.set(0, 0);
        } else {
            pn.col++;
            if (pn.col == g.getCols()) {
                pn.row++;
                pn.col = 0;
            }
        }
        
        while (pn.row < g.getRows()) {
            while (pn.col < g.getCols()) {
                if (g.getState(pn) == CellColor.NONE 
                        && g.hasAdjacentPieces(pn)) {
                    p.set(pn);
                    return true;
                }
                pn.col++;
            }
            pn.col = 0;
            pn.row++;
        }

        return false;
    }

    public void startNewGame() {
        numPositionsEvaluated = 0;
    }

    void setSearchDepth(final int newDepth) {
        searchDepth = newDepth;
    }

    int getSearchDepth() {
        return searchDepth;
    }

    void setAlphaBetaPruningEnabled(final boolean abOn) {
        alphaBetaPruningEnabled = abOn;
    }

    int getNumPositionsEvaluated() {
        return numPositionsEvaluated;
    }

    static final class Stats {

        Stats(GomokuGrid grid, CellColor color) {
            uncapped2 = grid.getInARowsCRAP(color, 2, 0);
            capped2 = grid.getInARowsCRAP(color, 2, 1);

            uncapped3 = grid.getInARowsCRAP(color, 3, 0);
            capped3 = grid.getInARowsCRAP(color, 3, 1);

            uncapped4 = grid.getInARowsCRAP(color, 4, 0);
            capped4 = grid.getInARowsCRAP(color, 4, 1);
        }
        final int capped2;
        final int uncapped2;
        final int capped3;
        final int uncapped3;
        final int capped4;
        final int uncapped4;
    }

    private static int eval(GomokuGrid grid, CellColor color) {

        final Stats c = new Stats(grid, color);
        final Stats u = new Stats(grid, color.opponent());

        int retVal = 0;

        if (u.uncapped4 > 0) {
            return MINWIN;
        }
        if (c.uncapped4 > 0) {
            return MAXWIN;
        }

        retVal += c.capped2 * 5;
        retVal -= u.capped2 * 5;

        retVal += c.uncapped2 * 10;
        retVal -= u.uncapped2 * 10;

        retVal += c.capped3 * 20;
        retVal -= u.capped3 * 30;

        retVal += c.uncapped3 * 100;
        retVal -= u.uncapped3 * 120;

        retVal += c.capped4 * 500;
        retVal -= u.capped4 * 500;

        return Math.max(MINWIN, Math.min(MAXWIN, retVal));
    }
}
