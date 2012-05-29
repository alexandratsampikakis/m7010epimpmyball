/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import mygame.balls.UserData;

/**
 * Very temporary class that reads and writes data on the server
 * 
 * @author Jimmy
 */
public class DataManager {
    
    private DataManager() {
    }
    
    protected static void writeData(UserData data) {
        
        BufferedWriter bw = null;
        
        try {
            
            File dir = new File("users");
            dir.mkdir();
            
            File f = new File("users/" + data.userName);
            f.createNewFile();

            bw = new BufferedWriter(
                    new FileWriter(f));
            
            bw.write("id");
            bw.newLine();
            bw.write("" + data.id);
            bw.newLine();
            
            bw.write("rank");
            bw.newLine();
            bw.write("" + data.rank);
            bw.newLine();
            
            bw.write("bling");
            bw.newLine();
            bw.write("" + data.bling);
            bw.newLine();
            
            bw.close();
            
        } catch (Exception e) {
            // TODO Handle error! :)
        }
    }
    
    protected static UserData readFile(String userName) {
        
        HashMap<String, String> userData = new HashMap<String, String>();
        File file = new File("users/" + userName);
        BufferedReader br = null;
        
        try { 
            br = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(file)));
                
            String key, value;
            
            while ((key = br.readLine()) != null &&
                    (value = br.readLine()) != null) {
                userData.put(key, value);
            }
            
            br.close();
            
        } catch (Exception e) {
            return null;
        }
        
        UserData data = new UserData();
        String val;
        
        if ((val = userData.get("id")) != null) {
            data.id = Long.decode(val);
        } 
        if ((val = userData.get("rank")) != null) {
            data.rank = Integer.decode(val);
        }
        if ((val = userData.get("bling")) != null) {
            data.bling = Long.decode(val);
        }
        
        data.userName = userName;
        data.materialIndex = 0;
        
        return data;
    }
    
    
    protected static HashMap<String, String> getUserList() {
        
        HashMap<String, String> userList = new HashMap<String, String>();
        File file = new File("userlist");
        BufferedReader br = null;
        
        try {
            
            if (!file.exists()) {
                file.createNewFile();
                userList.put("id", "0");
                saveUserList(userList);
                return userList;
            }
            
            br = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(file)));
            
            String id = br.readLine();
            if (id != null) {
                userList.put("id", id);
            }
                
            String name, pass;
           
            while ((name = br.readLine()) != null &&
                    (pass = br.readLine()) != null) {
                userList.put(name, pass);
            }
            
            br.close();
            
        } catch (Exception e) {
            return null;
        }
        return userList;
    }
    
    
    protected static void saveUserList(HashMap<String, String> list) {

        try {
            
            File file = new File("userlist");
            
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(file));
            
            bw.write(list.get("id"));
            bw.newLine();
            
            for (String name : list.keySet()) {
                if (name.equals("id"))
                    continue;
                String pass = list.get(name);
                bw.write(name);
                bw.newLine();
                bw.write(pass);
                bw.newLine();
            }
            
            bw.close();
            
        } catch (Exception e) {
            // TODO Handle error! :)
        }
    }
}
