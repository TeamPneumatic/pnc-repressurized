package pneumaticCraft.common.thirdparty;

import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class Chisel implements IThirdParty{

    @Override
    public void preInit(){

    }

    @Override
    public void init(){

    }

    @Override
    public void postInit(){
        try {
            Class modClass = Class.forName("info.jbcs.minecraft.chisel.Chisel");
            PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderCTMId", "renderCTMId").getInt(null));
            PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderEldritchId", "renderEldritchId").getInt(null));
            PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderCarpetId", "renderCarpetId").getInt(null));
        } catch(Throwable e) {
            Log.error("Chisel reflection failed:");
            e.printStackTrace();
        }
    }

    @Override
    public void clientSide(){

    }

}
