package k4unl.minecraft.Hydraulicraft.api;

import java.lang.reflect.Constructor;

import net.minecraft.tileentity.TileEntity;
import codechicken.multipart.TMultiPart;

/**
 * 
 * @author minemaarten
 * Original idea and class from PneumaticCraft.
 * Modified by K-4U for Hydraulicraft.
 */
public class HydraulicBaseClassSupplier {
	private static Constructor baseHandlerConstructor;
	private static Constructor multipartConstructor;
	
    public static IBaseClass getBaseClass(TileEntity target, PressureTier pTier, int maxStorage){
    	IBaseClass baseClassEntity = null;
        try {
            if(baseHandlerConstructor == null) baseHandlerConstructor = Class.forName("k4unl.minecraft.Hydraulicraft.tileEntities.TileHydraulicBase").getConstructor(PressureTier.class, int.class);
            baseClassEntity = (IBaseClass)baseHandlerConstructor.newInstance(pTier, maxStorage);
            baseClassEntity.init(target);
        } catch(Exception e) {
            System.err.println("[Hydraulicraft API] An error has occured whilst trying to get a base class. Here's a stacktrace:");
            e.printStackTrace();
        }
    	
        return baseClassEntity;
    }

    public static IBaseClass getBaseClass(TMultiPart target, PressureTier pTier, int maxStorage){
    	IBaseClass baseClassEntity = null;
        try {
            if(multipartConstructor == null) multipartConstructor = Class.forName("k4unl.minecraft.Hydraulicraft.tileEntities.TileHydraulicBase").getConstructor(PressureTier.class, int.class);
            baseClassEntity = (IBaseClass)multipartConstructor.newInstance(pTier, maxStorage);
            baseClassEntity.init(target);
        } catch(Exception e){
        	System.err.println("[Hydraulicraft API] An error has occured whilst trying to get a base class. Here's a stacktrace:");
            e.printStackTrace();
        }
        return baseClassEntity;
    }
    

}
