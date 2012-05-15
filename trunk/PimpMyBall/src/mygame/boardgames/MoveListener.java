/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boardgames;

import boardgames.gomoku.CellColor;
import boardgames.gomoku.WinningRow;

/**
 *
 * @author Jimmy
 */
public interface MoveListener {
    public void onMove(CellColor color, GridPoint p);
    public void onWin(WinningRow wr);
}
