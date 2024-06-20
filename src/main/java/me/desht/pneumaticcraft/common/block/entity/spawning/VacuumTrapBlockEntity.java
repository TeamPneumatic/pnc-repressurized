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

package me.desht.pneumaticcraft.common.block.entity.spawning;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.inventory.VacuumTrapMenu;
import me.desht.pneumaticcraft.common.item.SpawnerCoreItem.SpawnerCoreItemHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VacuumTrapBlockEntity extends AbstractAirHandlingBlockEntity implements
        IMinWorkingPressure, MenuProvider, ISerializableTanks, IRangedTE {
    public static final String DEFENDER_TAG = Names.MOD_ID + ":defender";
    public static final int MEMORY_ESSENCE_AMOUNT = 100;

    private final SpawnerCoreItemHandler inv = new SpawnerCoreItemHandler(this);

    private final List<Mob> targetEntities = new ArrayList<>();

    private final RangeManager rangeManager = new RangeManager(this, 0x60600060);

    @GuiSynced
    private final SmartSyncTank xpTank = new XPTank();

    @DescSynced
    private boolean isCoreLoaded;
    @DescSynced
    public Problems problem = Problems.OK;

    public VacuumTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.VACUUM_TRAP.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_VACUUM_TRAP, 4);
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        xpTank.tick();

        rangeManager.setRange(3 + getUpgrades(ModUpgrades.RANGE.get()));
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (isOpen() && isCoreLoaded && nonNullLevel().random.nextBoolean()) {
            ClientUtils.emitParticles(level, worldPosition, ParticleTypes.PORTAL);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        isCoreLoaded = inv.getStats() != null;

        if (isOpen() && isCoreLoaded && inv.getStats().getUnusedPercentage() > 0 && getPressure() <= getMinWorkingPressure()) {
            if ((nonNullLevel().getGameTime() & 0xf) == 0) {
                scanForEntities();
            }
            Vec3 trapVec = Vec3.atCenterOf(worldPosition);
            double min = nonNullLevel().getFluidState(worldPosition).getType() == Fluids.WATER ? 2.5 : 1.75;
            for (Mob e : targetEntities) {
                if (!e.isAlive() || e.getTags().contains(DEFENDER_TAG)) continue;
                // kludge: mobs in water seem a bit flaky about getting close enough so increase the absorb dist a bit
                if (e.distanceToSqr(trapVec) <= min) {
                    absorbEntity(e);
                    addAir((int) (PneumaticValues.USAGE_VACUUM_TRAP * e.getHealth()));
                } else {
                    e.getNavigation().moveTo(trapVec.x(), trapVec.y(), trapVec.z(), 1.2);
                }
            }
        }
        if (!isCoreLoaded)
            problem = Problems.NO_CORE;
        else if (inv.getStats().getUnusedPercentage() == 0)
            problem = Problems.CORE_FULL;
        else if (!isOpen())
            problem = Problems.TRAP_CLOSED;
        else
            problem = Problems.OK;
    }

    private void absorbEntity(Mob e) {
        int toAdd = 1;
        if (xpTank.getFluid().getAmount() >= MEMORY_ESSENCE_AMOUNT) {
            toAdd += e.level().random.nextInt(3) + 1;
        }
        ISpawnerCoreStats newStats = inv.getStats().addAmount(e.getType(), toAdd);
        if (newStats != inv.getStats()) {
            e.discard();
            if (toAdd > 1) xpTank.drain(MEMORY_ESSENCE_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
            inv.getStackInSlot(0).set(ModDataComponents.SPAWNER_CORE_STATS, newStats);
            e.level().playSound(null, worldPosition, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1f, 2f);
            if (level instanceof ServerLevel) {
                ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 0.5, e.getZ(), 5, 0, 1, 0, 0);
            }
        }
    }

    private void scanForEntities() {
        targetEntities.clear();
        targetEntities.addAll(nonNullLevel().getEntitiesOfClass(Mob.class, rangeManager.getExtentsAsAABB(), this::isApplicable));
    }

    private boolean isApplicable(LivingEntity e) {
        return e.getType().is(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_WHITELISTED)
                || e.canChangeDimensions()
                && !(e instanceof DroneEntity)
                && !(e instanceof Warden)
                && !(e instanceof TamableAnimal t && t.isTame())
                && !e.getType().is(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_BLACKLISTED);
    }

    @Override
    public IFluidHandler getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return xpTank;
    }

    public IFluidTank getFluidTank() {
        return xpTank;
    }

    @Nonnull
    @Override
    public Map<DataComponentType<SimpleFluidContent>, PNCFluidTank> getSerializableTanks() {
        return Map.of(ModDataComponents.MAIN_TANK.get(), xpTank);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inv;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == Direction.DOWN || side.getAxis() == getRotation().getAxis();
    }

    @Override
    public float getMinWorkingPressure() {
        return -0.5f;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new VacuumTrapMenu(windowId, inv, getBlockPos());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inv.deserializeNBT(provider, tag.getCompound("Items"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Items", inv.serializeNBT(provider));
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        // if we're wrenching, any spawner core should stay in the trap
        if (!shouldPreserveStateOnBreak()) {
            super.getContentsToDrop(drops);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        inv.loadContainerContents(componentInput.get(ModDataComponents.BLOCK_ENTITY_SAVED_INV));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        if (shouldPreserveStateOnBreak()) {
            builder.set(ModDataComponents.BLOCK_ENTITY_SAVED_INV, inv.toContainerContents());
        }
    }

    public boolean isOpen() {
        return getBlockState().getBlock() == ModBlocks.VACUUM_TRAP.get() && getBlockState().getValue(BlockStateProperties.OPEN);
    }

    @Override
    public RangeManager getRangeManager() {
        return rangeManager;
    }

    public enum Problems implements ITranslatableEnum {
        OK,
        NO_CORE,
        CORE_FULL,
        TRAP_CLOSED;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.problems.vacuum_trap." + this.toString().toLowerCase(Locale.ROOT);
        }
    }

    private class XPTank extends SmartSyncTank {
        public XPTank() {
            super(VacuumTrapBlockEntity.this, 16000);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().is(PneumaticCraftTags.Fluids.EXPERIENCE);
        }
    }
}
