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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPressurizedSpawner;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPressurizedSpawner extends TileEntityPneumaticBase implements
        IMinWorkingPressure, IRedstoneControl<TileEntityPressurizedSpawner>,
        MenuProvider, IRangedTE
{
    public static final int BASE_SPAWN_INTERVAL = 200;
    private static final int MAX_NEARBY_ENTITIES = 32;

    private final ItemSpawnerCore.SpawnerCoreItemHandler inventory = new ItemSpawnerCore.SpawnerCoreItemHandler(this);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
    @GuiSynced
    public TileEntityVacuumTrap.Problems problem = TileEntityVacuumTrap.Problems.OK;
    @GuiSynced
    private final RedstoneController<TileEntityPressurizedSpawner> rsController = new RedstoneController<>(this);
    private int counter = -1;  // -1 => re-init on next tick
    @DescSynced
    private boolean running;
    private final RangeManager rangeManager = new RangeManager(this, 0x60400040).withCustomExtents(this::buildCustomExtents);

    public TileEntityPressurizedSpawner(BlockPos pos, BlockState state) {
        super(ModTileEntities.PRESSURIZED_SPAWNER.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_PRESSURIZED_SPAWNER, 4);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        rangeManager.setRange(2 + getUpgrades(EnumUpgrade.RANGE));
        if (counter < 0) counter = getSpawnInterval();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (running) {
            Level level = nonNullLevel();
            double x = (double)worldPosition.getX() + level.random.nextDouble();
            double y = (double)worldPosition.getY() + level.random.nextDouble();
            double z = (double)worldPosition.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        ISpawnerCoreStats stats = inventory.getStats();
        running = false;
        problem = TileEntityVacuumTrap.Problems.OK;
        if (stats == null) {
            problem = TileEntityVacuumTrap.Problems.NO_CORE;
        } else if (getPressure() > getMinWorkingPressure() && rsController.shouldRun()) {
            running = true;
            if (--counter <= 0) {
                if (!trySpawnSomething(stats) && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 5, 0, 0, 0, 0);
                }
                addAir(-getAirUsage());
                counter = getSpawnInterval();
            }
        }
    }

    private AABB buildCustomExtents() {
        // following vanilla spawner behaviour of constrained Y-value (-1 .. +2)
        AABB aabb = new AABB(getBlockPos(), getBlockPos());
        return aabb.inflate(getRange(), 0, getRange()).expandTowards(0, 2, 0).expandTowards(0, -1, 0);
    }

    private boolean trySpawnSomething(ISpawnerCoreStats stats) {
        EntityType<?> type = stats.pickEntity(true);
        if (type != null && level instanceof ServerLevel serverworld) {
            int spawnRange = getRange();
            double x = (double)worldPosition.getX() + (serverworld.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
            double y = worldPosition.getY() + serverworld.random.nextInt(3) - 1;
            double z = (double)worldPosition.getZ() + (serverworld.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
            if (serverworld.noCollision(type.getAABB(x, y, z))) {
                Entity entity = type.create(serverworld);
                if (!(entity instanceof Mob mobentity)) return false;
                int entityCount = serverworld.getEntitiesOfClass(Mob.class, rangeManager.getExtents()).size();
                if (entityCount >= MAX_NEARBY_ENTITIES) return false;
                entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
                if (ForgeEventFactory.doSpecialSpawn(mobentity, level, (float)entity.getX(), (float)entity.getY(), (float)entity.getZ(), null, MobSpawnType.SPAWNER)) {
                    return false;
                }
                mobentity.finalizeSpawn(serverworld, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null, null);
                if (!serverworld.tryAddFreshEntityWithPassengers(entity)) return false;
                level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, worldPosition, 0);
                mobentity.spawnAnim();
                mobentity.setPersistenceRequired();
                return true;
            }
        }
        return false;
    }

    public int getSpawnInterval() {
        return (int)(BASE_SPAWN_INTERVAL / getSpeedMultiplierFromUpgrades());
    }

    public int getAirUsage() { return PneumaticValues.USAGE_PRESSURIZED_SPAWNER * (getUpgrades(EnumUpgrade.SPEED) + 1); }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public float getMinWorkingPressure() {
        return 10f;
    }

    @Override
    public RedstoneController<TileEntityPressurizedSpawner> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ContainerPressurizedSpawner(windowId, inv, getBlockPos());
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return rangeManager.shouldShowRange() ? rangeManager.getExtents() : super.getRenderBoundingBox();
    }

    @Override
    public RangeManager getRangeManager() {
        return rangeManager;
    }
}
