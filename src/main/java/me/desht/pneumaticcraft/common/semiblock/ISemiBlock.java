package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.PacketDescription;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ISemiBlock {

    /**
     * Get an ID for this semiblock, which should match the corresponding item's registry name.
     * @return a semiblock ID
     */
    ResourceLocation getId();

    World getWorld();

    BlockPos getPos();

    default Vec3d getCentrePos() {
        return new Vec3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
    }
    
    int getIndex();

    void writeToNBT(CompoundNBT tag);

    void readFromNBT(CompoundNBT tag);

    void tick();

    void initialize(World world, BlockPos pos);
    
    void prePlacement(PlayerEntity player, ItemStack stack, Direction facing);

    void invalidate();

    boolean isInvalid();

    void addDrops(NonNullList<ItemStack> drops);

    boolean canPlace(Direction facing);

    void onPlaced(PlayerEntity player, ItemStack stack, Direction facing);

    boolean onRightClickWithConfigurator(PlayerEntity player, Direction side);
    
    void onSemiBlockRemovedFromThisPos(ISemiBlock semiBlock);
    
    default boolean canCoexistInSameBlock(ISemiBlock semiBlock){
        return false;
    }

    PacketDescription getDescriptionPacket();
}
