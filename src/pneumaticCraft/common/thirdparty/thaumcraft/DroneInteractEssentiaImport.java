package pneumaticCraft.common.thirdparty.thaumcraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.drone.IBlockInteractHandler;
import pneumaticCraft.api.drone.ICustomBlockInteract;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.lib.Textures;
import thaumcraft.api.aspects.IAspectContainer;

public class DroneInteractEssentiaImport implements ICustomBlockInteract{

    @Override
    public String getName(){
        return "essentiaImport";
    }

    @Override
    public ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ESSENTIA_IM;
    }

    @Override
    public boolean doInteract(ChunkPosition pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate){
        TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(te instanceof IAspectContainer) {
            IAspectContainer container = (IAspectContainer)te;

        }
        return false;
    }

    @Override
    public int getCraftingColorIndex(){
        // TODO Auto-generated method stub
        return 0;
    }

}
