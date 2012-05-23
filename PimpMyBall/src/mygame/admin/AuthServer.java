/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import mygame.balls.UserData;


/**
 *
 * @author Jimmy
 */
public class AuthServer {
    
    private static Random rand = new Random();

    void setUserOnline(long userId) {
        
    }

    void setUserOffline(long userId) {
        
    }
    
    private static class ASUser {
        
        Vector3f pos = new Vector3f(0, 100, 0);
        String name;
        String password;
        long userID;
        int rank = 1000;
        boolean online = false;
        
        ASUser(String name, String password, long id) {
            this.name = name;
            this.password = password;
            this.userID = id;
            // this.rank = rand.nextInt(2000);
        }
    }
    
    static HashMap<String, AuthServer> servers = new HashMap<String, AuthServer>();
    static ASUser[] fakeUsers = new ASUser[] {
        new ASUser("alex", "pass", 0x03L),
        new ASUser("jimmy", "klass", 0x07L),
        new ASUser("nicke", "kass", 0x15L),
    };
    
    private String name;
    private HashMap<String, ASUser> usersByName = new HashMap<String, ASUser>();
    private HashMap<Long, ASUser> usersById = new HashMap<Long, ASUser>();
    
            
    private AuthServer(String name) {
        
        // Read from file
        this.name = name;
        
        for (ASUser u : fakeUsers) {
            usersByName.put(u.name, u);
            usersById.put(u.userID, u);
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
        
        ASUser u = usersByName.get(name);

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
    
    public void saveData(ArrayList<UserData> data) {
        for (UserData ud : data) {
            ASUser u = usersByName.get(ud.userName);
            u.rank = ud.rank;
        }
    }
    
    public void userLoggedOut(long id) {
        ASUser user = usersById.get(id);
        if (user != null) {
            user.online = false;
        }
    }
    
    public void userLoggedIn(long id) {
        ASUser user = usersById.get(id);
        if (user != null) {
            user.online = true;
        }
    }
    
    public boolean isLoggedIn(long id) {
        ASUser user = usersById.get(id);
        return (user == null) ? false : user.online;
    }
}
