package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.PacketDescription;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;

public interface ISemiBlock {

    World getWorld();

    BlockPos getPos();
    
    int getIndex();

    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    void update();

    void initialize(World world, BlockPos pos);
    
    void prePlacement(EntityPlayer player, ItemStack stack, EnumFacing facing);

    void invalidate();

    boolean isInvalid();

    void addDrops(NonNullList<ItemStack> drops);

    boolean canPlace(EnumFacing facing);

    void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing);

    boolean onRightClickWithConfigurator(EntityPlayer player, EnumFacing side);
    
    void onSemiBlockRemovedFromThisPos(ISemiBlock semiBlock);
    
    default boolean canCoexistInSameBlock(ISemiBlock semiBlock){
        return false;
    }

    PacketDescription getDescriptionPacket();

    EnumGuiId getGuiID();
}
