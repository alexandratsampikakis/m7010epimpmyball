
package mygame;

import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapText;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.builder.HoverEffectBuilder;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.PopupBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.StyleBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.chatcontrol.ChatControlDialogDefinition;
import de.lessvoid.nifty.controls.common.CommonBuilders;
import de.lessvoid.nifty.controls.common.DialogPanelControlDefinition;
import de.lessvoid.nifty.controls.common.MenuButtonControlDefinition;
import de.lessvoid.nifty.controls.console.builder.ConsoleBuilder;
import de.lessvoid.nifty.controls.dragndrop.DragAndDropDialogDefinition;
import de.lessvoid.nifty.controls.dropdown.DropDownDialogControlDefinition;
import de.lessvoid.nifty.controls.dropdown.builder.DropDownBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.controls.listbox.ListBoxDialogControlDefinition;
import de.lessvoid.nifty.controls.scrollpanel.ScrollPanelDialogControlDefinition;
import de.lessvoid.nifty.controls.sliderandscrollbar.SliderAndScrollbarDialogControlDefinition;
import de.lessvoid.nifty.controls.textfield.TextFieldDialogControlDefinition;
import de.lessvoid.nifty.controls.textfield.TextFieldDialogController;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.Screen;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a terrain object and a collision node to go with it. Then
 * drops several balls from the sky that collide with the terrain
 * and roll around.
 * Left click to place a sphere on the ground where the crosshairs intersect the terrain.
 * Hit keys 1 or 2 to raise/lower the terrain at that spot.
 *
 * @author
 */
public class Main extends SimpleApplication {

    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Node treeNode;
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
    private Geometry treeGeom;
    private Node playerNode;
    private Geometry playerGeometry;
    private RigidBodyControl playerControl;
    private RigidBodyControl treeControl;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private ParticleEmitter smoke;
    private static CommonBuilders builders = new CommonBuilders();

    public static void main(String[] args) {
        Main app = new Main();
        TextFieldDialogController.haxx = app;
        app.start();
    }

