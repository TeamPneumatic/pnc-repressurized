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

package me.desht.pneumaticcraft.common.block.entity.compressor;

import me.desht.pneumaticcraft.api.block.PNCBlockStateProperties;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.inventory.AirCompressorMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class AirCompressorBlockEntity extends AbstractAirHandlingBlockEntity implements IRedstoneControl<AirCompressorBlockEntity>, MenuProvider {
    private static final int INVENTORY_SIZE = 1;

    private final AirCompressorFuelHandler itemHandler = new AirCompressorFuelHandler();

    private static final int FUEL_SLOT = 0;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    private int maxBurnTime; // in here the total burn time of the current burning item is stored.
    @GuiSynced
    public final RedstoneController<AirCompressorBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    public int curFuelUsage;
    @GuiSynced
    public float airPerTick;
    private float airBuffer;

    public AirCompressorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.AIR_COMPRESSOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    AirCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volume) {
        super(type, pos, state, tier, volume, 4);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new AirCompressorMenu(i, playerInventory, getBlockPos());
    }

    public boolean isActive() {
        return getBlockState().hasProperty(PNCBlockStateProperties.ON) && getBlockState().getValue(PNCBlockStateProperties.ON);
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (isActive()) {
            spawnBurningParticle();
        }
    }

    @Override
    public void tickServer() {
        airPerTick = getBaseProduction() * getSpeedMultiplierFromUpgrades() * getHeatEfficiency() / 100F;

        if (rsController.shouldRun() && burnTime < curFuelUsage) {
            ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
            int itemBurnTime = fuelStack.getBurnTime(RecipeType.SMELTING);
//            int itemBurnTime = CommonHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
            if (itemBurnTime > 0) {
                burnTime += itemBurnTime;
                maxBurnTime = burnTime;
                if (fuelStack.hasCraftingRemainingItem()) {
                    itemHandler.setStackInSlot(FUEL_SLOT, fuelStack.getCraftingRemainingItem());
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
        boolean newIsActive = burnTime > curFuelUsage;
        if (isActive() != newIsActive) {
            BlockState state = getBlockState();
            if (state.hasProperty(PNCBlockStateProperties.ON)) {
                nonNullLevel().setBlockAndUpdate(getBlockPos(), state.setValue(PNCBlockStateProperties.ON, newIsActive));
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
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandler;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurn");
        itemHandler.deserializeNBT(provider, tag.getCompound("Items"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurn", maxBurnTime);
        tag.put("Items", itemHandler.serializeNBT(provider));
    }

    @Override
    public RedstoneController<AirCompressorBlockEntity> getRedstoneController() {
        return rsController;
    }

    private class AirCompressorFuelHandler extends BaseItemStackHandler {
        AirCompressorFuelHandler() {
            super(AirCompressorBlockEntity.this, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return slot == FUEL_SLOT &&
                    (itemStack.isEmpty() || itemStack.getBurnTime(RecipeType.SMELTING) > 0 && FluidUtil.getFluidContained(itemStack).isEmpty());
        }
    }

}
