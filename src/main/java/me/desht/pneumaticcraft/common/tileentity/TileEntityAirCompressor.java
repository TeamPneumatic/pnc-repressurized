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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.BlockAirCompressor;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.inventory.AirCompressorMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements IRedstoneControl<TileEntityAirCompressor>, MenuProvider {
    private static final int INVENTORY_SIZE = 1;

    private final AirCompressorFuelHandler itemHandler = new AirCompressorFuelHandler();
    private final LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> itemHandler);

    private static final int FUEL_SLOT = 0;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    private int maxBurnTime; // in here the total burn time of the current burning item is stored.
    @GuiSynced
    public final RedstoneController<TileEntityAirCompressor> rsController = new RedstoneController<>(this);
    @DescSynced
    private boolean isActive;
    @GuiSynced
    public int curFuelUsage;
    @GuiSynced
    public float airPerTick;
    private float airBuffer;

    public TileEntityAirCompressor(BlockPos pos, BlockState state) {
        this(ModBlockEntities.AIR_COMPRESSOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    TileEntityAirCompressor(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volume) {
        super(type, pos, state, tier, volume, 4);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new AirCompressorMenu(i, playerInventory, getBlockPos());
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void tickClient() {
        super.tickClient();
        if (isActive) spawnBurningParticle();
    }

    @Override
    public void tickServer() {
        airPerTick = getBaseProduction() * getSpeedMultiplierFromUpgrades() * getHeatEfficiency() / 100F;

        if (rsController.shouldRun() && burnTime < curFuelUsage) {
            ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
            int itemBurnTime = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
            if (itemBurnTime > 0) {
                burnTime += itemBurnTime;
                maxBurnTime = burnTime;
                if (fuelStack.hasContainerItem()) {
                    itemHandler.setStackInSlot(FUEL_SLOT, fuelStack.getContainerItem());
                } else {
                    itemHandler.extractItem(FUEL_SLOT, 1, false);
                }
            }
        }

        curFuelUsage = (int) (getBaseProduction() * getSpeedUsageMultiplierFromUpgrades() / 10);
        if (burnTime >= curFuelUsage) {
            burnTime -= curFuelUsage;
            airBuffer += airPerTick;
            if (airBuffer >= 1f) {
                int toAdd = (int) airBuffer;
                addAir(toAdd);
                airBuffer -= toAdd;
                addHeatForAir(toAdd);
            }
        }
        boolean wasActive = isActive;
        isActive = burnTime > curFuelUsage;
        if (wasActive != isActive) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockAirCompressor.ON)) {
                nonNullLevel().setBlockAndUpdate(getBlockPos(), state.setValue(BlockAirCompressor.ON, isActive));
            }
        }
        airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation() : null);
    }

    protected void addHeatForAir(int air) {
        // do nothing, override in advanced
    }

    public int getHeatEfficiency() {
        return 100;
    }

    public int getBaseProduction() {
        return PneumaticValues.PRODUCTION_COMPRESSOR;
    }

    private void spawnBurningParticle() {
        Level level = nonNullLevel();
        if (level.random.nextInt(3) != 0) return;
        float px = getBlockPos().getX() + 0.5F;
        float py = getBlockPos().getY() + level.random.nextFloat() * 6.0F / 16.0F;
        float pz = getBlockPos().getZ() + 0.5F;
        float f3 = 0.5F;
        float f4 = level.random.nextFloat() * 0.4F - 0.2F;
        switch (getRotation()) {
            case EAST -> {
                level.addParticle(ParticleTypes.SMOKE, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.FLAME, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
            }
            case WEST -> {
                level.addParticle(ParticleTypes.SMOKE, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.FLAME, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
            }
            case SOUTH -> {
                level.addParticle(ParticleTypes.SMOKE, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.FLAME, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
            }
            case NORTH -> {
                level.addParticle(ParticleTypes.SMOKE, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.FLAME, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }

    public int getBurnTimeRemainingScaled(int parts) {
        if (maxBurnTime == 0 || burnTime < curFuelUsage) return 0;
        return parts * burnTime / maxBurnTime;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), getBlockPos().getX() + 1, getBlockPos().getY() + 1, getBlockPos().getZ() + 1);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurn");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurn", maxBurnTime);
        tag.put("Items", itemHandler.serializeNBT());
    }

    @Override
    public RedstoneController<TileEntityAirCompressor> getRedstoneController() {
        return rsController;
    }

    private class AirCompressorFuelHandler extends BaseItemStackHandler {
        AirCompressorFuelHandler() {
            super(TileEntityAirCompressor.this, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return slot == FUEL_SLOT &&
                    (itemStack.isEmpty() || ForgeHooks.getBurnTime(itemStack, RecipeType.SMELTING) > 0 && FluidUtil.getFluidContained(itemStack).isEmpty());
        }
    }

}