    public Main() {
        
    }
    /**
     * 
     */
    @Override
    public void simpleInitApp() {
        
        //Play sound
        AudioNode backgroundMusic = new AudioNode(assetManager, "Sounds/gameMusic.wav", true);
        backgroundMusic.setVolume(0);
        backgroundMusic.play();
        
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
        
        // Activate the Nifty-JME integration:
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE); 
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);

        guiViewPort.addProcessor(niftyDisplay);
        flyCam.setDragToRotate(true); // Need the mouse for clicking.
        
        /**
     * nifty demo code
     */
    nifty.loadStyleFile("nifty-default-styles.xml");
    nifty.loadControlFile("nifty-default-controls.xml");
    nifty.registerSound("intro", "Interface/sound/19546__tobi123__Gong_mf2.wav");
    nifty.registerMusic("credits", "Interface/sound/Loveshadow_-_Almost_Given_Up.ogg");
    nifty.registerMouseCursor("hand", "Interface/mouse-cursor-hand.png", 5, 4);
    //nifty.setDebugOptionPanelColors(true);
    registerMenuButtonHintStyle(nifty);
    registerStyles(nifty);
    registerConsolePopup(nifty);

    // register some helper controls
    MenuButtonControlDefinition.register(nifty);
    DialogPanelControlDefinition.register(nifty);

    // register the dialog controls
    ListBoxDialogControlDefinition.register(nifty);
    DropDownDialogControlDefinition.register(nifty);
    ScrollPanelDialogControlDefinition.register(nifty);
    ChatControlDialogDefinition.register(nifty);
    TextFieldDialogControlDefinition.register(nifty);
    SliderAndScrollbarDialogControlDefinition.register(nifty);
    DragAndDropDialogDefinition.register(nifty);

    createIntroScreen(nifty);
    createDemoScreen(nifty);
    nifty.gotoScreen("start");
   
    }

    public void startGame() {
        setUpTerrain();
    }
    
    @Override
    public void update() {
        super.update();
    }

    @Override
    public void simpleUpdate(float tpf) {
        /*Vector3f camDir = cam.getDirection().clone();
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
        ShadowUtil.updateFrustumPoints2(shadowCam, points);*/
        
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
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private static Screen createIntroScreen(final Nifty nifty) {
        Screen screen = new ScreenBuilder("start") {

      {
        controller(new DefaultScreenController() {

          @Override
          public void onStartScreen() {
            nifty.gotoScreen("demo");
          }
        });
        layer(new LayerBuilder("layer") {

          {
            childLayoutCenter();
            onStartScreenEffect(new EffectBuilder("fade") {

              {
                length(3000);
                effectParameter("start", "#0");
                effectParameter("end", "#f");
              }
            });
            onStartScreenEffect(new EffectBuilder("playSound") {

              {
                startDelay(1400);
                effectParameter("sound", "intro");
              }
            });
            onActiveEffect(new EffectBuilder("gradient") {

              {
                effectValue("offset", "0%", "color", "#66666fff");
                effectValue("offset", "85%", "color", "#000f");
                effectValue("offset", "100%", "color", "#44444fff");
              }
            });
            panel(new PanelBuilder() {

              {
                alignCenter();
                valignCenter();
                childLayoutHorizontal();
                width("856px");
                panel(new PanelBuilder() {

                  {
                    width("300px");
                    height("256px");
                    childLayoutCenter();
                    
                  }
                });
                panel(new PanelBuilder() {

                  {
                    alignCenter();
                    valignCenter();
                    childLayoutOverlay();
                    width("256px");
                    height("256px");
                    onStartScreenEffect(new EffectBuilder("shake") {

                      {
                        length(250);
                        startDelay(1300);
                        inherit();
                        effectParameter("global", "false");
                        effectParameter("distance", "10.");
                      }
                    });
                    onStartScreenEffect(new EffectBuilder("imageSize") {

                      {
                        length(600);
                        startDelay(3000);
                        effectParameter("startSize", "1.0");
                        effectParameter("endSize", "2.0");
                        inherit();
                        neverStopRendering(true);
                      }
                    });
                    onStartScreenEffect(new EffectBuilder("fade") {

                      {
                        length(600);
                        startDelay(3000);
                        effectParameter("start", "#f");
                        effectParameter("end", "#0");
                        inherit();
                        neverStopRendering(true);
                      }
                    });
                    image(new ImageBuilder() {

                      {
                        filename("Interface/pimpMyBall_small.png");
                        onStartScreenEffect(new EffectBuilder("move") {

                          {
                            length(1000);
                            startDelay(300);
                            timeType("exp");
                            effectParameter("factor", "6.f");
                            effectParameter("mode", "in");
                            effectParameter("direction", "right");
                          }
                        });
                      }
                    });
                  }
                });
                panel(new PanelBuilder() {

                  {
                    width("300px");
                    height("256px");
                    childLayoutCenter();
                    
                  }
                });
              }
            });
          }
        });
        layer(new LayerBuilder() {

          {
            backgroundColor("#ddff");
            onStartScreenEffect(new EffectBuilder("fade") {

              {
                length(1000);
                startDelay(3000);
                effectParameter("start", "#0");
                effectParameter("end", "#f");
              }
            });
          }
        });
      }
    }.build(nifty);
    return screen;
  }
    
  private static Screen createDemoScreen(final Nifty nifty) {
    final CommonBuilders common = new CommonBuilders();
    Screen screen = new ScreenBuilder("demo") {

      {
        controller(new ControlsScreenController(
                //"menuButtonListBox", "dialogListBox",
                //"menuButtonDropDown", "dialogDropDown",
                "menuButtonTextField", "dialogTextField",
                "menuButtonSlider", "dialogSliderAndScrollbar",
                "menuButtonScrollPanel", "dialogScrollPanel",
                "menuButtonChatControl", "dialogChatControl",
                "menuButtonDragAndDrop", "dialogDragAndDrop"));
        inputMapping("de.lessvoid.nifty.input.mapping.DefaultInputMapping"); // this will enable Keyboard events for the screen controller
        layer(new LayerBuilder("layer") {
        
          {
            //backgroundImage("Interface/background-new.png");
            childLayoutVertical();
            panel(new PanelBuilder("navigation") {

              {
                width("100%");
                height("63px");
                backgroundColor("#5588");
                childLayoutHorizontal();
                padding("20px");
                //control(MenuButtonControlDefinition.getControlBuilder("menuButtonListBox", "ListBox", "ListBox demonstration\n\nThis example shows adding and removing items from a ListBox\nas well as the different selection modes that are available."));
                panel(builders.hspacer("10px"));
                //control(MenuButtonControlDefinition.getControlBuilder("menuButtonDropDown", "DropDown", "DropDown and RadioButton demonstration\n\nThis shows how to dynamically add items to the\nDropDown control as well as the change event."));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonTextField", "Log in", "TextField demonstration\n\nThis example demonstrates the Textfield example using the password\nmode and the input length restriction. It also demonstrates\nall of the new events the Textfield publishes on the Eventbus."));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonSlider", "My profile", "Sliders and Scrollbars demonstration\n\nThis creates sliders to change a RGBA value and it\ndisplays a scrollbar that can be customized."));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonScrollPanel", "Statistics", "ScrollPanel demonstration\n\nThis simply shows an image and uses the ScrollPanel\nto scroll around its area. You can directly input\nthe x/y position you want the ScrollPanel to scroll to."));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonChatControl", "ChatControl", "Chat Control demonstration\n\nThis control was contributed by Nifty User ractoc. It demonstrates\nhow you can combine Nifty standard controls to build more\ncomplex stuff. In this case we've just included his work as\nanother standard control to Nifty! :)"));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonDragAndDrop", "Drag and Drop", "Drag and Drop demonstration\n\nDrag and Drop has been extended with Nifty 1.3"));
                panel(builders.hspacer("10px"));
                control(MenuButtonControlDefinition.getControlBuilder("menuButtonCredits", "?", "Credits\n\nCredits and Thanks!", "25px"));
              }
            });
            panel(new PanelBuilder("dialogParent") {

              {
                childLayoutOverlay();
                width("100%");
                alignCenter();
                valignCenter();
                control(new ControlBuilder("dialogListBox", ListBoxDialogControlDefinition.NAME));
                control(new ControlBuilder("dialogTextField", TextFieldDialogControlDefinition.NAME));
                control(new ControlBuilder("dialogSliderAndScrollbar", SliderAndScrollbarDialogControlDefinition.NAME));
                control(new ControlBuilder("dialogDropDown", DropDownDialogControlDefinition.NAME));
                control(new ControlBuilder("dialogScrollPanel", ScrollPanelDialogControlDefinition.NAME));
                control(new ControlBuilder("dialogChatControl", ChatControlDialogDefinition.NAME));
                control(new ControlBuilder("dialogDragAndDrop", DragAndDropDialogDefinition.NAME));
              }
            });
          }
        });
        layer(new LayerBuilder() {

          {
            childLayoutVertical();
            panel(new PanelBuilder() {

              {
                height("*");
              }
            });
            panel(new PanelBuilder() {

              {
                childLayoutCenter();
                height("50px");
                width("100%");
                backgroundColor("#5588");
                panel(new PanelBuilder() {

                  {
                    paddingLeft("25px");
                    paddingRight("25px");
                    height("50%");
                    width("100%");
                    alignCenter();
                    valignCenter();
                    childLayoutHorizontal();
                    control(new LabelBuilder() {

                      {
                        label("Screen Resolution:");
                      }
                    });
                    panel(common.hspacer("7px"));
                    control(new DropDownBuilder("resolutions") {

                      {
                        width("200px");
                      }
                    });
                    panel(common.hspacer("*"));
                    control(new ButtonBuilder("resetScreenButton", "Restart Screen") {

                      {
                      }
                    });
                  }
                });
              }
            });
          }
        });
        layer(new LayerBuilder("whiteOverlay") {

          {
            onCustomEffect(new EffectBuilder("renderQuad") {

              {
                customKey("onResolutionStart");
                length(350);
                neverStopRendering(false);
              }
            });
            onStartScreenEffect(new EffectBuilder("renderQuad") {

              {
                length(300);
                effectParameter("startColor", "#ddff");
                effectParameter("endColor", "#0000");
              }
            });
            onEndScreenEffect(new EffectBuilder("renderQuad") {

              {
                length(300);
                effectParameter("startColor", "#0000");
                effectParameter("endColor", "#ddff");
              }
            });
          }
        });
      }
    }.build(nifty);
    return screen;
  }
  
  private static void registerMenuButtonHintStyle(final Nifty nifty) {
    new StyleBuilder() {

      {
        id("special-hint");
        base("nifty-panel-bright");
        childLayoutCenter();
        onShowEffect(new EffectBuilder("fade") {

          {
            length(150);
            effectParameter("start", "#0");
            effectParameter("end", "#d");
            inherit();
            neverStopRendering(true);
          }
        });
        onShowEffect(new EffectBuilder("move") {

          {
            length(150);
            inherit();
            neverStopRendering(true);
            effectParameter("mode", "fromOffset");
            effectParameter("offsetY", "-15");
          }
        });
        onCustomEffect(new EffectBuilder("fade") {

          {
            length(150);
            effectParameter("start", "#d");
            effectParameter("end", "#0");
            inherit();
            neverStopRendering(true);
          }
        });
        onCustomEffect(new EffectBuilder("move") {

          {
            length(150);
            inherit();
            neverStopRendering(true);
            effectParameter("mode", "toOffset");
            effectParameter("offsetY", "-15");
          }
        });
      }
    }.build(nifty);

    new StyleBuilder() {

      {
        id("special-hint#hint-text");
        base("base-font");
        alignLeft();
        valignCenter();
        textHAlignLeft();
        color("#000f");
      }
    }.build(nifty);
  }

  private static void registerStyles(final Nifty nifty) {
    new StyleBuilder() {

      {
        id("base-font-link");
        base("base-font");
        color("#8fff");
        interactOnRelease("$action");
        onHoverEffect(new HoverEffectBuilder("changeMouseCursor") {

          {
            effectParameter("id", "hand");
          }
        });
      }
    }.build(nifty);

    new StyleBuilder() {

      {
        id("creditsImage");
        alignCenter();
      }
    }.build(nifty);

    new StyleBuilder() {

      {
        id("creditsCaption");
        font("Interface/verdana-48-regular.fnt");
        width("100%");
        textHAlignCenter();
      }
    }.build(nifty);

    new StyleBuilder() {

      {
        id("creditsCenter");
        base("base-font");
        width("100%");
        textHAlignCenter();
      }
    }.build(nifty);
  }

  private static void registerConsolePopup(Nifty nifty) {
    new PopupBuilder("consolePopup") {

      {
        childLayoutAbsolute();
        panel(new PanelBuilder() {

          {
            childLayoutCenter();
            width("100%");
            height("100%");
            alignCenter();
            valignCenter();
            control(new ConsoleBuilder("console") {

              {
                width("80%");
                lines(25);
                alignCenter();
                valignCenter();
                onStartScreenEffect(new EffectBuilder("move") {

                  {
                    length(150);
                    inherit();
                    neverStopRendering(true);
                    effectParameter("mode", "in");
                    effectParameter("direction", "top");
                  }
                });
                onEndScreenEffect(new EffectBuilder("move") {

                  {
                    length(150);
                    inherit();
                    neverStopRendering(true);
                    effectParameter("mode", "out");
                    effectParameter("direction", "top");
                  }
                });
              }
            });
          }
        });
      }
    }.registerPopup(nifty);
  }

  
    
  public void crap() {
      System.out.println("SKRIIIV!!!!!!");
  }
  
  public void sendLogin(String username, String password) {
      System.out.println("USer:" + username + ", pass: " + password);
  }
}
