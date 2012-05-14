package mygame.network;

import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
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
    private static Server server;
    public static final String NAME = "Test Ball Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;
    private static int timeCounter = 0;
    //private static ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private static ArrayList<User> users = new ArrayList<User>();

    public static void main(String[] args) throws Exception {
        initializeClasses();
        BallServer app = new BallServer();
        app.start();
        app.setPauseOnLostFocus(false);

        // Use this to test the client/server name version check
        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        server.start();

        synchronized (NAME) {
            NAME.wait();
        }
    }

    @Override
    public void simpleInitApp() {

        MessageListener messageListener = new MessageListener<HostedConnection>() {

            public void messageReceived(HostedConnection conn, Message message) {
                if (message instanceof BallMessage) {
                    int id = conn.getId();
                    BallServer.this.enqueue(new MyCallable((BallMessage) message, conn));
                } else {
                    System.err.println("Received odd message:" + message);
                }
            }
        };


        server.addMessageListener(messageListener);


        ConnectionListener connectionListener = new ConnectionListener() {

            public void connectionAdded(Server server, HostedConnection conn) {
                Ball ball = new Ball(assetManager);
                rootNode.attachChild(ball);
                int id = conn.getId();
                users.add(new User(ball, conn));
                bulletAppState.getPhysicsSpace().add(ball.getControl());
                /*/OSPARVÄRT//////////*/ Vector3f pos = new Vector3f(100f, 200f, 0f);
                /*/OSPARVÄRT//////////*/ ball.getControl().setPhysicsLocation(pos);
                /*/OSPARVÄRT//////////*/ ChaseCamera camera = new ChaseCamera(cam, ball.getGeometry(), inputManager);
                /*/OSPARVÄRT//////////*/ camera.setUpVector(Vector3f.UNIT_Y);
            }

            public void connectionRemoved(Server server, HostedConnection conn) {
                for (User user : users) {
                    if (conn == user.getHostedConnection()) {
                        users.remove(user);
                    }
                }
            }
        };

        server.addConnectionListener(connectionListener);
        // Keep running basically forever

        //Creating a sky
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        setUpTerrain();
        initCamera();
        initLighting();

    }

    @Override
    public void update() {
        super.update();
        if (timeCounter > 5) {
            timeCounter = 0;
            broadcastData();
        }
        timeCounter++;

    }

    private void broadcastData() {
        Vector3f pos = Vector3f.ZERO;
        Vector3f vel = Vector3f.ZERO;
        Vector3f acc = Vector3f.ZERO;
        for (User user : users) {
            HostedConnection host = user.getHostedConnection();
            Ball ball = user.getBall();
            pos = ball.getControl().getPhysicsLocation();
            vel = ball.getControl().getLinearVelocity();
            BallMessage ballMessage = new BallMessage(pos, vel, Vector3f.ZERO);
            ballMessage.setReliable(false);
            server.broadcast(ballMessage);
        }
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    private class MyCallable implements Callable {

        BallMessage ballMessage;
        HostedConnection conn;

        public MyCallable(BallMessage ballMessage, HostedConnection conn) {
            this.ballMessage = ballMessage;
            this.conn = conn;
        }

        // Set the velocity of the player
        public Object call() {
            for (User user : users) {
                //Det här ska ju göras snyggare sen...
                if (user.getHostedConnection() == conn) {
                    RigidBodyControl control = user.getBall().getControl();
                    Vector3f newSpeed = ballMessage.getVelocity().mult(10f);
                    control.setAngularVelocity(newSpeed);
                }
            }
            return ballMessage;
        }
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

    private void initCamera() {
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(20f);
    }
}
