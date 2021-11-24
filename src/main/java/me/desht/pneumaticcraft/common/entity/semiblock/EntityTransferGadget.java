/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
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

    private static final DataParameter<Integer> IO_MODE = EntityDataManager.defineId(EntityTransferGadget.class, DataSerializers.INT);
    private static final DataParameter<Integer> SIDE = EntityDataManager.defineId(EntityTransferGadget.class, DataSerializers.INT);

    private int counter;

    public EntityTransferGadget(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        getEntityData().define(IO_MODE, IOMode.OUTPUT.ordinal());
        getEntityData().define(SIDE, Direction.UP.get3DDataValue());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide && isAlive()  && ++counter >= TRANSFER_INTERVAL) {
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
            toggle(player);
            return true;
        } else {
            return super.onRightClickWithConfigurator(player, side);
        }
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hitVec, Hand hand) {
        // since this is a cheap early game item, let's allow toggling with empty hand
        // not force the player to craft & charge up a logistics configurator
        if (player.getItemInHand(hand).isEmpty()) {
            toggle(player);
            return ActionResultType.SUCCESS;
        } else {
            return super.interactAt(player, hitVec, hand);
        }
    }

    private void toggle(PlayerEntity player) {
        setIOMode(getIOMode().toggle());
        player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
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
        return Direction.values()[getEntityData().get(SIDE)];
    }

    @Override
    public void setSide(Direction facing) {
        getEntityData().set(SIDE, facing.get3DDataValue());
    }

    public IOMode getIOMode() {
        return IOMode.values()[getEntityData().get(IO_MODE)];
    }

    private void setIOMode(IOMode mode) {
        getEntityData().set(IO_MODE, mode.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);

        counter = compound.getInt("counter");
        setSide(Direction.from3DDataValue(compound.getByte("facing")));
        setIOMode(compound.getBoolean("input") ? IOMode.INPUT : IOMode.OUTPUT);
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("counter", counter);
        compound.putByte("facing", (byte) getSide().get3DDataValue());
        compound.putBoolean("input", getIOMode() == IOMode.INPUT);
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
        curInfo.add(xlate(getIOMode().getTranslationKey()));
    }

    @Override
    protected AxisAlignedBB calculateBlockBounds() {
        AxisAlignedBB b = super.calculateBlockBounds();
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
        Direction side = getSide();
        Direction otherSide = getSide().getOpposite();
        TileEntity outputTE = level.getBlockEntity(getBlockPos().relative(side));
        if (inputTE != null && outputTE != null) {
            if (getIOMode() == IOMode.OUTPUT) {
                tryTransferItem(inputTE, outputTE, side, otherSide);
                tryTransferFluid(inputTE, outputTE, side, otherSide);
            } else {
                tryTransferItem(outputTE, inputTE, otherSide, side);
                tryTransferFluid(outputTE, inputTE, otherSide, side);
            }
        }
    }

    private void tryTransferItem(TileEntity inputTE, TileEntity outputTE, Direction side, Direction otherSide) {
        inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
                .ifPresent(input -> outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, otherSide)
                        .ifPresent(output -> IOHelper.transferOneItem(input, output)));
    }

    private void tryTransferFluid(TileEntity inputTE, TileEntity outputTE, Direction side, Direction otherSide) {
        inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
                .ifPresent(input -> outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, otherSide)
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
