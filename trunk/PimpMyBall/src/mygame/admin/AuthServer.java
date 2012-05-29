/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import mygame.balls.UserData;


/**
 *
 * @author Jimmy
 */
public class AuthServer {
    
    private static long NEW_USER_ID;
    private static AuthServer instance;
    
    private HashMap<String, String> users = new HashMap<String, String>();
    private HashMap<Long, Boolean> userStatus = new HashMap<Long, Boolean>();
            
    protected static AuthServer getInstance() {
        if (instance == null) {
            instance = new AuthServer();
        }
        return instance;
    }
    
    private AuthServer() {
        users = DataManager.getUserList();
        NEW_USER_ID = Long.decode(users.get("id"));
    }
    
    /**
     * Very safe authentication.... :)
     * 
     * @param name
     * @param pass
     * @return 
     */
    protected UserData authenticate(String name, String pass) {
        if (users.get(name).equals(pass)) {
            return DataManager.readFile(name);
        }
        return null;
    }
    
    protected UserData createUser(String name, String pass) {
 
        // Only create a new user if the name is unique
        if (users.get(name) == null) {
 
            UserData data = new UserData();
            data.id = NEW_USER_ID++;
            data.rank = 1000;
            data.userName = name;
            data.position = new Vector3f();
            data.bling = 0;
            data.materialIndex = 0;
            
            users.put(name, pass);
            DataManager.writeData(data);
            
            users.put("id", "" + NEW_USER_ID);
            DataManager.saveUserList(users);
        
            return data;
        }
        
        return null;
    }
    
    protected void saveData(ArrayList<UserData> data) {
        
        if (data != null) {
            for (UserData ud : data) {
                DataManager.writeData(ud);
            }
        }
        
        users.put("id", "" + NEW_USER_ID);
        DataManager.saveUserList(users);
    }
    
    protected void userLoggedOut(long id) {
        userStatus.put(id, false);
    }
    
    protected void userLoggedIn(long id) {
        userStatus.put(id, true);
    }
    
    protected boolean isLoggedIn(long id) {
        Boolean status = userStatus.get(id);
        return (status == null) ? false : status;
    }
}
