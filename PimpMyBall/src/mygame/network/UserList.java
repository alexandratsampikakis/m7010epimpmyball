/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import java.util.ArrayList;

/**
 *
 * @author nicnys-8
 */
public class UserList extends ArrayList<User> {
    
    User getUserWithId(long id) {
        for (User user : this) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
}
