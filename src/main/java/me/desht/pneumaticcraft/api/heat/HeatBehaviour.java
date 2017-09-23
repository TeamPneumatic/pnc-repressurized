package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Extend this class, and register it via {@link PneumaticRegistry.getInstance().registerHeatBehaviour()}
 * This can be used to add heat dependent logic to non-TE's or blocks you don't have access to. PneumaticCraft uses this to power Furnaces with heat,
 * and to turn Lava into Obsidian when heat is drained. This only works for ticking heat logic, so not for static heat sources like lava blocks.
 */
public abstract class HeatBehaviour<Tile extends TileEntity> {

    private IHeatExchangerLogic connectedHeatLogic;
    private World world;
    private BlockPos pos;
    private Tile cachedTE;
    private IBlockState blockState;

    /**
     * Called by the connected IHeatExchangerLogic.
     *
     * @param connectedHeatLogic
     * @param world
     * @param pos
     */
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos) {
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.pos = pos;
        cachedTE = null;
        blockState = null;
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

    public Tile getTileEntity() {
        if (cachedTE == null || cachedTE.isInvalid()) cachedTE = (Tile) world.getTileEntity(pos);
        return cachedTE;
    }

    public IBlockState getBlockState() {
        if (blockState == null) blockState = world.getBlockState(pos);
        return blockState;
    }

    /**
     * Unique id for this behaviour. Used in NBT saving. I recommend prefixing it with your modid.
     *
     * @return
     */
    public abstract String getId();

    /**
     * Return true when this heat behaviour is applicable for this coordinate. World access methods can be used here (getWorld(), getX(), getY(), getZ(), getBlock(), getTileEntity()).
     *
     * @return
     */
    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void update();

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
    }

    public void readFromNBT(NBTTagCompound tag) {
        pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeatBehaviour) {
            HeatBehaviour behaviour = (HeatBehaviour) o;
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
