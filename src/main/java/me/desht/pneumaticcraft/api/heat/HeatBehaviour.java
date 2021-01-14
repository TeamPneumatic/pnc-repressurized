package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Supplier;

/**
 * You can extend this class, and register it via
 * {@link HeatRegistrationEvent#registerHeatBehaviour(ResourceLocation, Supplier)} )}
 * <p>
 * This can be used to add heat dependent logic to non-TE's or blocks you don't have access to. For example,
 * PneumaticCraft uses this to power Furnaces with heat, and to turn Lava into Obsidian when heat is drained.
 * This only works for ticking heat logic, not for static heat sources like lava blocks.
 * <p>
 * For general blockstate transitions, datapacks are the preferred way to add custom heat behaviours. See
 * {@code data/pneumaticcraft/pneumaticcraft/block_heat_properties/}
 */
public abstract class HeatBehaviour<T extends TileEntity> implements INBTSerializable<CompoundNBT> {
    private IHeatExchangerLogic connectedHeatLogic;
    private World world;
    private BlockPos pos;
    private T cachedTE;
    private BlockState blockState;
    private Direction direction;  // direction of this behaviour, from the tile entity's point of view

    /**
     * This method is called by the connected {@link IHeatExchangerLogic} when it initialises itself as a hull
     * heat exchanger; this happens when the owning tile entity gets a neighbor block update.  You can override
     * and extend this method, but <strong>be sure to call the super method</strong>!
     * @param connectedHeatLogic the connected heat exchanger logic
     * @param world the world
     * @param pos block pos of the owning tile entity
     * @param direction direction of this behaviour (from the tile entity's point of view)
     */
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        this.cachedTE = null;
        this.blockState = null;
        return this;
    }

    public IHeatExchangerLogic getHeatExchanger() {
        return connectedHeatLogic;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    public T getTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved()) //noinspection unchecked
            cachedTE = (T) world.getTileEntity(pos);
        return cachedTE;
    }

    public BlockState getBlockState() {
        if (blockState == null) blockState = world.getBlockState(pos);
        return blockState;
    }

    /**
     * Unique id for this behaviour, also used in NBT saving.
     *
     * @return a unique ID
     */
    public abstract ResourceLocation getId();

    /**
     * Return true when this heat behaviour is applicable for this coordinate. World access methods can be used here
     * (getWorld(), getPos(), getBlockState(), getTileEntity()).
     *
     * @return true if this behaviour is applicable here
     */
    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void tick();

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("BlockPos", NBTUtil.writeBlockPos(pos));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        pos = NBTUtil.readBlockPos(nbt.getCompound("BlockPos"));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeatBehaviour) {
            HeatBehaviour<?> behaviour = (HeatBehaviour<?>) o;
            return behaviour.getId().equals(getId()) && behaviour.getPos().equals(getPos());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = getId().hashCode();
        i = i * 31 + getPos().hashCode();
        return i;
    }
}
