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
        Class modClass = null;
        try {
            modClass = Class.forName("info.jbcs.minecraft.chisel.Chisel");
        } catch(ClassNotFoundException e) {

        }

        if(modClass == null) {
            try {
                modClass = Class.forName("com.cricketcraft.chisel.Chisel");
            } catch(ClassNotFoundException e) {

            }
        }

        if(modClass != null) {
            try {
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderCTMId", "renderCTMId").getInt(null));
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderEldritchId", "renderEldritchId").getInt(null));
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "RenderCarpetId", "renderCarpetId").getInt(null));
            } catch(Throwable e) {
                Log.error("Chisel reflection failed:");
                e.printStackTrace();
            }
        } else {
            Log.error("Neither Chisel 1 or Chisel 2's main mod class could be found, even though chisel is in the instance. Report to MineMaarten plax!");
        }
    }

    @Override
    public void clientSide(){

    }

    @Override
    public void clientInit(){}

}
