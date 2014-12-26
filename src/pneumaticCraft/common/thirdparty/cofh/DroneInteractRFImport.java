package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.entity.EntityCreature;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.drone.IBlockInteractHandler;
import pneumaticCraft.api.drone.ICustomBlockInteract;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyStorage;

public class DroneInteractRFImport implements ICustomBlockInteract{

    @Override
    public String getName(){
        return "rfImport";
    }

    @Override
    public ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_RF_IM;
    }

    @Override
    public boolean doInteract(ChunkPosition pos, EntityCreature drone, IBlockInteractHandler interactHandler, boolean simulate){
        TileEntity te = drone.worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(te instanceof IEnergyProvider) {
            IEnergyProvider provider = (IEnergyProvider)te;
            IEnergyStorage droneEnergy = CoFHCore.getEnergyStorage(drone);
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                if(interactHandler.getSides()[d.ordinal()]) {
                    int transferedEnergy = droneEnergy.receiveEnergy(provider.extractEnergy(d, Math.min(1000, interactHandler.useCount() ? interactHandler.getRemainingCount() : Integer.MAX_VALUE), true), true);
                    if(transferedEnergy > 0) {
                        if(!simulate) {
                            interactHandler.decreaseCount(transferedEnergy);
                            droneEnergy.receiveEnergy(transferedEnergy, false);
                            provider.extractEnergy(d, transferedEnergy, false);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.RAIN_PLANT_DAMAGE;
    }
}
