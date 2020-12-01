package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTransferGadget extends EntitySemiblockBase implements IDirectionalSemiblock {
    private static final int TRANSFER_INTERVAL = 40;

    private static final double INDENT = 1/16D;
    private static final double THICKNESS = 1/32D;
    private static final double ANTI_Z_FIGHT = 0.001D;

    private static final DataParameter<Integer> IO_MODE = EntityDataManager.createKey(EntityTransferGadget.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SIDE = EntityDataManager.createKey(EntityTransferGadget.class, DataSerializers.VARINT);

    private int counter;
//    public Vector3d renderingOffset;

    public EntityTransferGadget(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {
        super.registerData();

        getDataManager().register(IO_MODE, IOMode.OUTPUT.ordinal());
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

        setIOMode(IOMode.OUTPUT);
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

    public IOMode getIOMode() {
        return IOMode.values()[getDataManager().get(IO_MODE)];
    }

    private void setIOMode(IOMode mode) {
        getDataManager().set(IO_MODE, mode.ordinal());
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        counter = compound.getInt("counter");
        setSide(Direction.byIndex(compound.getByte("facing")));
        setIOMode(compound.getBoolean("input") ? IOMode.INPUT : IOMode.OUTPUT);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.putInt("counter", counter);
        compound.putByte("facing", (byte) getSide().getIndex());
        compound.putBoolean("input", getIOMode() == IOMode.INPUT);
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
        curInfo.add(xlate(getIOMode().getTranslationKey()));
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
            if (getIOMode() == IOMode.OUTPUT) {
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

    public enum IOMode implements ITranslatableEnum {
        INPUT(Textures.MODEL_TRANSFER_GADGET_IN),
        OUTPUT(Textures.MODEL_TRANSFER_GADGET_OUT);

        private final ResourceLocation texture;

        IOMode(ResourceLocation texture) {
            this.texture = texture;
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        IOMode toggle() {
            return this == INPUT ? OUTPUT : INPUT;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.transfer_gadget.io_mode." + toString().toLowerCase(Locale.ROOT);
        }
    }
}
