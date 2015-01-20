package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import cofh.api.energy.EnergyStorage;

public class ExtendedPropertyRF implements IExtendedEntityProperties{

    public EnergyStorage energy = new EnergyStorage(Integer.MAX_VALUE);

    @Override
    public void saveNBTData(NBTTagCompound compound){
        energy.writeToNBT(compound);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound){
        energy.readFromNBT(compound);
    }

    @Override
    public void init(Entity entity, World world){}

}
