/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import mygame.admin.messages.GameServerStartedMessage;
import mygame.admin.messages.LoginSuccessMessage;
import mygame.admin.messages.LogoutMessage;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginFailedMessage;
import mygame.admin.messages.IncomingBallMessage;
import mygame.admin.messages.UserLeftServerMessage;
import mygame.admin.messages.UserEnteredServerMessage;
import mygame.admin.messages.BackupDataMessage;
import mygame.admin.messages.BallAcceptedMessage;
import mygame.admin.messages.BallRejectedMessage;
import com.jme3.network.serializing.Serializer;
import mygame.admin.messages.RegisterUserMessage;
import mygame.balls.BallUpdate;
import mygame.balls.UserData;
import mygame.balls.messages.AggregateBallUpdatesMessage;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.BallUpdateMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.RequestUsersMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.util.GridPoint;
import mygame.util.GridSize;
import mygame.boardgames.network.GomokuEndMessage;
import mygame.boardgames.network.AbstractGomokuMessage;
import mygame.boardgames.network.GomokuDrawMessage;
import mygame.boardgames.network.GomokuStartMessage;
import mygame.boardgames.network.GomokuUpdateMessage;

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
        Serializer.registerClass(RegisterUserMessage.class);
        
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
        Serializer.registerClass(AggregateBallUpdatesMessage.class);
        Serializer.registerClass(BallDirectionMessage.class);
        Serializer.registerClass(ConnectedUsersMessage.class);
        Serializer.registerClass(RequestUsersMessage.class);
        Serializer.registerClass(UserAddedMessage.class);
        
        // Gomoku messages
        Serializer.registerClass(GridPoint.class);
        Serializer.registerClass(GridSize.class);
        Serializer.registerClass(AbstractGomokuMessage.class); 
        Serializer.registerClass(GomokuStartMessage.class);
        Serializer.registerClass(GomokuEndMessage.class);
        Serializer.registerClass(GomokuUpdateMessage.class);
        Serializer.registerClass(GomokuDrawMessage.class);
        
        Serializer.registerClass(ChatMessage.class);
        
        // Other stuff
        Serializer.registerClass(BallUpdate.class);
        
        Serializer.registerClass(LogoutMessage.class);
        Serializer.registerClass(UserEnteredServerMessage.class);
        Serializer.registerClass(UserLeftServerMessage.class);
    }
    
}
