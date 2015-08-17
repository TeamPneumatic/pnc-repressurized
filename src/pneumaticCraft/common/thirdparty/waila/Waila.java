package pneumaticCraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.tileentity.IRedstoneControl;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;

public class Waila implements IThirdParty{

    @Override
    public void preInit(){}

    @Override
    public void init(){
        FMLInterModComms.sendMessage("Waila", "register", "pneumaticCraft.common.thirdparty.waila.Waila.callbackRegister");
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){

    }

    public static void callbackRegister(IWailaRegistrar registrar){
        registrar.registerBodyProvider(new WailaPneumaticHandler(), IPneumaticMachine.class);
        registrar.registerBodyProvider(new WailaHeatHandler(), IHeatExchanger.class);
        registrar.registerBodyProvider(new WailaPlantHandler(), BlockPneumaticPlantBase.class);
        registrar.registerBodyProvider(new WailaSemiBlockHandler(), Block.class);
        registrar.registerBodyProvider(new WailaRedstoneControl(), IRedstoneControl.class);
        registrar.registerBodyProvider(new WailaTubeModuleHandler(), TileEntityPressureTube.class);
        registrar.registerNBTProvider(new WailaPneumaticHandler(), IPneumaticMachine.class);
        registrar.registerNBTProvider(new WailaHeatHandler(), IHeatExchanger.class);
        registrar.registerNBTProvider(new WailaTubeModuleHandler(), TileEntityPressureTube.class);
        registrar.registerNBTProvider(new WailaSemiBlockHandler(), Block.class);
        registrar.registerNBTProvider(new WailaRedstoneControl(), IRedstoneControl.class);

        if(Loader.isModLoaded(ModIds.FMP)) {
            registrar.registerBodyProvider(new WailaFMPHandler(), "tile.pressureTube");
            registrar.registerBodyProvider(new WailaFMPHandler(), "tile.advancedPressureTube");
        }
    }

    @Override
    public void clientInit(){}
}
