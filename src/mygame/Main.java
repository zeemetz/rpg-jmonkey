    package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import jme3tools.converters.ImageToAwt;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
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
import de.lessvoid.nifty.controls.console.builder.ConsoleBuilder;
import de.lessvoid.nifty.controls.dropdown.builder.DropDownBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.Color;



public class Main extends SimpleApplication 
{
    MainMenu mm = new MainMenu();
    OptionMenu om = new OptionMenu();
    PlayingController pc = new PlayingController();
    BulletAppState bulletAppState;
    Nifty n;
    
    // control
    boolean summonControl=false;
    boolean isSummon=false;
    boolean isPlaying = false;
    
    public static void main(String[] args) 
    {
        Main app = new Main();
        AppSettings appset = new AppSettings(true);
        appset.setFullscreen(false);
        appset.setTitle("RPG");
        //implement setting kedalam app
        app.setSettings(appset);
        app.setShowSettings(false);
        
        app.start();
    }

    @Override
    public void simpleInitApp() 
    {
        // option screen
        stateManager.attach(mm);
        stateManager.attach(om);
        stateManager.attach(pc);
        
        // nifty
        NiftyJmeDisplay display = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, viewPort);
        n = display.getNifty();
        guiViewPort.addProcessor(display);
        n.fromXml("Interface/Screen.xml", "start", mm,om, pc); // dengan manipulasi state
        
        // variable declaration
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // setting root
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0, 200, 0));
        cam.lookAt(new Vector3f(0, -1, 0), Vector3f.UNIT_Y);
        
        flyCam.setMoveSpeed(1000);
        flyCam.setDragToRotate(true);        setDisplayStatView(false);
        setDisplayFps(false);
        
        initLight();
        initTerrain();
        //initAudio();
        
        //splash screen
        //splashScreen(n);
        //n.gotoScreen("splashScreen");
    }
    
    AudioNode audio_bg;
    AudioNode audio_setting;
    
    private void initAudio() 
    {
        /*
        audio_bg = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
        audio_bg.setLooping(false);
        audio_bg.setVolume(2);
        rootNode.attachChild(audio_bg);
        */
        audio_setting = new AudioNode(assetManager, "Sounds/lagu.ogg", false);
        audio_setting.setLooping(true);  
        rootNode.attachChild(audio_setting);
        audio_setting.play();
    }

    public void initLight()
    {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -1, -1));
        rootNode.addLight(sun);
    }
    
    TerrainQuad terrain;
    public void initTerrain()
    {
        // Alpha Map
        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/alphaMap.png"));
        
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64);
        
        Texture road = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex2", road);
        mat_terrain.setFloat("Tex2Scale", 64);
        
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex3", dirt);
        mat_terrain.setFloat("Tex3Scale", 64);
        
        // Height Map
         Texture  heightMapImage = assetManager.loadTexture("Textures/heightMap.png");
        //AbstractHeightMap heighMap = new ImageBasedHeightMap( heightMapImage.getImage() );
        AbstractHeightMap heighMap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0) );
        heighMap.load();
        
        // Attach to root
        terrain = new TerrainQuad("My Terrain", 65, 513, heighMap.getHeightMap());
        
        terrain.setMaterial(mat_terrain);
        
        terrain.setLocalScale(1, 0.3f, 1);
        terrain.setLocalTranslation(0, -500, 0);
        rootNode.attachChild(terrain);
        
        //Physics
        CollisionShape colShape = CollisionShapeFactory.createMeshShape(terrain); 
        RigidBodyControl landscape = new RigidBodyControl(colShape, 0); 
        
        terrain.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
        
        // sky
        rootNode.attachChild(SkyFactory.createSky(
            assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    }
    
    
  private Screen splashScreen(final Nifty nifty) {
    Screen screen = new ScreenBuilder("splashScreen") {

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
                    text(new TextBuilder() {

                      {
                        text("Nifty 1.3 Core");
                        style("base-font");
                        alignCenter();
                        valignCenter();
                        onStartScreenEffect(new EffectBuilder("fade") {

                          {
                            length(1000);
                            effectValue("time", "1700", "value", "0.0");
                            effectValue("time", "2000", "value", "1.0");
                            effectValue("time", "2600", "value", "1.0");
                            effectValue("time", "3200", "value", "0.0");
                            post(false);
                            neverStopRendering(true);
                          }
                        });
                      }
                    });
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
                        filename("Interface/yin.png");
                        onStartScreenEffect(new EffectBuilder("move") {

                          {
                            length(1000);
                            startDelay(300);
                            timeType("exp");
                            effectParameter("factor", "6.f");
                            effectParameter("mode", "in");
                            effectParameter("direction", "left");
                          }
                        });
                      }
                    });
                    image(new ImageBuilder() {

                      {
                        filename("Interface/yang.png");
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
                    text(new TextBuilder() {

                      {
                        text("Nifty 1.3 Standard Controls");
                        style("base-font");
                        alignCenter();
                        valignCenter();
                        onStartScreenEffect(new EffectBuilder("fade") {

                          {
                            length(1000);
                            effectValue("time", "1700", "value", "0.0");
                            effectValue("time", "2000", "value", "1.0");
                            effectValue("time", "2600", "value", "1.0");
                            effectValue("time", "3200", "value", "0.0");
                            post(false);
                            neverStopRendering(true);
                          }
                        });
                      }
                    });
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
    
    @Override
    public void simpleUpdate(float tpf)
    {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) 
    {
        //TODO: add render code
    }
}
