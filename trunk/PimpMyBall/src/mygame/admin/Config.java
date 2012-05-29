/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Jimmy
 */
public class Config {
    
    private static ServerInfo info = null;
    
    public static ServerInfo getCentralServerInfo() {
        
        if (info == null) {
            try {

                File file = new File("server.conf");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                            new FileInputStream(file)));

                String name = br.readLine();
                String ip = br.readLine();
                String port = br.readLine();

                br.close();

                info = new ServerInfo(name, ip, Integer.decode(port));

            } catch (Exception e) {
                info = new ServerInfo("Central Server", "localhost", 5111);
            }
        }
        
        return info;
    }
}
