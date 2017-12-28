package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.PacketDescription;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISemiBlock {

    World getWorld();

    BlockPos getPos();
    
    int getIndex();

    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    void update();

    void initialize(World world, BlockPos pos);

    void invalidate();

    boolean isInvalid();

    void addDrops(NonNullList<ItemStack> drops);

    boolean canPlace();

    void onPlaced(EntityPlayer player, ItemStack stack);

    boolean onRightClickWithConfigurator(EntityPlayer player);
    
    void onSemiBlockRemovedFromThisPos(ISemiBlock semiBlock);
    
    default boolean canCoexistInSameBlock(ISemiBlock semiBlock){
        return false;
    }

    PacketDescription getDescriptionPacket();
}
