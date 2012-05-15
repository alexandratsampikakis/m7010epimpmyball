package mygame.network;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.util.concurrent.Callable;

/**
 * Creates a terrain object and a collision node to go with it. Then
 * drops several balls from the sky that collide with the terrain
 * and roll around.
 * Left click to place a sphere on the ground where the crosshairs intersect the terrain.
 * Hit keys 1 or 2 to raise/lower the terrain at that spot.
 *
 * @author
 */
public class TestClient extends SimpleApplication {

    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Material matRock;
    Material matWire;
    private int timeCounter = 0;
    private Client client;
    private BasicShadowRenderer bsr;
    private Vector3f[] points;
    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector3f();
        }
    }
    protected BitmapText hintText;
    private PointLight pl;
    private Geometry lightMdl;
    private BulletAppState bulletAppState;
    private Ball player;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private ParticleEmitter smoke;
    
    private Vector3f realPosition = Vector3f.ZERO;
    private Vector3f realVelocity = Vector3f.ZERO;

    public static void main(String[] args) {
        BallServer.initializeClasses();
        TestClient app = new TestClient();
        app.start();
        app.setPauseOnLostFocus(false);
    }

    @Override
    public void simpleInitApp() {
        try {
            client = Network.connectToServer(BallServer.NAME, BallServer.VERSION,
                    "192.168.1.6",
                    // "localhost",
                    BallServer.PORT, BallServer.UDP_PORT);
        } catch (IOException ex) {
            Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.addMessageListener(new ClientMessageListener(), BallMessage.class);
        client.start();

        //Creating a sky
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        initKeys();
        initLighting();
        initShadow();

        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        initPlayer();
        initCamera();
        setUpTerrain();
    }
    
        private class ClientMessageListener implements MessageListener<Client> {
        public void messageReceived(Client source, Message message) {
            if (message instanceof BallMessage) {
                TestClient.this.enqueue(new MyCallable((BallMessage) message));
            }
        }
    }
    
            private class MyCallable implements Callable {
            BallMessage ballMessage;
            
            public MyCallable(BallMessage ballMessage) {
                this.ballMessage = ballMessage;
            }
            
            // Extract the velocity of the user sending the message.
            public Object call() {
                realPosition = ballMessage.getPosition();
                realVelocity = ballMessage.getVelocity();
                System.out.println("Receiving velocity: " + ballMessage.getVelocity());
                return ballMessage;
            }
        }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0;   // Dessa två gör ingen skillnad.
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left) {
            walkDirection.addLocal(camDir.negate()); ////////////////sjukt fiskigt!! måste ändra detta sen!!!!
        }
        if (right) {
            walkDirection.addLocal(camDir);
        }
        if (up) {
            walkDirection.addLocal(camLeft);
        }
        if (down) {
            walkDirection.addLocal(camLeft.negate());
        }
        
        Vector3f currentPosition = player.getGeometry().getLocalTranslation();
        Vector3f newDirection = realPosition.subtract(currentPosition);
        float newDirectionAbs = newDirection.length();
        if (newDirectionAbs > 0.5f) {  
            player.getControl().setLinearVelocity(realVelocity.add(newDirection));
        }

        //For the shadow
        Camera shadowCam = bsr.getShadowCamera(); //Behövs denna?
        ShadowUtil.updateFrustumPoints2(shadowCam, points);

        // Send velocity to server on a fixed interval
        if (timeCounter > 5) {
            BallMessage ballMessage = new BallMessage(Vector3f.ZERO, walkDirection, Vector3f.ZERO);
            ballMessage.setReliable(false);
            client.send(ballMessage);
            timeCounter = 0;
        }
        timeCounter++;
    }
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean isPressed, float tpf) {
            if (binding.equals("CharLeft")) {
                if (isPressed) {
                    left = true;
                } else {
                    left = false;
                }
            } else if (binding.equals("CharRight")) {
                if (isPressed) {
                    right = true;
                } else {
                    right = false;
                }
            } else if (binding.equals("CharForward")) {
                if (isPressed) {
                    up = true;
                } else {
                    up = false;
                }
            } else if (binding.equals("CharBackward")) {
                if (isPressed) {
                    down = true;
                } else {
                    down = false;
                }
            }
        }
    };

    /**
     * 
     */
    private void initLighting() {
        // Create directional light
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-0.5f, -.5f, -.5f).normalizeLocal());
        directionalLight.setColor(new ColorRGBA(0.50f, 0.50f, 0.50f, 1.0f));
        rootNode.addLight(directionalLight);
        //Create ambient light
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor((ColorRGBA.White).mult(2.5f));
        rootNode.addLight(ambientLight);
    }

    /**
     * 
     */
    private void initCamera() {
        flyCam.setEnabled(false);
        ChaseCamera camera = new ChaseCamera(cam, player.getGeometry(), inputManager);
        camera.setDragToRotate(false);
    }

    /**
     * 
     */
    private void setUpTerrain() {
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", 128f);
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();
        } catch (Exception e) {
        }
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f)); // patch size, and a multiplier
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(2, 2, 2));
        terrain.setLocked(false); // unlock it so we can edit the height
        rootNode.attachChild(terrain);
        terrain.addControl(new RigidBodyControl(0));
        terrain.setShadowMode(ShadowMode.CastAndReceive);
        bulletAppState.getPhysicsSpace().addAll(terrain);
    }

    /**
     * 
     */
    private void initPlayer() {
        player = new Ball(assetManager);
        rootNode.attachChild(player);
        bulletAppState.getPhysicsSpace().add(player.getControl());
        Vector3f pos = new Vector3f(100f, 200f, 0f);
        player.getControl().setPhysicsLocation(pos);
    }

    private void initKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(actionListener, "CharLeft");
        inputManager.addListener(actionListener, "CharRight");
        inputManager.addListener(actionListener, "CharForward");
        inputManager.addListener(actionListener, "CharBackward");
    }

    public void initShadow() {
        bsr = new BasicShadowRenderer(assetManager, 512);
        bsr.setDirection(new Vector3f(-0.5f,-.5f,-.5f).normalizeLocal());
        viewPort.addProcessor(bsr);
    }

    /*private class SendAction extends AbstractAction {
        private boolean reliable;

        public SendAction(boolean reliable) {
            super(reliable ? "TCP" : "UDP");
            this.reliable = reliable;
        }

        public void actionPerformed(ActionEvent evt) {
            BallMessage ballMessage = new BallMessage(Vector3f.ZERO, walkDirection, Vector3f.ZERO);
            ballMessage.setReliable(reliable);
            System.out.println("Sending:" + ballMessage);
            client.send(ballMessage);
        }
    }*/
}
