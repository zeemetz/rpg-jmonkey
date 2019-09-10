
package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class ModelCharacterControl extends AbstractControl
{
    @Override
    protected void controlUpdate(float tpf) 
    {
        System.out.println("control");
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) 
    {
        
    }

    public Control cloneForSpatial(Spatial spatial) 
    {
        return null;
    }
    
}
