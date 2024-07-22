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

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Locale;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TransferGadgetEntity extends AbstractSemiblockEntity implements IDirectionalSemiblock {
    private static final int TRANSFER_INTERVAL = 40;

    private static final double INDENT = 1/16D;
    private static final double THICKNESS = 1/32D;
    private static final double ANTI_Z_FIGHT = 0.001D;

    private static final EntityDataAccessor<Integer> IO_MODE = SynchedEntityData.defineId(TransferGadgetEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(TransferGadgetEntity.class, EntityDataSerializers.INT);

    private int counter;

    public TransferGadgetEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(IO_MODE, IOMode.OUTPUT.ordinal());
        builder.define(SIDE, Direction.UP.get3DDataValue());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && isAlive()  && ++counter >= TRANSFER_INTERVAL) {
            counter = 0;
            doTransfer();
        }
    }

    @Override
    public void onPlaced(Player player, ItemStack stack, Direction facing) {
        super.onPlaced(player, stack, facing);

        setIOMode(IOMode.OUTPUT);
        setSide(facing);
    }

    @Override
    public boolean onRightClickWithConfigurator(Player player, Direction side) {
        if (getSide() == side) {
            toggle(player);
            return true;
        } else {
            return super.onRightClickWithConfigurator(player, side);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        // since this is a cheap early game item, let's allow toggling with empty hand
        // not force the player to craft & charge up a logistics configurator
        if (player.getItemInHand(hand).isEmpty()) {
            toggle(player);
            return InteractionResult.SUCCESS;
        } else {
            return super.interactAt(player, hitVec, hand);
        }
    }

    private void toggle(Player player) {
        setIOMode(getIOMode().toggle());
        player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
    }

    @Override
    public boolean canStay() {
        return canPlace(getSide());
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockEntity te = getCachedTileEntity();
        return te != null &&
                (IOHelper.getInventoryForBlock(te, facing).isPresent() || IOHelper.getFluidHandlerForBlock(te, facing).isPresent());
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
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        counter = compound.getInt("counter");
        setSide(Direction.from3DDataValue(compound.getByte("facing")));
        setIOMode(compound.getBoolean("input") ? IOMode.INPUT : IOMode.OUTPUT);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("counter", counter);
        compound.putByte("facing", (byte) getSide().get3DDataValue());
        compound.putBoolean("input", getIOMode() == IOMode.INPUT);
    }

    @Override
    public void addTooltip(Consumer<Component> curInfo, Player player, CompoundTag tag, boolean extended) {
        curInfo.accept(xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
        curInfo.accept(xlate(getIOMode().getTranslationKey()));
    }

    @Override
    protected AABB calculateBlockBounds() {
        AABB b = super.calculateBlockBounds();
        return switch (getSide()) {
            case UP -> new AABB(b.minX - THICKNESS, b.maxY - INDENT, b.minZ - THICKNESS,
                    b.maxX + THICKNESS, b.maxY + THICKNESS, b.maxZ + THICKNESS);
            case DOWN -> new AABB(b.minX - THICKNESS, 0 - ANTI_Z_FIGHT, b.minZ - THICKNESS,
                    b.maxX + THICKNESS, b.minY + INDENT, b.maxZ + THICKNESS);
            case NORTH -> new AABB(b.minX - THICKNESS, b.minY - THICKNESS, 0 - ANTI_Z_FIGHT,
                    b.maxX + THICKNESS, b.maxY + THICKNESS, b.minZ + INDENT);
            case SOUTH -> new AABB(b.minX - THICKNESS, b.minY - THICKNESS, b.maxZ - INDENT,
                    b.maxX + THICKNESS, b.maxY + THICKNESS, 1 + ANTI_Z_FIGHT);
            case WEST -> new AABB(0 - ANTI_Z_FIGHT, b.minY - THICKNESS, b.minZ - THICKNESS,
                    b.minX + INDENT, b.maxY + THICKNESS, b.maxZ + THICKNESS);
            case EAST -> new AABB(b.maxX - INDENT, b.minY - THICKNESS, b.minZ - THICKNESS,
                    1 + ANTI_Z_FIGHT, b.maxY + THICKNESS, b.maxZ + THICKNESS);
        };
    }

    private void doTransfer() {
        // TODO capability caching
        BlockEntity inputTE = getCachedTileEntity();
        Direction side = getSide();
        Direction otherSide = getSide().getOpposite();
        BlockEntity outputTE = level().getBlockEntity(getBlockPos().relative(side));
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

    private void tryTransferItem(BlockEntity inputTE, BlockEntity outputTE, Direction side, Direction otherSide) {
        IOHelper.getInventoryForBlock(inputTE, side)
                .ifPresent(input -> IOHelper.getInventoryForBlock(outputTE, otherSide)
                        .ifPresent(output -> IOHelper.transferOneItem(input, output)));
    }

    private void tryTransferFluid(BlockEntity inputTE, BlockEntity outputTE, Direction side, Direction otherSide) {
        IOHelper.getFluidHandlerForBlock(inputTE, side)
                .ifPresent(input -> IOHelper.getFluidHandlerForBlock(outputTE, otherSide)
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
