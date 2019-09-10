
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import javax.swing.JOptionPane;

public class MainMenu extends AbstractAppState implements ScreenController
{
    Nifty nifty;
    Main app;

    public Nifty getNifty() {
        return this.nifty;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) 
    {
        super.initialize(stateManager, app);
        this.app = (Main)app;
    }
    
    public void newGame()
    {
        nifty.gotoScreen("game");
    }

    public void option()
    {
        nifty.gotoScreen("option");
    }
    
    public void summonRed()
    {
        app.summonControl = true;
        app.isSummon=true;
    }
    
    public void summonGreen()
    {
        app.summonControl = true;
        app.isSummon=true;
    }
    
    public void playing()
    {
        app.isPlaying = true;
        nifty.gotoScreen("none");
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
}
