/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.network.HostedConnection;

/**
 *
 * @author nicnys-8
 */
public class User {

    private Ball ball;
    private HostedConnection conn;

    public User(Ball ball, HostedConnection conn) {
        this.ball = ball;
        this.conn = conn;
    }

    public Ball getBall() {
        return ball;
    }

    public HostedConnection getHostedConnection() {
        return conn;
    }
}