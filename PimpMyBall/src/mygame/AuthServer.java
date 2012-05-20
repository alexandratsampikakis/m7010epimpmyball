/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;
import java.util.HashMap;
import mygame.network.User;


/**
 *
 * @author Jimmy
 */
public class AuthServer {
    
    private static class ASUser {
        
        Vector3f pos;
        String name;
        String password;
        long userID;
        int rank = 1000;
        boolean active = false;
        
        ASUser(String name, String password, long id) {
            this.name = name;
            this.password = password;
            this.userID = id;
        }
    }
    
    static HashMap<String, AuthServer> servers = new HashMap<String, AuthServer>();
    static ASUser[] fakeUsers = new ASUser[] {
        new ASUser("alex", "pass", 0x03L),
        new ASUser("jimmy", "klass", 0x07L),
        new ASUser("nicke", "kass", 0x15L),
    };
    
    private String name;
    private HashMap<String, ASUser> users = new HashMap<String, ASUser>();
    
    private AuthServer(String name) {
        
        // Read from file
        this.name = name;
        
        for (ASUser u : fakeUsers) {
            users.put(u.name, u);
        }
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Very safe authentication.... :)
     * 
     * @param name
     * @param pass
     * @return 
     */
    public User authenticate(String name, String pass) {
        
        ASUser u = users.get(name);
        
        if (u != null && u.password.equals(pass)) {
            // Create and return user
        }
        
        return null;
    }
    
    public static AuthServer createServer(String name) {
        
        AuthServer serv = servers.get(name);
        
        if (serv == null) {
            serv = new AuthServer(name);
            servers.put(name, serv);
        }
        
        return serv;
    }
    
    public static AuthServer getServer(String name) {
        return servers.get(name);
    }
    
}
