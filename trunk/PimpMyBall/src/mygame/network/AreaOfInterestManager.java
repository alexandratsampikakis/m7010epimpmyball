/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;

/**
 *
 * @author nicnys-8
 */
public class AreaOfInterestManager {
    // Area of interest stuff

    private int xMin, xMax, yMin, yMax;
    private int nrOfCols, nrOfRows;
    private ArrayList<ServerSideUser>[][] userAreas =
            (ArrayList<ServerSideUser>[][]) new ArrayList[nrOfCols][nrOfRows];

    public AreaOfInterestManager() {
        xMin = - 510;
        xMax = 510;
        yMin = -510;
        yMax = 510;
        nrOfCols = 20;
        nrOfRows = 20;

    }

    public ArrayList<HostedConnection> getAreaOfInterest(ServerSideUser user) {
        ArrayList<HostedConnection> filter = new ArrayList<HostedConnection>();
        /*User ball = user.getBall();
        Vector3f pos = ball.getGeometry().getWorldTranslation();*/

        return filter;
    }

    public void updateAreasOfInterest() {
        for (int i = 0; i < nrOfRows; i++) {
            for (int j = 0; j < nrOfCols; j++) {
                for (ServerSideUser user : userAreas[i][j]) {
                    update(user);
                }
            }
        }
    }

    private void update(ServerSideUser user) {
        /*User ball = user.getBall();
        Vector3f pos = ball.getGeometry().getWorldTranslation();*/
    }
}
