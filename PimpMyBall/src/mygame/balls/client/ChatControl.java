/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.RenderManager;
import java.util.concurrent.Callable;
import mygame.admin.ChatMessage;
import mygame.util.CleanRawInputListener;

/**
 *
 * @author Jimmy
 */
public class ChatControl extends AbstractAppState {
    
    private Client client;
    private SimpleApplication app;
    private ChatDelegate delegate;
    private BitmapFont guiFont;
    
    private BitmapText chatTextField;
    private String chatString = "";
    private boolean isEnteringChat = false;
    
    public ChatControl(ChatDelegate delegate, Client client) {
        
        this.client = client;
        this.delegate = delegate;
        
        client.addMessageListener(new MessageListener<Client>() {
            public void messageReceived(Client source, Message m) {
                ChatControl.this.app.enqueue(new MyCallable((ChatMessage)m));
            }
        }, ChatMessage.class);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        app.getInputManager().addRawInputListener(rawInputListener);
        
        guiFont = app.getAssetManager().loadFont("Interface/Fonts/HelveticaNeue.fnt");
        chatTextField = new BitmapText(guiFont, false);

        chatTextField.setSize(guiFont.getCharSet().getRenderedSize());
        chatTextField.setText("");
        chatTextField.setColor(ColorRGBA.White);
        chatTextField.setLocalTranslation(new Vector3f(16, 40, 0f));

        this.app = (SimpleApplication) app;
        this.app.getGuiNode().attachChild(chatTextField);
    
        System.out.println("Initialized " + this);
    }
   
    @Override
    public void stateAttached(AppStateManager stateManager) {
        super.stateAttached(stateManager);
        System.out.println("Attached " + this + ".");
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        System.out.println("Detached " + this + ".");
    }

    @Override
    public void update(float tpf) {}
    
    @Override
    public void render(RenderManager rm) {}
    
    private CleanRawInputListener rawInputListener = new CleanRawInputListener() {
        @Override
        public void onKeyEvent(KeyInputEvent evt) {
            
            if (!evt.isPressed()) {
                return;

            } else if (!isEnteringChat && evt.getKeyCode() == KeyInput.KEY_RETURN) {
                isEnteringChat = true;
                chatTextField.setText(((isEnteringChat) ? "Enter message: _" : ""));
                evt.setConsumed();

            } else if (isEnteringChat) {

                switch (evt.getKeyCode()) {
                    case KeyInput.KEY_ESCAPE:
                        isEnteringChat = false;
                        chatTextField.setText("");
                        evt.setConsumed();
                        return;

                    case KeyInput.KEY_RETURN:
                        if (chatString.equals("")) {
                            isEnteringChat = false;
                            chatTextField.setText("");
                            evt.setConsumed();
                            return;
                        } else {
                            client.send(new ChatMessage(chatString, delegate.getChatterId()));
                            chatString = "";
                        }
                        break;

                    case KeyInput.KEY_BACK:
                        if (chatString.length() > 0) {
                            chatString = chatString.substring(0, chatString.length() - 1);
                        }
                        break;

                    default:
                        char c = evt.getKeyChar();
                        chatString += c;
                        break;
                }
                chatTextField.setText("Enter message: " + chatString + "_");
                evt.setConsumed();
            }
        }
    };

    
    private class MyCallable implements Callable {
        
        private ChatMessage msg;
        
        public MyCallable(ChatMessage msg) {
            this.msg = msg;
        }
        
        @Override
        public Object call() throws Exception {
            delegate.onIncomingMessage(msg.getText(), msg.getSenderId());
            return msg;
        }
    }
}
