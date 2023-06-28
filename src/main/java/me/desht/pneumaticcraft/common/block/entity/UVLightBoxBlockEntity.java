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

package me.desht.pneumaticcraft.common.block.entity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.UVLightBoxBlock;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.UVLightBoxMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class UVLightBoxBlockEntity extends AbstractAirHandlingBlockEntity implements
        IMinWorkingPressure, IRedstoneControl<UVLightBoxBlockEntity>, MenuProvider /*,ILightProvider */ {
    private static final String NBT_EXPOSURE = "pneumaticcraft:uv_exposure";

    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_SLOT = 0;

    private static final List<RedstoneMode<UVLightBoxBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("standard.always", new ItemStack(Items.GUNPOWDER),
                    te -> true),
            new ReceivingRedstoneMode<>("standard.high_signal", new ItemStack(Items.REDSTONE),
                    te -> te.getCurrentRedstonePower() > 0),
            new ReceivingRedstoneMode<>("standard.low_signal", new ItemStack(Items.REDSTONE_TORCH),
                    te -> te.getCurrentRedstonePower() == 0),
            new ReceivingRedstoneMode<>("uvLightBox.interpolate", new ItemStack(Items.COMPARATOR),
                    te -> te.getCurrentRedstonePower() > 0)
    );
    public static final int RS_MODE_INTERPOLATE = 3;

    // avoid rapid blockstate switching, which is a framerate killer
    private long lastStateUpdate = 0;
    private BlockState pendingState;

    @GuiSynced
    public final RedstoneController<UVLightBoxBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @GuiSynced
    public int threshold = 100;

    private final UVInputHandler inputHandler = new UVInputHandler();
    private final ItemStackHandler outputHandler = new BaseItemStackHandler(this, INVENTORY_SIZE);
    private final UVInvWrapper inventoryExt = new UVInvWrapper();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventoryExt);
    private LazyOptional<IItemHandler> cachedEjectHandler = LazyOptional.empty();

    public int ticksExisted;

    public UVLightBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UV_LIGHT_BOX.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_UV_LIGHTBOX, 4);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        ticksExisted++;
        ItemStack stack = getLoadedPCB();
        boolean didWork = false;
        if (rsController.getCurrentMode() == RS_MODE_INTERPOLATE) {
            threshold = Math.min(100, 25 + rsController.getCurrentRedstonePower() * 5);
        }
        if (!stack.isEmpty() && rsController.shouldRun()) {
            int progress = getExposureProgress(stack);
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && progress < 100) {
                addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                if (ticksExisted % ticksPerProgress(progress) == 0) {
                    progress++;
                    setExposureProgress(stack, progress);
                }
                if (progress >= threshold) {
                    if (outputHandler.insertItem(0, inputHandler.getStackInSlot(0), true).isEmpty()) {
                        ItemStack toMove = inputHandler.extractItem(0, 1, false);
                        outputHandler.insertItem(0, toMove, false);
                    }
                }
                didWork = true;
            }
        }
        if (getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            tryEject();
        }
        checkStateUpdates(stack, didWork);
    }

    private void checkStateUpdates(ItemStack loadedStack, boolean didWork) {
        BlockState state = getBlockState();
        if (state.getBlock() == ModBlocks.UV_LIGHT_BOX.get()) {
            boolean loaded = state.getValue(UVLightBoxBlock.LOADED);
            boolean update = false;
            if (loaded == loadedStack.isEmpty()) {
                state = state.setValue(UVLightBoxBlock.LOADED, !loadedStack.isEmpty());
                update = true;
            }
            if (didWork != getBlockState().getValue(UVLightBoxBlock.LIT)) {
                state = state.setValue(UVLightBoxBlock.LIT, didWork);
                update = true;
            }
            long now = nonNullLevel().getGameTime();
            if (update) {
                if (now - lastStateUpdate > 10) {
                    nonNullLevel().setBlockAndUpdate(worldPosition, state);
                    pendingState = null;
                    lastStateUpdate = now;
                } else {
                    pendingState = state;
                }
            } else if (pendingState != null && now - lastStateUpdate > 10) {
                nonNullLevel().setBlockAndUpdate(worldPosition, pendingState);
                pendingState = null;
                lastStateUpdate = now;
            }
        }
    }

    private void tryEject() {
        Direction dir = getUpgradeCache().getEjectDirection();
        if (dir != null) {
            getEjectionHandler().ifPresent(handler -> {
                ItemStack stack = outputHandler.extractItem(0, 1, true);
                if (!stack.isEmpty() && ItemHandlerHelper.insertItem(handler, stack, false).isEmpty()) {
                    outputHandler.extractItem(0, 1, false);
                }
            });
        }
    }

    private LazyOptional<IItemHandler> getEjectionHandler() {
        if (!cachedEjectHandler.isPresent()) {
            Direction dir = getUpgradeCache().getEjectDirection();
            BlockEntity te = nonNullLevel().getBlockEntity(worldPosition.relative(dir));
            cachedEjectHandler = IOHelper.getInventoryForTE(te, dir.getOpposite());
            if (cachedEjectHandler.isPresent()) {
                cachedEjectHandler.addListener(l -> cachedEjectHandler = LazyOptional.empty());
            }
        }
        return cachedEjectHandler;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        threshold = tag.getInt("threshold");
        inputHandler.deserializeNBT(tag.getCompound("Items"));
        outputHandler.deserializeNBT(tag.getCompound("Output"));
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putInt("threshold", threshold);
        nbt.put("Items", inputHandler.serializeNBT());
        nbt.put("Output", outputHandler.serializeNBT());
    }

    public static int getExposureProgress(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(NBT_EXPOSURE) : 0;
    }

    public static ItemStack setExposureProgress(ItemStack stack, int progress) {
        Validate.isTrue(progress >= 0 && progress <= 100);
        stack.getOrCreateTag().putInt(NBT_EXPOSURE, progress);
        return stack;
    }

    private int ticksPerProgress(int progress) {
        int ticks;
        if (progress < 20) {
            ticks = 20;
        } else if (progress < 40) {
            ticks = 40;
        } else if (progress < 60) {
            ticks = 80;
        } else if (progress < 80) {
            ticks = 160;
        } else {
            ticks = 300;
        }
        return Math.max(1, (int) (ticks / getSpeedMultiplierFromUpgrades()));
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void onDescUpdate() {
        nonNullLevel().getChunkSource().getLightEngine().checkBlock(getBlockPos());

        super.onDescUpdate();
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        try {
            threshold = Mth.clamp(Integer.parseInt(tag), 1, 100);
            setChanged();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inputHandler;
    }

    public IItemHandler getOutputInventory() {
        return outputHandler;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
        return invCap;
    }

    @Override
    public RedstoneController<UVLightBoxBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX;
    }

    private ItemStack getLoadedPCB() {
        return inputHandler.getStackInSlot(PCB_SLOT);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new UVLightBoxMenu(i, playerInventory, getBlockPos());
    }

    public int getThreshold() {
        return threshold;
    }

    private class UVInputHandler extends BaseItemStackHandler {
        UVInputHandler() {
            super(UVLightBoxBlockEntity.this, INVENTORY_SIZE);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1; // Only process one item at a time.
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof EmptyPCBItem && EmptyPCBItem.getEtchProgress(itemStack) == 0;
        }
    }

    private class UVInvWrapper implements IItemHandler {
        UVInvWrapper() {
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? inputHandler.getStackInSlot(0) : outputHandler.getStackInSlot(0);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot == 0 ? inputHandler.insertItem(0, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? outputHandler.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? inputHandler.getSlotLimit(0) : outputHandler.getSlotLimit(0);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? inputHandler.isItemValid(0, stack) : outputHandler.isItemValid(0, stack);
        }
    }
}
