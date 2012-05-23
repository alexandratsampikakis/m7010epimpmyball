package mygame.balls.server;

import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.HashSet;
import mygame.boardgames.GridPoint;

/**
 *
 * @author nicnys-8
 */
public class AreaOfInterestManager {
    // Area of interest stuff

    private int xMin, xMax, yMin, yMax;
    private int width;
    private int height;
    private int nrOfCols, nrOfRows;
    //private BiMap<Long, User> users;
    private ArrayList[][] areas;

    public AreaOfInterestManager() {
        xMin = - 512;
        xMax = 512;
        yMin = -512;
        yMax = 512;
        width = xMax - xMin;
        height = yMax - yMin;
        nrOfCols = 20;
        nrOfRows = 20;
        resetAllAreas();
    }

    /*public final void updateEntireBoard() {
    
    // empty the matrix
    emptyMatrix();
    // sort all users
    for (User user : users.getValues()) {
    int row = getRow(user);
    int col = getCol(user);
    areas[row][col].add(user.getConnection());
    }
    }*/
    public void setAOIMidpoint(User user) {

        GridPoint oldArea = user.getCurrentArea();
        GridPoint newArea = new GridPoint(getRow(user), getCol(user));

        HostedConnection connection = user.getConnection();

        if (oldArea == null) {
            user.setCurrentArea(newArea);
            areas[newArea.row][newArea.col].add(connection);
            
        } else if (!oldArea.equals(newArea)) {
            user.setCurrentArea(newArea);
            areas[oldArea.row][oldArea.col].remove(connection);
            areas[newArea.row][newArea.col].add(connection);
            System.out.println(
                    user.getUserData().userName
                    + " just moved from ("
                    + oldArea.row
                    + ","
                    + oldArea.col
                    + ") to ("
                    + newArea.row
                    + ","
                    + newArea.col
                    + ").");
        }
    }

    public HashSet<HostedConnection> getInterestedConnections(User user) {

        HashSet<HostedConnection> filter = new HashSet<HostedConnection>();

        int row = getRow(user);
        int col = getCol(user);

        int startRow = Math.max(0, row - 1);
        int endRow = Math.min(nrOfRows - 1, row + 1);

        int startCol = Math.max(0, col - 1);
        int endCol = Math.min(nrOfCols - 1, col + 1);

        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                filter.addAll(areas[i][j]);
            }
        }
        return filter;
    }

    /**
     * Empties all areas
     */
    public void resetAllAreas() {
        areas = new ArrayList[nrOfRows][nrOfCols];
        for (int i = 0; i < nrOfCols; i++) {
            for (int j = 0; j < nrOfRows; j++) {
                areas[i][j] = new ArrayList<User>();
            }
        }
    }

    /**
     * Returns the column of the game world that the user is currently in
     * @param user
     * @return 
     */
    public int getCol(User user) {
        float x = user.getBall().getPosition().x;
        x -= xMin;
        int col = (int) ((x / width) * nrOfCols);
        col = Math.max(0, col);
        return Math.min(col, nrOfCols - 1);
    }

    public int getRow(User user) {
        float y = user.getBall().getPosition().y;
        y -= yMin;
        int row = (int) ((y / height) * nrOfRows);
        row = Math.max(0, row);
        return Math.min(row, nrOfRows - 1);
    }
}
