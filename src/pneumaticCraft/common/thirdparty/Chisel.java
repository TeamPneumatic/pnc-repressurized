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

        if(modClass == null) {
            try {
                modClass = Class.forName("team.chisel.Chisel");
            } catch(ClassNotFoundException e) {

            }
        }

        if(modClass != null) {
            try {
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "renderEldritchId").getInt(null));
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "renderLayeredId").getInt(null));
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "renderGlowId").getInt(null));
                PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(modClass, "roadLineId").getInt(null));
            } catch(Throwable e) {
                Log.error("Chisel reflection failed:");
                e.printStackTrace();
            }
        } else {
            Log.error("Neither Chisel 1 or Chisel 2's main mod class could be found, even though chisel is in the instance. Report to MineMaarten plax!");
        }

        try {
            Class ctmlibClass = Class.forName("com.cricketcraft.ctmlib.ClientUtils");
            PneumaticRegistry.getInstance().registerConcealableRenderId(ReflectionHelper.findField(ctmlibClass, "renderCTMId").getInt(null));
        } catch(ClassNotFoundException e) {
            Log.error("Chisel's ClientUtils inside ctmlib couldn't be found! No support for connected texture blocks for camouflage is given");
        } catch(Throwable e) {
            Log.error("Chisel reflection failed, No support for connected texture blocks for camouflage is given. Stacktrace:");
            e.printStackTrace();
        }
    }

    @Override
    public void clientSide(){

    }

    @Override
    public void clientInit(){}

}
