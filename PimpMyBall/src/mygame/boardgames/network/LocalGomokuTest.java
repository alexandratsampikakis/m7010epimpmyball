/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.util.Random;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;

import mygame.boardgames.Select3D;
import mygame.boardgames.gomoku.GomokuBoard3D;
import mygame.boardgames.gomoku.player.AIPlayer;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.gomoku.player.LocalPlayer;


/**
 *
 * @author Jimmy
 */
public class LocalGomokuTest extends SimpleApplication implements ActionListener {

    private static boolean USE_CURSOR = false;
    private static boolean USE_AI_OPPONENT = true;
            
    private GomokuBoard3D board = null;
    private GomokuGame game;
    private GomokuPlayer turn;
    private Random rand = new Random();
    
    public static void main(String[] args) throws Exception {
        LocalGomokuTest app = new LocalGomokuTest();
        app.start();
    }
    
    private void startNewGame() {
        
        // Create a new game as specified by the message
        game = new GomokuGame();
        
        // Kraschar med två AI-spelare!! :( 
        // + risk för stack overflow...
        GomokuPlayer p1 = new LocalPlayer(); // new AIPlayer();
        GomokuPlayer p2 = (USE_AI_OPPONENT) ? new AIPlayer() : new LocalPlayer();
        
        if (rand.nextBoolean()) {
            turn = p1;
            game.setPlayers(p1, p2);
        } else {
            turn = p2;
            game.setPlayers(p2, p1);
        }
        
        if (USE_AI_OPPONENT) {
            // AI opponent plays instantly..
            turn = p1;
        }
    
        if (board != null)
            rootNode.detachChild(board);
        
        // Create a 3D model of the board
        board = new GomokuBoard3D(assetManager, game);
        board.setLocalScale(0.5f);
        rootNode.attachChild(board);
        
        // Start the game
        game.start();
    }

    @Override
    public void simpleInitApp() {

        // Mouse
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("restart", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "click", "restart");
        
        initControls();
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.7f));
        rootNode.addLight(al);
        
        startNewGame();
    }

    @Override
    public void simpleUpdate(float tpf) {}
    @Override
    public void simpleRender(RenderManager rm) {}

    public void onAction(String name, boolean isPressed, float tpf) {

        if (!isPressed) { // key was released
            if (name.equals("click")) {
                // Find the position on the board that was clicked
                CollisionResult closest;

                if (USE_CURSOR) {
                    Vector2f click2d = inputManager.getCursorPosition();
                    closest = Select3D.select(click2d, cam, board);
                } else {
                    closest = Select3D.select(cam, board);
                }

                if (closest != null) {
                    
                    GridPoint p = closest.getGeometry().getUserData("pos");
                    
                    if (p != null && game.tryMove(turn, p)) {
                        if (!USE_AI_OPPONENT)
                            turn = turn.getOpponent();
                    }
                }
                
            } else if (name.equals("restart")) {
                startNewGame();
            }
        }
    }
    
    /** Create crosshairs or cursor */
    private void initControls() {
        
        if (USE_CURSOR) {
            
            flyCam.setEnabled(false);
            
            Texture tex = assetManager.loadTexture("Interface/Logo/Cursor.png");
            cursor = new Picture("cursor");
            cursor.setTexture(assetManager, (Texture2D) tex, true);
            cursor.setWidth(64);
            cursor.setHeight(64);
            guiNode.attachChild(cursor);

            inputManager.addRawInputListener(inputListener);
            inputManager.setCursorVisible(false);
            
        } else {
            
            flyCam.setMoveSpeed(0f);
            
            guiNode.detachAllChildren();
            guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText ch = new BitmapText(guiFont, false);
            int size = guiFont.getCharSet().getRenderedSize();
            ch.setSize(size * 2);
            ch.setText("+");        // fake crosshairs :)
            ch.setLocalTranslation( // center
                    settings.getWidth() / 2 - size / 3 * 2,
                    settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
            guiNode.attachChild(ch);
        }
    }
    
    private Picture cursor;
    private RawInputListener inputListener = new RawInputListener() {

        private float x = 0, y = 0;

        public void beginInput() {}
        public void endInput() {}
        public void onJoyAxisEvent(JoyAxisEvent evt) {}
        public void onJoyButtonEvent(JoyButtonEvent evt) {}
        public void onMouseMotionEvent(MouseMotionEvent evt) {
            
            x = evt.getX(); // += evt.getDX();
            y = evt.getY(); // += evt.getDY();

            // Prevent mouse from leaving screen
            AppSettings settings = LocalGomokuTest.this.settings;
            x = FastMath.clamp(x, 0, settings.getWidth());
            y = FastMath.clamp(y, 0, settings.getHeight());
            
            // adjust for hotspot
            cursor.setPosition(x, y - 64);
        }
        public void onMouseButtonEvent(MouseButtonEvent evt) {}
        public void onKeyEvent(KeyInputEvent evt) {}
        public void onTouchEvent(TouchEvent evt) {}
    };
}

