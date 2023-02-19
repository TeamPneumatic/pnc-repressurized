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
import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.KeroseneLampMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

public class KeroseneLampBlockEntity extends AbstractTickingBlockEntity implements
        IRedstoneControl<KeroseneLampBlockEntity>, ISerializableTanks, MenuProvider {

    private static final List<RedstoneMode<KeroseneLampBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("standard.always", new ItemStack(Items.GUNPOWDER),
                    te -> true),
            new ReceivingRedstoneMode<>("standard.high_signal", new ItemStack(Items.REDSTONE),
                    te -> te.getCurrentRedstonePower() > 0),
            new ReceivingRedstoneMode<>("standard.low_signal", new ItemStack(Items.REDSTONE_TORCH),
                    te -> te.getCurrentRedstonePower() == 0),
            new ReceivingRedstoneMode<>("keroseneLamp.interpolate", new ItemStack(Items.COMPARATOR),
                    te -> te.getCurrentRedstonePower() > 0)
    );

    public static final int INVENTORY_SIZE = 2;
    public static final int TICK_RATE = 5;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int RS_MODE_INTERPOLATE = 3;
    private static final int LIGHT_SPACING = 3;
    public static final int MAX_RANGE = 30;

    private final Set<BlockPos> managingLights = new HashSet<>();
    private boolean isOn;
    @GuiSynced
    private int range;
    @GuiSynced
    private int targetRange = 10;
    @GuiSynced
    private final RedstoneController<KeroseneLampBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @GuiSynced
    private int fuel;
    private int checkingX, checkingY, checkingZ;
    private int rangeSq;

    @DescSynced
    @GuiSynced
    private final SmartSyncTank tank = new SmartSyncTank(this, 2000) {
        @Override
        protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
            super.onContentsChanged(prevFluid, prevAmount);
            if (prevFluid != fluid.getFluid()) {
                fuelQuality = calculateFuelQuality(fluid.getFluid());
            }
        }
    };
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    @DescSynced
    private float fuelQuality = -1f; // the quality of the liquid currently in the tank; basically, its burn time

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || FluidUtil.getFluidHandler(itemStack).isPresent();
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    public KeroseneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KEROSENE_LAMP.get(), pos, state);
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
        return inventoryCap;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        tank.tick();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        final Level level = nonNullLevel();
        if (getBlockState().getValue(LIT) && level.random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.FLAME, getBlockPos().getX() + 0.4 + 0.2 * level.random.nextDouble(), getBlockPos().getY() + 0.2 + tank.getFluidAmount() / 1000D * 3 / 16D, getBlockPos().getZ() + 0.4 + 0.2 * level.random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (fuelQuality < 0) {
            fuelQuality = calculateFuelQuality(tank.getFluid().getFluid());
        }
        processFluidItem(INPUT_SLOT, OUTPUT_SLOT);
        if (nonNullLevel().getGameTime() % TICK_RATE == 0) {
            int effectiveRange = rsController.shouldRun() && fuel > 0 ? targetRange : 0;
            if (rsController.getCurrentMode() == RS_MODE_INTERPOLATE) {
                effectiveRange = (int) (rsController.getCurrentRedstonePower() / 15D * targetRange);
            }
            updateRange(Math.min(effectiveRange, tank.getFluidAmount())); //Fade out the lamp when almost empty.
            updateLights();
            useFuel();
        }
    }

    public float calculateFuelQuality(Fluid fuel) {
        // 110 comes from kerosene's fuel value of 1,100,000 divided by the old FUEL_PER_MB value (10000)
        boolean isKerosene = fuel.is(PneumaticCraftTags.Fluids.KEROSENE);
        float quality = ConfigHelper.common().machines.keroseneLampCanUseAnyFuel.get() ?
                PneumaticRegistry.getInstance().getFuelRegistry().getFuelValue(level, fuel) / 110f :
                isKerosene ? 10000f : 0f;
        if (isKerosene) {
            quality *= 2.5f;  // kerosene is better than everything for lighting purposes
        }
        quality *= ConfigHelper.common().machines.keroseneLampFuelEfficiency.get();
        return quality;
    }

    private void useFuel() {
        if (fuelQuality == 0) return; // tank is empty or a non-burnable liquid in the tank
        fuel -= rangeSq * 3;
        while (fuel <= 0 && !tank.drain(1, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
            fuel += fuelQuality;
        }
        if (fuel < 0) fuel = 0;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        checkingX = getBlockPos().getX();
        checkingY = getBlockPos().getY();
        checkingZ = getBlockPos().getZ();
    }

    public void removeLights() {
        // called from KeroseneLampBlock#onRemove()
        // note: this should *not* be done in setRemoved(), since that's also called on level unload!
        for (BlockPos pos : managingLights) {
            if (nonNullLevel().isLoaded(pos) && isLampLight(pos)) {
                nonNullLevel().removeBlock(pos, false);
            }
        }
    }

    private boolean isLampLight(BlockPos pos) {
        return nonNullLevel().getBlockState(pos).getBlock() == ModBlocks.KEROSENE_LAMP_LIGHT.get();
    }

    private void updateLights() {
        int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
        checkingX += LIGHT_SPACING;
        if (checkingX > getBlockPos().getX() + roundedRange) {
            checkingX = getBlockPos().getX() - roundedRange;
            checkingY += LIGHT_SPACING;
            if (checkingY > getBlockPos().getY() + roundedRange) {
                checkingY = getBlockPos().getY() - roundedRange;
                checkingZ += LIGHT_SPACING;
                if (checkingZ > getBlockPos().getZ() + roundedRange) checkingZ = getBlockPos().getZ() - roundedRange;
            }
        }
        BlockPos pos = new BlockPos(checkingX, checkingY, checkingZ);
        if (!nonNullLevel().isLoaded(pos)) return;

        if (managingLights.contains(pos)) {
            if (isLampLight(pos)) {
                if (!passesRaytraceTest(pos, getBlockPos())) {
                    nonNullLevel().removeBlock(pos, false);
                    managingLights.remove(pos);
                }
            } else {
                managingLights.remove(pos);
            }
        } else {
            tryAddLight(pos);
        }
    }

    private void updateRange(int targetRange) {
        if (targetRange > range) {
            range++;
            int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
            for (int x = -roundedRange; x <= roundedRange; x += LIGHT_SPACING) {
                for (int y = -roundedRange; y <= roundedRange; y += LIGHT_SPACING) {
                    for (int z = -roundedRange; z <= roundedRange; z += LIGHT_SPACING) {
                        BlockPos pos = getBlockPos().offset(x, y, z);
                        if (!managingLights.contains(pos)) {
                            tryAddLight(pos);
                        }
                    }
                }
            }
        } else if (targetRange < range) {
            range--;
            Iterator<BlockPos> iterator = managingLights.iterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                if (nonNullLevel().isLoaded(pos)) {
                    if (!isLampLight(pos)) {
                        iterator.remove();
                    } else if (PneumaticCraftUtils.distBetweenSq(pos, getBlockPos()) > rangeSq) {
                        nonNullLevel().removeBlock(pos, false);
                        iterator.remove();
                    }
                }
            }
        }
        rangeSq = range * range;
        boolean wasOn = isOn;
        isOn = range > 0;
        if (isOn != wasOn) {
            nonNullLevel().getChunkSource().getLightEngine().checkBlock(getBlockPos());
            nonNullLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(LIT, isOn));
        }
    }

    private boolean passesRaytraceTest(BlockPos pos, BlockPos lampPos) {
        // must be run on server!
        ClipContext ctx = new ClipContext(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), new Vec3(lampPos.getX() + 0.5, lampPos.getY() + 0.5, lampPos.getZ() + 0.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, FakePlayerFactory.getMinecraft((ServerLevel) getLevel()));
        BlockHitResult rtr = nonNullLevel().clip(ctx);
        return rtr.getType() == HitResult.Type.BLOCK && rtr.getBlockPos().equals(lampPos);
    }

    private void tryAddLight(BlockPos pos) {
        if (!ConfigHelper.common().advanced.disableKeroseneLampFakeAirBlock.get()
                && nonNullLevel().isLoaded(pos)
                && PneumaticCraftUtils.distBetweenSq(pos, getBlockPos()) <= rangeSq
                && nonNullLevel().isEmptyBlock(pos)
                && !isLampLight(pos)
                && passesRaytraceTest(pos, getBlockPos()))
        {
            nonNullLevel().setBlockAndUpdate(pos, ModBlocks.KEROSENE_LAMP_LIGHT.get().defaultBlockState());
            managingLights.add(pos);
        }
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("lights", managingLights.stream().map(NbtUtils::writeBlockPos).collect(Collectors.toCollection(ListTag::new)));
        tag.putByte("targetRange", (byte) targetRange);
        tag.putByte("range", (byte) range);
        tag.put("Items", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        managingLights.clear();
        ListTag lights = tag.getList("lights", 10);
        for (int i = 0; i < lights.size(); i++) {
            managingLights.add(NbtUtils.readBlockPos(lights.getCompound(i)));
        }
        fuelQuality = calculateFuelQuality(tank.getFluid().getFluid());
        targetRange = tag.getByte("targetRange");
        range = tag.getByte("range");
        rangeSq = range * range;
        inventory.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public RedstoneController<KeroseneLampBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        try {
            targetRange = Mth.clamp(Integer.parseInt(tag), 1, MAX_RANGE);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public SmartSyncTank getTank() {
        return tank;
    }

    public int getRange() {
        return range;
    }

    public int getTargetRange() {
        return targetRange;
    }

    public int getFuel() {
        return fuel;
    }

    @NotNull
    @Override
    public LazyOptional<IFluidHandler> getFluidCap(Direction side) {
        return fluidCap;
    }

    public float getFuelQuality() {
        return fuelQuality;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new KeroseneLampMenu(i, playerInventory, getBlockPos());
    }
}
