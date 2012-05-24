
package mygame;

import com.jme3.app.Application;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.Color;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
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
import de.lessvoid.nifty.builder.TextBuilder;
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
import java.awt.Component;
import java.io.IOException;
import javax.swing.JOptionPane;
import mygame.admin.CentralServer;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginSuccessMessage;
import mygame.admin.NetworkHelper;
import mygame.admin.ServerInfo;
import mygame.balls.client.BallClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.boardgames.BoardGameAppState;
import com.jme3.network.Client;
import com.jme3.scene.UserData;
import mygame.admin.SerializerHelper;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginSuccessMessage;

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

    private static CommonBuilders builders = new CommonBuilders();
    private TextFieldDialogController textFieldDialogController;
    static Client centralServerClient;
    static CentralServerListener centralServerListener;
    static BallClient ballClient;
    private boolean changeScreen;
    NiftyJmeDisplay niftyDisplay;
    Nifty nifty;
    private String username;
    private String password;
    private Application app;
    Main2 main2;

    /**
     * MAIN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        Main app = new Main();
        TextFieldDialogController.haxx = app;
        
        SerializerHelper.initializeClasses();
        ServerInfo centralServerInfo = CentralServer.info;
        centralServerClient = NetworkHelper.connectToServer(centralServerInfo);
        centralServerListener = new CentralServerListener();
        centralServerClient.addMessageListener(centralServerListener);
        centralServerClient.start();
        app.start();
    }
    
    /**
     * 
     * @param userName
     * @param passWord 
     */
    public void sendLogin(String userName, String passWord) {
        username = userName;
        password = passWord;
        changeScreen = true;
        System.out.println("User:" + username + ", pass: " + password);
        centralServerClient.send(new LoginMessage(username, password));
    }

    private static class CentralServerListener implements MessageListener<Client> {
        
        public void messageReceived(Client source, Message message) {

            if (message instanceof LoginSuccessMessage) {

                LoginSuccessMessage loginMessage = (LoginSuccessMessage) message;
                BallClient app;
                try {
                    System.out.println("ServerInfo.NAME: " + loginMessage.serverInfo.NAME);
                    System.out.println("UserData.userName: " + loginMessage.userData.userName);
                    app = new BallClient(loginMessage.serverInfo, loginMessage.userData, loginMessage.secret);
                    app.start();
                    app.setPauseOnLostFocus(false);
                    
                } catch (Exception ex) {
                    Logger.getLogger(BallClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    
    /**
     * 
     */
    @Override
    public void simpleInitApp() {
        //Creating a sky
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
        
        // Activate the Nifty-JME integration:
        niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
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
       registerCreditsPopup(nifty);

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
       createGameScreen(nifty);
       //nifty.gotoScreen("startGame");
        
    }
    
    @Override
    public void update() {
        super.update();
    }
    
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private static Screen createGameScreen(final Nifty nifty) {
        Screen screen = new ScreenBuilder("startGame") {

      {
        controller(new DefaultScreenController() {
            
          @Override
          public void onStartScreen() {
            //nifty.gotoScreen("demo");
          }

          public void quitGame() {
            nifty.exit();
            
          }
        });
        
      }
    }.build(nifty);
    return screen;
  }
    
    /**
     * 
     * @param nifty
     * @return 
     */
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
                "menuButtonDragAndDrop", "dialogDragAndDrop"){
                    
                    @Override
                    public void onStartScreen() {
                        //nifty.gotoScreen("demo");
                    }
                    
                    public void startGame(String nextScreen) {
                        //nifty.gotoScreen(nextScreen);  // switch to another screen
                        //nifty.exit();
                        nifty.gotoScreen("startGame");
                    }

                    public void quitGame() {
                        //app.stop();
                        System.out.println("Avslut!"); // kommer inte hit :(
                    }
                });
        
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

  private static void registerCreditsPopup(final Nifty nifty) {
    final CommonBuilders common = new CommonBuilders();
    new PopupBuilder("creditsPopup") {

      {
        childLayoutCenter();
        panel(new PanelBuilder() {

          {
            width("80%");
            height("80%");
            alignCenter();
            valignCenter();
            onStartScreenEffect(new EffectBuilder("move") {

              {
                length(400);
                inherit();
                effectParameter("mode", "in");
                effectParameter("direction", "top");
              }
            });
            onEndScreenEffect(new EffectBuilder("move") {

              {
                length(400);
                inherit();
                neverStopRendering(true);
                effectParameter("mode", "out");
                effectParameter("direction", "top");
              }
            });
            onEndScreenEffect(new EffectBuilder("fadeSound") {

              {
                effectParameter("sound", "credits");
              }
            });
            onActiveEffect(new EffectBuilder("gradient") {

              {
                effectValue("offset", "0%", "color", "#00bffecc");
                effectValue("offset", "75%", "color", "#00213cff");
                effectValue("offset", "100%", "color", "#880000cc");
              }
            });
            onActiveEffect(new EffectBuilder("playSound") {

              {
                effectParameter("sound", "credits");
              }
            });
            padding("10px");
            childLayoutVertical();
            panel(new PanelBuilder() {

              {
                width("100%");
                height("*");
                childLayoutOverlay();
                childClip(true);
                panel(new PanelBuilder() {

                  {
                    width("100%");
                    childLayoutVertical();
                    onActiveEffect(new EffectBuilder("autoScroll") {

                      {
                        length(100000);
                        effectParameter("start", "0");
                        effectParameter("end", "-3200");
                        inherit(true);
                      }
                    });
                    panel(common.vspacer("800px"));
                    text(new TextBuilder() {

                      {
                        text("Nifty 1.3");
                        style("creditsCaption");
                      }
                    });
                    text(new TextBuilder() {

                      {
                        text("Standard Controls Demonstration using JavaBuilder pattern");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("30px"));
                    text(new TextBuilder() {

                      {
                        text("\"Look ma, No XML!\" :)");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("70px"));
                    panel(new PanelBuilder() {

                      {
                        width("100%");
                        height("256px");
                        childLayoutCenter();
                        panel(new PanelBuilder() {

                          {
                            alignCenter();
                            valignCenter();
                            childLayoutHorizontal();
                            width("656px");
                            panel(new PanelBuilder() {

                              {
                                width("200px");
                                height("256px");
                                childLayoutCenter();
                                
                              }
                            });
                            panel(new PanelBuilder() {

                              {
                                width("256px");
                                height("256px");
                                alignCenter();
                                valignCenter();
                                childLayoutOverlay();
                                image(new ImageBuilder() {

                                  {
                                    filename("Interface/pimpMyBall_small.png");
                                  }
                                });
                              }
                            });
                            panel(new PanelBuilder() {

                              {
                                width("200px");
                                height("256px");
                                childLayoutCenter();
                                text(new TextBuilder() {

                                  {
                                    text("Nifty 1.3 Standard Controls");
                                    style("base-font");
                                    alignCenter();
                                    valignCenter();
                                  }
                                });
                              }
                            });
                          }
                        });
                      }
                    });
                    panel(common.vspacer("70px"));
                    text(new TextBuilder() {

                      {
                        text("written and performed\nby void");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("100px"));
                    text(new TextBuilder() {

                      {
                        text("Sound Credits");
                        style("creditsCaption");
                      }
                    });
                    text(new TextBuilder() {

                      {
                        text("This demonstration uses creative commons licenced sound samples\nand music from the following sources");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("30px"));
                    image(new ImageBuilder() {

                      {
                        style("creditsImage");
                        filename("Interface/freesound.png");
                      }
                    });
                    panel(common.vspacer("25px"));
                    text(new TextBuilder() {

                      {
                        text("Interface/19546__tobi123__Gong_mf2.wav");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("50px"));
                    image(new ImageBuilder() {

                      {
                        style("creditsImage");
                        filename("Interface/cc-mixter-logo.png");
                        set("action", "openLink(http://ccmixter.org/)");
                      }
                    });
                    panel(common.vspacer("25px"));
                    text(new TextBuilder() {

                      {
                        text("\"Almost Given Up\" by Loveshadow");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("100px"));
                    text(new TextBuilder() {

                      {
                        text("Additional Credits");
                        style("creditsCaption");
                      }
                    });
                    text(new TextBuilder() {

                      {
                        //text("ueber awesome Yin/Yang graphic by Dori\n(http://www.nadori.de)\n\nThanks! :)");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("100px"));
                    text(new TextBuilder() {

                      {
                        text("Special thanks go to");
                        style("creditsCaption");
                      }
                    });
                    text(new TextBuilder() {

                      {
                        text(
                                "The following people helped creating Nifty with valuable feedback,\nfixing bugs or sending patches.\n(in no particular order)\n\n"
                                + "chaz0x0\n"
                                + "Tumaini\n"
                                + "arielsan\n"
                                + "gaba1978\n"
                                + "ractoc\n"
                                + "bonechilla\n"
                                + "mdeletrain\n"
                                + "mulov\n"
                                + "gouessej\n");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("75px"));
                    text(new TextBuilder() {

                      {
                        text("Greetings and kudos go out to");
                        style("creditsCaption");
                      }
                    });
                    text(new TextBuilder() {

                      {
                        text(
                                "Ariel Coppes and Ruben Garat of Gemserk\n(http://blog.gemserk.com/)\n\n\n"
                                + "Erlend, Kirill, Normen, Skye and Ruth of jMonkeyEngine\n(http://www.jmonkeyengine.com/home/)\n\n\n"
                                + "Brian Matzon, Elias Naur, Caspian Rychlik-Prince for lwjgl\n(http://www.lwjgl.org/\n\n\n"
                                + "KappaOne, MatthiasM, aho, Dragonene, darkprophet, appel, woogley, Riven, NoobFukaire\nfor valuable input and discussions at #lwjgl IRC on the freenode network\n\n\n"
                                + "... and Kevin Glass\n(http://slick.cokeandcode.com/)\n\n\n\n\n\n\n\n"
                                + "As well as everybody that has not yet given up on Nifty =)\n\n"
                                + "And again sorry to all of you that I've forgotten. You rock too!\n\n\n");
                        style("creditsCenter");
                      }
                    });
                    panel(common.vspacer("350px"));
                    image(new ImageBuilder() {

                      {
                        style("creditsImage");
                        filename("Interface/nifty-logo.png");
                      }
                    });
                  }
                });
              }
            });
            panel(new PanelBuilder() {

              {
                width("100%");
                paddingTop("10px");
                childLayoutCenter();
                control(new ButtonBuilder("creditsBack") {

                  {
                    label("Back");
                    alignRight();
                    valignCenter();
                  }
                });
              }
            });
          }
        });
      }
    }.registerPopup(nifty);
  }
    
}
