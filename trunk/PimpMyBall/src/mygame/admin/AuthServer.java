/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.Random;
import mygame.balls.UserData;


/**
 *
 * @author Jimmy
 */
public class AuthServer {
    
    private static Random rand = new Random();
    
    private static class ASUser {
        
        Vector3f pos = new Vector3f(0, 100, 0);
        String name;
        String password;
        long userID;
        int rank;
        boolean active = false;
        
        ASUser(String name, String password, long id) {
            this.name = name;
            this.password = password;
            this.userID = id;
            this.rank = rand.nextInt(2000);
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
    public UserData authenticate(String name, String pass) {
        
        ASUser u = users.get(name);

        if (u != null && u.password.equals(pass)) {
            UserData data = new UserData();
            data.id = u.userID;
            data.rank = u.rank;
            data.userName = u.name;
            data.position = u.pos;
            data.bling = 0;
            data.materialIndex = 0;
            return data;
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