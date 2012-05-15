/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.WinningRow;

/**
 *
 * @author Jimmy
 */
public interface MoveListener {
    public void onMove(CellColor color, GridPoint p);
    public void onWin(WinningRow wr);
}
