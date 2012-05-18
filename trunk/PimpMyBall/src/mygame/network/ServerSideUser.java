/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.asset.AssetManager;
import com.jme3.network.HostedConnection;

/**
 *
 * @author nicnys-8
 */
public class ServerSideUser extends User {
    private HostedConnection conn;

    public ServerSideUser(AssetManager assetManager, long id, HostedConnection conn) {
        super(assetManager, id);
        this.conn = conn;
    }

    public HostedConnection getHostedConnection() {
        return conn;
    }
}