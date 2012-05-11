package mygame.network;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
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
public class BallServer extends SimpleApplication {

    public static void initializeClasses() {
        // Doing it here means that the client code only needs to
        // call our initialize. 
        Serializer.registerClass(BallMessage.class);
    }
    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Material matRock;
    Material matWire;
    boolean wireframe = false;
    protected BitmapText hintText;
    private Geometry lightMdl;
    private BulletAppState bulletAppState;
    private Geometry collisionSphere;
    private Geometry collisionBox;
    private Geometry selectedCollisionObject;
    private Node playerNode;
    private Geometry playerGeometry;
    private RigidBodyControl playerControl;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private static Server server;
    public static final String NAME = "Test Ball Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;
    private static int timeCounter = 0;
    private static ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private static ArrayList<HostedConnection> evenHosts = new ArrayList<HostedConnection>();

    public static void main(String[] args) throws Exception {
        initializeClasses();
        BallServer app = new BallServer();
        app.start();

        // Use this to test the client/server name version check
        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        server.start();

        MessageListener messageListener = new MessageListener<HostedConnection>() {

            public void messageReceived(HostedConnection source, Message m) {
                if (m instanceof BallMessage) {
                    // Keep track of the name just in case we 
                    // want to know it for some other reason later and it's
                    // a good example of session data
                    //source.setAttribute("name", ((BallMessage) m).getName());
                    System.out.println("Broadcasting:" + m + "  reliable:" + m.isReliable());
                    // Just rebroadcast... the reliable flag will stay the
                    // same so if it came in on UDP it will go out on that too
                    source.getServer().broadcast(m);
                } else {
                    System.err.println("Received odd message:" + m);
                }
            }
        };

        server.addMessageListener(messageListener);

        ConnectionListener connectionListener = new ConnectionListener() {

            public void connectionAdded(Server server, HostedConnection conn) {
                hostedConnections.add(conn);
                if (conn.getId() % 2 == 0) { // Just a random test to use when filtering...
                    evenHosts.add(conn);
                }
            }

            public void connectionRemoved(Server server, HostedConnection conn) {
                hostedConnections.remove(conn);
            }
        };

        server.addConnectionListener(connectionListener);
        // Keep running basically forever

        synchronized (NAME) {
            NAME.wait();
        }
    }

    @Override
    public void simpleInitApp() {
        //Creating a sky
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        //Play sound
        AudioNode backgroundMusic = new AudioNode(assetManager, "Sounds/gameMusic.wav", true);
        backgroundMusic.setVolume(0);
        backgroundMusic.play();

        initKeys();
        initLighting();

        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        initPlayer();
        initCamera();
        setUpTerrain();
    }

    @Override
    public void update() {
        super.update();
        broadcastData();
    }

    private void broadcastData() {
        if (timeCounter > 100) {
            timeCounter = 0;
            Vector3f pos = playerNode.getLocalTranslation();
            Vector3f acc = Vector3f.ZERO;
            BallMessage ballMessage = new BallMessage(pos, walkDirection, acc);
            ballMessage.setReliable(false);
            server.broadcast(Filters.in(evenHosts), ballMessage);
            System.out.println("Bredkast!");
        }
        timeCounter++;
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        playerControl.setAngularVelocity(walkDirection.mult(10f));
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

    private void testCollision(Vector3f oldLoc) {
        if (terrain.collideWith(selectedCollisionObject.getWorldBound(), new CollisionResults()) > 0) {
            selectedCollisionObject.setLocalTranslation(oldLoc);
        }
    }

    private void initLighting() {
        // Create directional light
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-0.08f, -0.4f, -0.9f).normalizeLocal());
        directionalLight.setColor(new ColorRGBA(0.50f, 0.50f, 0.50f, 1.0f));
        rootNode.addLight(directionalLight);
        //Create ambient light
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor((ColorRGBA.White).mult(2.5f));
        rootNode.addLight(ambientLight);
    }

    private void initCamera() {
        flyCam.setEnabled(false);
        ChaseCamera camera = new ChaseCamera(cam, playerNode, inputManager);
        camera.setDragToRotate(false);

        // Make the camera follow the avatar.
        //this.cam.setLocation( playerGeometry.localToWorld( new Vector3f( 0, 0 /* units above car*/, 20 /* units behind car*/ ), null));
        //this.cam.lookAt(this.playerGeometry.getWorldTranslation(), Vector3f.UNIT_Y);
    }

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
        bulletAppState.getPhysicsSpace().addAll(terrain);
    }

    private void initPlayer() {
        float radius = 2;
        playerNode = new Node("Player");
        playerGeometry = new Geometry("PlayerGeometry", new Sphere(100, 100, radius));
        rootNode.attachChild(playerNode);
        playerNode.attachChild(playerGeometry);
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        playerGeometry.setMaterial(material);
        playerNode.setLocalTranslation(new Vector3f(0, 100, 0));
        SphereCollisionShape sphereShape = new SphereCollisionShape(radius);
        float stepHeight = 500f;
        playerControl = new RigidBodyControl(sphereShape, stepHeight);
        playerNode.addControl(playerControl);
        //playerControl.setRestitution(0.001f);
        playerControl.setFriction(12f);
        bulletAppState.getPhysicsSpace().add(playerControl);
    }

    private void initKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(actionListener, "CharLeft", "CharRight");
        inputManager.addListener(actionListener, "CharForward", "CharBackward");
    }
}
