
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import javax.swing.JOptionPane;

public class OptionMenu extends AbstractAppState implements ScreenController
{
    Main app;
    Nifty nifty;
    AppSettings appSet = new AppSettings(true);
    
    public void fullScreen()
    {
        appSet.setFullscreen(true);
        app.setSettings(appSet);
        app.restart();
    }
    
    public void normalScreen()
    {
        appSet.setFullscreen(false);
        app.setSettings(appSet);
        app.restart();
    }
    
    public void back()
    {
        nifty.gotoScreen("start");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) 
    {
        super.initialize(stateManager, app);
        this.app = (Main)app;
        
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
