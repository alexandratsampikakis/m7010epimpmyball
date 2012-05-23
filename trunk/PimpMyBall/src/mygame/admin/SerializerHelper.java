/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.serializing.Serializer;
import mygame.balls.UserData;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.BallUpdateMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.RequestUsersMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.boardgames.GridPoint;
import mygame.boardgames.GridSize;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.network.broadcast.GomokuEndMessage;
import mygame.boardgames.network.GomokuMessage;
import mygame.boardgames.network.broadcast.GomokuStartMessage;
import mygame.boardgames.network.NewGameMessage;
import mygame.boardgames.network.broadcast.GomokuUpdateMessage;

/**
 *
 * @author Jimmy
 */
public class SerializerHelper {
 
    // public static void initializeClientClasses() {}
    // public static void initializeCentralServerClasses() {}
    // public static void initializeBallServerClasses() {}
    
    public static void initializeClasses() {
        
        // Login messages
        Serializer.registerClass(LoginMessage.class);
        Serializer.registerClass(LoginFailedMessage.class);
        Serializer.registerClass(LoginSuccessMessage.class);
        
        // Ball server messages
        Serializer.registerClass(GameServerStartedMessage.class);
        Serializer.registerClass(BackupDataMessage.class);
        Serializer.registerClass(IncomingBallMessage.class);
        Serializer.registerClass(BallAcceptedMessage.class);
        Serializer.registerClass(BallRejectedMessage.class);
        
        Serializer.registerClass(UserData.class);
        Serializer.registerClass(ServerInfo.class);
        
        // Ball messages
        Serializer.registerClass(HelloMessage.class);
        Serializer.registerClass(BallUpdateMessage.class);
        Serializer.registerClass(BallDirectionMessage.class);
        Serializer.registerClass(ConnectedUsersMessage.class);
        Serializer.registerClass(RequestUsersMessage.class);
        Serializer.registerClass(UserAddedMessage.class);
        
        // Gomoku messages
        Serializer.registerClass(GridPoint.class);
        Serializer.registerClass(GridSize.class);
        Serializer.registerClass(GomokuMessage.class);
        Serializer.registerClass(NewGameMessage.class);
        
        Serializer.registerClass(GomokuStartMessage.class);
        Serializer.registerClass(GomokuEndMessage.class);
        Serializer.registerClass(GomokuUpdateMessage.class);
        
        Serializer.registerClass(ChatMessage.class);
    }
    
}
