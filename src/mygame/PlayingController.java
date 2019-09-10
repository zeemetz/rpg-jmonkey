
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.JOptionPane;
import sun.awt.EmbeddedFrame;
import sun.security.x509.IssuerAlternativeNameExtension;

public class PlayingController extends AbstractAppState implements ScreenController
{
    public Nifty nifty;
    Main app;
    Node rootNode;
    AssetManager assetManager;
    BulletAppState bulletAppState;
    
    TerrainQuad terrain;
    Geometry geom;
    
    FlyByCamera flyCam;
    
    Vector<ModelCharacter> hero= new Vector<ModelCharacter>();
    Vector<Enemy> enemy= new Vector<Enemy>();
    
    Node actionNode = new Node();
    
    boolean playerPhase = true;
    
    int money=15000;
    
    public int getMoney()
    {
        return money;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) 
    {
        // main component
        super.initialize(stateManager, app);
        this.app = (Main)app;
        assetManager = this.app.getAssetManager();
        this.rootNode = this.app.getRootNode();
        this.cam = this.app.getCamera();
        this.inputManager = this.app.getInputManager();
        this.terrain = this.app.terrain;
        this.bulletAppState = this.app.bulletAppState;
        this.flyCam = this.app.getFlyByCamera();
        this.nifty = app.getStateManager().getState(MainMenu.class).getNifty();
        currTime = lastTime = 0;
        
        // cursor
        Box b = new Box(new Vector3f().zero(), 8, 0, 8);
        geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        // listener
        inputManager.addMapping("leftMouseCLick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("action", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(action, "leftMouseCLick", "action");
        
        // enemy 
        enemy.add(new Enemy(assetManager, "Models/Sinbad/Sinbad.mesh.xml"));
        rootNode.attachChild(enemy.lastElement().model);
        bulletAppState.getPhysicsSpace().add(enemy.lastElement().player);
        enemy.lastElement().player.setPhysicsLocation(new Vector3f(-104, -400, 0));
        
        enemy.add(new Enemy(assetManager, "Models/Sinbad/Sinbad.mesh.xml"));
        rootNode.attachChild(enemy.lastElement().model);
        bulletAppState.getPhysicsSpace().add(enemy.lastElement().player);
        enemy.lastElement().player.setPhysicsLocation(new Vector3f(104, -400, 0));
    }
    
    public ActionListener action = new ActionListener() 
    {
        public void onAction(String name, boolean isPressed, float tpf) 
        {
            if(isPressed)
            {
                if(name.equals("leftMouseCLick"))
                {
                    if(app.summonControl)
                    {
                        flyCam.setEnabled(false);
                        app.summonControl=false;
                        size++;
                    }
                    
                    //====================================================================================
                    
                    // pick hero
                    if(app.isPlaying)
                    {
                        CollisionResults crs = new CollisionResults();
                        Vector2f cursor = inputManager.getCursorPosition();
                        Vector3f start = cam.getWorldCoordinates(cursor, 0);
                        Vector3f des = cam.getWorldCoordinates(cursor, 1).subtract(start).normalize();

                        // update box position
                        Ray ray = new Ray(start, des);
                        for(int i = 0 ; i < hero.size() ; i++)
                        {
                            hero.get(i).model.collideWith(ray, crs);
                            if(crs.size()>0)
                            {
                               nifty.gotoScreen("action");
                               hero.get(i).isPicked = true;
                               hero.get(i).channel.setAnim("RunBase");
                               crs.clear();
                               break;
                            }
                            else
                            {
                                nifty.gotoScreen("none");
                                hero.get(i).isPicked = false;
                                hero.get(i).attacking = false;
                                hero.get(i).isMove = false;
                                hero.get(i).healing = false;
                                hero.get(i).channel.setAnim("IdleBase");
                                //hero.get(i).clearNode(actionNode);
                                //rootNode.detachChild(actionNode);
                                floor.detachAllChildren();
                            }
                        }
                    }
                }
                
                //==========================================================================================
                
                if (name.equals("action") && playerPhase)
                {
                    CollisionResults crs = new CollisionResults();
                    Vector2f cursor = inputManager.getCursorPosition();
                    Vector3f start = cam.getWorldCoordinates(cursor, 0);
                    Vector3f des = cam.getWorldCoordinates(cursor, 1).subtract(start).normalize();

                    // update box position
                    Ray ray = new Ray(start, des);
                    
                    for(int i = 0 ; i < hero.size() ; i++)
                    {
                        floor.collideWith(ray, crs);
                        
                        if(crs.size()>0 && !hero.get(i).endTurn)
                        {
                            if(hero.get(i).isMove && hero.get(i).isPicked)
                            {
                               hero.get(i).moveTo(koordinat);

                               hero.get(i).isMove = false;
                               hero.get(i).moving = true;
                            }
                            if(hero.get(i).attacking && hero.get(i).isPicked)
                            {
                                if(cekColideWithEnemy(enemy.get(i)))
                                {
                                    hero.get(i).attackTo(koordinat,actionNode);
                                    rootNode.attachChild(actionNode);
                                    hero.get(i).endTurn = true;
                                }
                                else
                                {
                                    JOptionPane.showMessageDialog(null, "Pick Enemy To Attack");
                                }
                            }
                            if(hero.get(i).healing && hero.get(i).isPicked)
                            {
                                if(cekColideWithHero(hero.get(i)))
                                {
                                    hero.get(i).healTo(koordinat,actionNode);
                                    rootNode.attachChild(actionNode);
                                    hero.get(i).endTurn = true;
                                }
                                else
                                {
                                    JOptionPane.showMessageDialog(null, "Pick Hero To Heal");
                                }
                            }
                        }
                    }
                }
                
            }
        }
    };
    
    public boolean cekColideWithHero(ModelCharacter temp)
    {
        CollisionResults crs = new CollisionResults();
        Vector2f cursor = inputManager.getCursorPosition();
        Vector3f start = cam.getWorldCoordinates(cursor, 0);
        Vector3f des = cam.getWorldCoordinates(cursor, 1).subtract(start).normalize();

        // update box position
        Ray ray = new Ray(start, des);
        
        for(int i = 0 ; i < hero.size();i++)
        {
            hero.get(i).model.collideWith(ray, crs);
            if(crs.size()>0)
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean cekColideWithEnemy(Enemy temp)
    {
        CollisionResults crs = new CollisionResults();
        Vector2f cursor = inputManager.getCursorPosition();
        Vector3f start = cam.getWorldCoordinates(cursor, 0);
        Vector3f des = cam.getWorldCoordinates(cursor, 1).subtract(start).normalize();

        // update box position
        Ray ray = new Ray(start, des);
        
        for(int i = 0 ; i < enemy.size();i++)
        {
            enemy.get(i).model.collideWith(ray, crs);
            if(crs.size()>0)
            {
                return true;
            }
        }
        return false;
    }
    
    Node floor = new Node();
    private void generateMovement(Vector3f pos, int times) 
    {
        for(int row = -times ; row <= times; row++ ) {
            for(int col = -times; col <= times; col++) {
                if( Math.abs(row) + Math.abs(col) > times ) continue;
                Vector3f temporary = pos.clone();
                temporary.x += row*16;
                temporary.z += col*16;
                temporary.y -= 8;
                        
                Box b = new Box(temporary, 8, 0, 8);
                Geometry geom = new Geometry("box", b);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.Red);
                geom.setMaterial(mat);
                mat.getAdditionalRenderState().setWireframe(true);
                floor.attachChild(geom);
            }
        }
        rootNode.attachChild(floor);
    }
    
    int size=0;
    InputManager inputManager;
    Camera cam;
    @Override
    public void update(float tpf) 
    {
        super.update(tpf);
        
        //=====================================================================================================================
        // get mouse position
        CollisionResults crs = new CollisionResults();
        Vector2f cursor = inputManager.getCursorPosition();
        Vector3f start = cam.getWorldCoordinates(cursor, 0);
        Vector3f des = cam.getWorldCoordinates(cursor, 1).subtract(start).normalize();
        
        //=====================================================================================================================
        // update box position
        Ray ray = new Ray(start, des);
        terrain.collideWith(ray, crs);
        if(crs.size()>0)
        {
           createCursor(crs.getClosestCollision().getContactPoint());
        }
        
        //=====================================================================================================================
        // summon control and placing character
        if(app.summonControl)
        {
            flyCam.setEnabled(true);
        }
        if(app.isSummon)
        {
            summon();
            app.isSummon = false;
        }
        if(hero.size()>size)
        {
            hero.lastElement().player.setPhysicsLocation(new Vector3f(koordinat.x,koordinat.y+50,koordinat.z));
        }
        
        //=====================================================================================================================
        // hero movement stoping
        for(int i = 0 ; i < hero.size() ; i++)
        {
            if(hero.get(i).moving)
            {
                hero.get(i).currPos = hero.get(i).player.getPhysicsLocation().clone();
                System.out.println(hero.get(i).currPos.distance(hero.get(i).destination));
                if(hero.get(i).currPos.distance(hero.get(i).destination) <= 20.6)
                {
                    Vector3f newPos = hero.get(i).destination.clone();
                    hero.get(i).player.setWalkDirection(Vector3f.ZERO);
                    hero.get(i).player.setPhysicsLocation(new Vector3f(newPos.x, newPos.y+20, newPos.z));
                    hero.get(i).moving = false;
                    hero.get(i).channel.setAnim("IdleBase");
                    floor.detachAllChildren();
                }
            }
        }
        
        //=====================================================================================================================
        // enemy bot
        int heroIndex=hero.size()-1;
        if(hero.size() > 0 && app.isPlaying && !playerPhase)
        {
            for(int i = 0 ; i < enemy.size(); i ++)
            {
                int max = (int) Math.abs(enemy.get(i).player.getPhysicsLocation().clone().distance(hero.lastElement().player.getPhysicsLocation().clone()));
                ModelCharacter nearestHero = hero.lastElement();
                for(int j = 0 ; j < hero.size()-1 ; j++)
                {
                    if((int)Math.abs(enemy.get(i).player.getPhysicsLocation().clone().distance(hero.get(j).player.getPhysicsLocation().clone())) <= max)
                    {
                        max = (int)Math.abs(enemy.get(i).player.getPhysicsLocation().clone().distance(hero.get(j).player.getPhysicsLocation().clone()));
                        nearestHero = hero.get(j);
                        heroIndex = j;
                    }
                }
                System.out.println(heroIndex);
                if(enemy.get(i).enemyAnimControl)
                {
                    enemy.get(i).channel.setAnim("RunBase");
                    enemy.get(i).enemyAnimControl = false;
                }
                enemy.get(i).moveTo(nearestHero.player.getPhysicsLocation().clone());
                enemyMoveControl(enemy.get(i), nearestHero);
                
                if(cekEnemyEndPhase(enemy))
                {
                    playerPhase = true;
                    enemyEndPhase();
                }
               
            }
        }
        
        if(playerPhase)
        {
           currTime = System.currentTimeMillis(); 
        }
        
        if((currTime - lastTime) > 3000)
        {
            enemyNode.detachAllChildren();
            rootNode.detachChild(enemyNode);
        }
    }
    
    long lastTime, currTime;
    
    public void heroEndPhase()
    {
        for(int i = 0 ; i < hero.size() ; i++)
        {
            hero.get(i).refresh();
        }
        actionNode.detachAllChildren();
        rootNode.detachChild(actionNode);
    }
    
    public void enemyEndPhase()
    {
        for(int i = 0 ; i < enemy.size() ; i++)
        {
            enemy.get(i).refresh();
        }
        //enemyNode.detachAllChildren();
        //rootNode.detachChild(enemyNode);
    }
    
    public boolean cekEnemyEndPhase( Vector<Enemy> enemy )
    {
        int index = 0;
        for(int i = 0 ; i < enemy.size() ; i ++)
        {
            if(enemy.get(i).endTurn)
            {
                index++;
            }
        }
        if(index == enemy.size())
            return true;
        
        else
            return false;
    }
    
    Node enemyNode = new Node();
    
    public void enemyMoveControl(Enemy enemy, ModelCharacter hero)
    {
        Vector3f currHeroPos = hero.player.getPhysicsLocation().clone();
        Vector3f currEnemyPos = enemy.player.getPhysicsLocation().clone();
        generateGridTales(currEnemyPos);
        if( (int)Math.abs( currEnemyPos.distance(currHeroPos) ) < 48 )
        {
            enemy.player.setWalkDirection(Vector3f.ZERO);
            generateGridTales(currEnemyPos);
            enemy.player.setPhysicsLocation(currEnemyPos);
            enemy.channel.setAnim("IdleBase");
            
             // enemy attack
            if(!enemy.attacking)
            {
                enemy.attackTo(hero.player.getPhysicsLocation().clone(), enemyNode);
                rootNode.attachChild(enemyNode);
                enemy.attacking = true;
                enemy.endTurn = true;
                lastTime = System.currentTimeMillis();
            }
        }
    }
    
    public void generateGridTales(Vector3f koordinat)
    {
        koordinat.x = (koordinat.x > 0) ? (koordinat.x-(koordinat.x%16)+8) : (koordinat.x-(koordinat.x%16)-8);
        koordinat.z = (koordinat.z > 0) ? (koordinat.z-(koordinat.z%16)+8) : (koordinat.z-(koordinat.z%16)-8);
    }
    
    Vector3f koordinat;
    public void createCursor(Vector3f koordinat)
    {
        generateGridTales(koordinat);
        
        geom.setLocalTranslation(koordinat);
        this.koordinat = koordinat;
    }
    
    public void attachCharacter()
    {
        for(int i = 0 ; i < hero.size() ; i++)
        {
            bulletAppState.getPhysicsSpace().add(hero.get(i).player);
            rootNode.attachChild(hero.get(i).model);
        }
    }
    
    public void summon() 
    {
        hero.add(new ModelCharacter(assetManager, "Models/Sinbad/Sinbad.mesh.xml"));
       
        koordinat.y+=100;
        
        bulletAppState.getPhysicsSpace().add(hero.lastElement().player);
        rootNode.attachChild(hero.lastElement().model);
        //attachCharacter();
    }

    public void bind(Nifty nifty, Screen screen) 
    {
        this.nifty = nifty;
    }

    public void onStartScreen() 
    {
        
    }

    public void onEndScreen() 
    {
        
    }
    
    public ModelCharacter findActiveHero()
    {
        for(int i = 0 ; i < hero.size() ; i++)
        {
            if(hero.get(i).isPicked)
            {
                return hero.get(i);
            }
        }
        return null;
    }
    
    ModelCharacter currHero;
    
    public void moveMenu()
    {
        currHero = findActiveHero();
        currHero.isMove = true;
        generateMovement(currHero.model.getLocalTranslation().clone(), 1);
    }
    
    public void attackMenu()
    {
        currHero = findActiveHero();
        currHero.attacking = true;
        currHero.healing = false;
        generateMovement(currHero.model.getLocalTranslation().clone(), 3);
    }
    
    public void healMenu()
    {
        currHero = findActiveHero();
        currHero.healing = true;
        currHero.attacking = false;
        generateMovement(currHero.model.getLocalTranslation().clone(), 3);
    }
    
    public void endOfMenu()
    {
        currHero = findActiveHero();
        nifty.gotoScreen("none");
        currHero.isPicked = false;
        currHero.isMove = false;
        currHero.attacking = false;
        currHero.healing = false;
        
        //currHero.clearNode(actionNode);
        //rootNode.detachChild(actionNode);
        currHero.channel.setAnim("IdleBase");
        floor.detachAllChildren();
    }
    
    public void endOfPhase()
    {
        playerPhase = false;
        heroEndPhase();
    }
}
