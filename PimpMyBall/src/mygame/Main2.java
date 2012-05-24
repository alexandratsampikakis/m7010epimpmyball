/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.converters.ImageToAwt;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Sphere;
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

/**
 * Creates a terrain object and a collision node to go with it. Then
 * drops several balls from the sky that collide with the terrain
 * and roll around.
 * Left click to place a sphere on the ground where the crosshairs intersect the terrain.
 * Hit keys 1 or 2 to raise/lower the terrain at that spot.
 *
 * @author
 */
public class Main2 extends SimpleApplication {

    TerrainQuad terrain;
    Node terrainPhysicsNode;
    
    Material matRock;
    Material matWire;
    Material matTree;
    Spatial tree;
    
    private BasicShadowRenderer bsr;
    private Vector3f[] points;
    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) points[i] = new Vector3f();
    }
    
    protected BitmapText hintText;
    private PointLight pl;
    private Geometry lightMdl;
    private Geometry collisionMarker;
    private BulletAppState bulletAppState;
    private Geometry collisionSphere;
    private Geometry collisionBox;
    private Geometry selectedCollisionObject;
    private Node playerNode;
    private Geometry playerGeometry;
    private RigidBodyControl playerControl;
    private RigidBodyControl treeControl;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private boolean left = false,
            right = false,
            up = false,
            down = false;

    public static void main(String[] args) {
        Main2 app = new Main2();
        app.start();
    }

    /*@Override
    public void initialize() {
    super.initialize();
    loadHintText();
    }*/
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
        initShadow();

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
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0;   // Dessa två gör ingen skillnad.
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left) {
            //walkDirection.addLocal(camLeft);
            walkDirection.addLocal(camDir.negate()); ////////////////sjukt fiskigt!! måste ändra detta sen!!!!
        }
        if (right) {
            //walkDirection.addLocal(camLeft.negate());
            walkDirection.addLocal(camDir);
        }
        if (up) {
            //walkDirection.addLocal(camDir);
            walkDirection.addLocal(camLeft);
        }
        if (down) {
            //walkDirection.addLocal(camDir.negate());
            walkDirection.addLocal(camLeft.negate());
        }
        playerControl.setAngularVelocity(walkDirection.mult(10f));
        
        //For the shadow
        Camera shadowCam = bsr.getShadowCamera(); //Behövs denna?
        ShadowUtil.updateFrustumPoints2(shadowCam, points);
    }
    
    /**
     * 
     */
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
    public void initShadow() {
        bsr = new BasicShadowRenderer(assetManager, 512);
        bsr.setDirection(new Vector3f(-0.5f,-.5f,-.5f).normalizeLocal());
        viewPort.addProcessor(bsr);
    }

    /**
     * 
     * @param oldLoc 
     */
    private void testCollision(Vector3f oldLoc) {
        if (terrain.collideWith(selectedCollisionObject.getWorldBound(), new CollisionResults()) > 0) {
            selectedCollisionObject.setLocalTranslation(oldLoc);
        }
    }

    /**
     * 
     */
    private void initLighting() {
        // Create directional light
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-0.5f,-.5f,-.5f).normalizeLocal());
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
        ChaseCamera camera = new ChaseCamera(cam, playerNode, inputManager);
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
        
        createTrees(-300, 500, 7, 100, 2);
        createTrees(-210, 315, 5, 50, 2);
        createTrees(-20, 130, 5, 50, 5);
        createTrees(-50, 0, 10, 250, 5);
        createTrees(-150, 10, 10, 0, 6);
        createTrees(-100, 150, 15, 100, 5);
        createTrees(-250, 200, 8, 200, 3);
    }

    /**
     * 
     */
    private void initPlayer() {
        float radius = 2;
        playerNode = new Node("Player");
        playerGeometry = new Geometry("PlayerGeometry", new Sphere(100, 100, radius));
        rootNode.attachChild(playerNode);
        playerNode.attachChild(playerGeometry);
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        playerGeometry.setMaterial(material);
        playerNode.setLocalTranslation(new Vector3f(0, 20, 0));
        SphereCollisionShape sphereShape = new SphereCollisionShape(radius);
        float stepHeight = 500f;
        playerControl = new RigidBodyControl(sphereShape, stepHeight);
        playerNode.addControl(playerControl);
        //playerControl.setRestitution(0.001f);
        playerControl.setFriction(12f);
        playerControl.setGravity(new Vector3f(1.0f,1.0f,1.0f)); //??
        playerNode.setShadowMode(ShadowMode.CastAndReceive);
        bulletAppState.getPhysicsSpace().add(playerControl);
    }
    
    /**
     * Fixa collision på träden. fungerar inte!!!!!!
     * @param xTree
     * @param zTree
     * @param sTree
     * @param rTree 
     */
    public void createTrees(float xTree, float zTree, float sTree, float rTree, int toHigherTree) {
        tree = assetManager.loadModel("Models/Tree/Tree.mesh.xml");
        Vector2f xz = new Vector2f(xTree, zTree);
        float yTree = terrain.getHeightmapHeight(xz)+toHigherTree;
        tree.setLocalTranslation(xTree, yTree, zTree);
        tree.scale(sTree);
        tree.rotate(0, rTree, 0);
        
        CollisionShape treeCollisionShape = CollisionShapeFactory.createMeshShape((Node) tree);
        treeControl = new RigidBodyControl(treeCollisionShape, 0);
        tree.addControl(treeControl);
        terrain.attachChild(tree);
        bulletAppState.getPhysicsSpace().add(treeControl);
    }

    /**
     * 
     */
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
}

