/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.server;

import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import mygame.balls.Ball;
import mygame.util.BiMap;

/**
 *
 * @author nicnys-8
 */
public class AreaOfInterestManager {
    // Area of interest stuff

    private int xMin, xMax, yMin, yMax;
    private int nrOfCols, nrOfRows;
    private BiMap<Long, User> users;

    public AreaOfInterestManager(BiMap<Long, User> userList) {
        this.users = userList;
        xMin = - 510;
        xMax = 510;
        yMin = -510;
        yMax = 510;
        nrOfCols = 20;
        nrOfRows = 20;
    }

    public HashSet<HostedConnection> getAreaOfInterest(User user) {
            HashSet<HostedConnection> filter = new HashSet<HostedConnection>();
        for (User u : users.getValues()) {
            filter.add(u.getConnection());
        }
        return filter;
    }
    
    public void updateAreasOfInterest() {
    }
}
