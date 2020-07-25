package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTransferGadget extends EntitySemiblockBase implements IDirectionalSemiblock {
    private static final int TRANSFER_INTERVAL = 40;

    private static final double INDENT = 1/16D;
    private static final double THICKNESS = 1/32D;
    private static final double ANTI_Z_FIGHT = 0.001D;

    private static final DataParameter<Integer> IO_MODE = EntityDataManager.createKey(EntityTransferGadget.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SIDE = EntityDataManager.createKey(EntityTransferGadget.class, DataSerializers.VARINT);

    public enum EnumInputOutput {
        INPUT,
        OUTPUT;

        EnumInputOutput toggle() {
            return this == INPUT ? OUTPUT : INPUT;
        }
    }

    private int counter;
    public Vector3d renderingOffset;

    public EntityTransferGadget(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {
        super.registerData();

        getDataManager().register(IO_MODE, EnumInputOutput.OUTPUT.ordinal());
        getDataManager().register(SIDE, Direction.UP.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote && isAlive()  && ++counter >= TRANSFER_INTERVAL) {
            counter = 0;
            doTransfer();
        }
    }

    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
        super.onPlaced(player, stack, facing);

        setIOMode(EnumInputOutput.OUTPUT);
        setSide(facing);
    }

    @Override
    public boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
        if (getSide() == side) {
            setIOMode(getIOMode().toggle());
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            return true;
        } else {
            return super.onRightClickWithConfigurator(player, side);
        }
    }

    @Override
    public boolean canStay() {
        return canPlace(getSide());
    }

    @Override
    public boolean canPlace(Direction facing) {
        TileEntity te = getCachedTileEntity();
        return te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent();
    }

    @Override
    public Direction getSide() {
        return Direction.values()[getDataManager().get(SIDE)];
    }

    @Override
    public void setSide(Direction facing) {
        getDataManager().set(SIDE, facing.ordinal());
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> param) {
        if (param == SIDE) renderingOffset = calcRenderingOffset(getBoundingBox(), getSide());
    }

    private Vector3d calcRenderingOffset(AxisAlignedBB aabb, Direction d) {
        double xl = aabb.getXSize() / 2;
        double zl = aabb.getZSize() / 2;

        switch (d) {
            case DOWN: return new Vector3d(0, -THICKNESS, 0);
            case UP: return new Vector3d(0, aabb.getYSize(), 0);
            case NORTH: return new Vector3d(-THICKNESS / 2, -THICKNESS / 2, -zl);
            case SOUTH: return new Vector3d(0, -THICKNESS / 2, zl);
            case WEST: return new Vector3d(-xl - THICKNESS, -THICKNESS / 2, 0);
            case EAST: return new Vector3d(xl, -THICKNESS / 2, 0);
            default: throw new IllegalArgumentException();
        }
    }

    public EnumInputOutput getIOMode() {
        return EnumInputOutput.values()[getDataManager().get(IO_MODE)];
    }

    private void setIOMode(EnumInputOutput mode) {
        getDataManager().set(IO_MODE, mode.ordinal());
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        counter = compound.getInt("counter");
        setSide(Direction.byIndex(compound.getByte("facing")));
        setIOMode(compound.getBoolean("input") ? EnumInputOutput.INPUT : EnumInputOutput.OUTPUT);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.putInt("counter", counter);
        compound.putByte("facing", (byte) getSide().getIndex());
        compound.putBoolean("input", getIOMode() == EnumInputOutput.INPUT);
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
    }

    @Override
    public AxisAlignedBB getBlockBounds() {
        AxisAlignedBB b = super.getBlockBounds();
        switch (getSide()) {
            case UP:
                return new AxisAlignedBB(b.minX - THICKNESS, b.maxY - INDENT, b.minZ - THICKNESS,
                        b.maxX + THICKNESS, b.maxY + THICKNESS, b.maxZ + THICKNESS);
            case DOWN:
                return new AxisAlignedBB(b.minX - THICKNESS, 0 - ANTI_Z_FIGHT, b.minZ - THICKNESS,
                        b.maxX + THICKNESS, b.minY + INDENT, b.maxZ + THICKNESS);
            case NORTH:
                return new AxisAlignedBB(b.minX - THICKNESS, b.minY - THICKNESS, 0 - ANTI_Z_FIGHT,
                        b.maxX + THICKNESS, b.maxY + THICKNESS, b.minZ + INDENT);
            case SOUTH:
                return new AxisAlignedBB(b.minX - THICKNESS, b.minY - THICKNESS, b.maxZ - INDENT,
                        b.maxX + THICKNESS, b.maxY + THICKNESS, 1 + ANTI_Z_FIGHT);
            case WEST:
                return new AxisAlignedBB(0 - ANTI_Z_FIGHT, b.minY - THICKNESS, b.minZ - THICKNESS,
                        b.minX + INDENT, b.maxY + THICKNESS, b.maxZ + THICKNESS);
            case EAST:
                return new AxisAlignedBB(b.maxX - INDENT, b.minY - THICKNESS, b.minZ - THICKNESS,
                        1 + ANTI_Z_FIGHT, b.maxY + THICKNESS, b.maxZ + THICKNESS);
            default:
                return b;
        }
    }

    private void doTransfer() {
        // TODO capability caching
        TileEntity inputTE = getCachedTileEntity();
        TileEntity outputTE = world.getTileEntity(getBlockPos().offset(getSide()));
        if (inputTE != null && outputTE != null) {
            if (getIOMode() == EnumInputOutput.OUTPUT) {
                tryTransferItem(inputTE, outputTE);
                tryTransferFluid(inputTE, outputTE);
            } else {
                tryTransferItem(outputTE, inputTE);
                tryTransferFluid(outputTE, inputTE);
            }
        }
    }

    private void tryTransferItem(TileEntity inputTE, TileEntity outputTE) {
        inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getSide())
                .ifPresent(input -> outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getSide().getOpposite())
                        .ifPresent(output -> IOHelper.transferOneItem(input, output)));
    }

    private void tryTransferFluid(TileEntity inputTE, TileEntity outputTE) {
        inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getSide())
                .ifPresent(input -> outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getSide().getOpposite())
                        .ifPresent(output -> FluidUtil.tryFluidTransfer(output, input, 100, true)));
    }
}
