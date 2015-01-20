package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.proxy.ClientProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class OpenComputers implements IThirdParty{
    public Block droneInterface;

    @Override
    public void preInit(){
        droneInterface = new BlockDroneInterface(Material.iron).setBlockName("droneInterface");
        Blockss.registerBlock(droneInterface);
        GameRegistry.registerTileEntity(TileEntityDroneInterface.class, "droneInterface");
        TileEntityProgrammer.registeredWidgets.add(new ProgWidgetCC());
    }

    @Override
    public void init(){

    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){
        ClientProxy.registerBaseModelRenderer(droneInterface, TileEntityDroneInterface.class, new ModelDroneInterface());
    }

    @Override
    public void clientInit(){}

}
