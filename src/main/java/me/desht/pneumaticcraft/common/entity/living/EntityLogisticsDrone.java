package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityLogisticsDrone extends EntityDrone {

    public EntityLogisticsDrone(World world) {
        super(world);
    }

    public EntityLogisticsDrone(World world, EntityPlayer player) {
        super(world, player);
    }

    @Override
    protected ItemStack getDroppedStack() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("currentAir", currentAir);
        tag.setInteger("color", getDroneColor());
        NBTTagCompound invTag = new NBTTagCompound();
        writeEntityToNBT(invTag);
        tag.setTag("UpgradeInventory", invTag.getTag("Inventory"));
        ItemStack drone = new ItemStack(Itemss.LOGISTICS_DRONE);
        drone.setTagCompound(tag);
        return drone;
    }
}
