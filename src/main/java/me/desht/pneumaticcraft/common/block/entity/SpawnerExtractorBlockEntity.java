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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.inventory.SpawnerExtractorMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.mixin.accessors.BaseSpawnerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SpawnerExtractorBlockEntity extends AbstractAirHandlingBlockEntity implements IMinWorkingPressure, MenuProvider {

    private static final int MAX_ENTITY_RANGE = 6;
    private Entity cachedEntity;

    public enum Mode { INIT, RUNNING, FINISHED }

    @DescSynced
    private Mode mode = Mode.INIT;
    @DescSynced
    private float targetSpeed;

    private float rotationDegrees;  // client-side
    private float prevRotationDegrees;

    @GuiSynced
    private float progress;
    private float currentSpeed;
    private int spawnFailures;

    public SpawnerExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SPAWNER_EXTRACTOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_SPAWNER_EXTRACTOR, 4);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        if (Math.abs(currentSpeed - targetSpeed) < 0.015f) {
            currentSpeed = targetSpeed;
        } else if (currentSpeed < targetSpeed) {
            currentSpeed += Math.max(0.005f, (targetSpeed - currentSpeed) / 20f);
        } else if (currentSpeed > targetSpeed) {
            currentSpeed -= Math.max(0.01f, (targetSpeed - currentSpeed) / 10f);
        }

        if (mode == Mode.INIT) {
            updateMode();
        }
        switch (mode) {
            case RUNNING -> {
                float incr = currentSpeed / 1200f;   // at max speed, 1200 ticks to completion
                progress = Math.min(1f, progress + incr);
            }
            case FINISHED -> {
                progress = 1f;
                targetSpeed = 0f;
                rotationDegrees = prevRotationDegrees;
            }
        }
    }

    @Override
    public void tickClient() {
        super.tickClient();

        prevRotationDegrees = rotationDegrees;
        // at full speed, 1 rotation per second
        rotationDegrees += currentSpeed * 18f;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        int defenderChance = nonNullLevel().getDifficulty() == Difficulty.EASY ? 40 : 20;
        if (mode == Mode.RUNNING) {
            addAir(PneumaticValues.USAGE_SPAWNER_EXTRACTOR);
            if (progress >= 1f) {
                extractSpawnerCore();
            } else if (currentSpeed > 0.1f && nonNullLevel().random.nextInt(defenderChance) == 0) {
                // spawn defending entities; each entity nearby will slow down the extraction process
                nonNullLevel().getBlockEntity(worldPosition.below(), BlockEntityType.MOB_SPAWNER).ifPresent(te -> {
                    if (!trySpawnDefender(te)) {
                        spawnFailures++;
                    }
                });
            }
        }
        if ((nonNullLevel().getGameTime() & 0xf) == 3) {
            targetSpeed = getTargetSpeed();
        }
        if ((nonNullLevel().getGameTime() & 0x3f) == 3) {
            spawnFailures = Math.max(0, spawnFailures - 1);
        }
    }

    private boolean trySpawnDefender(SpawnerBlockEntity te) {
        // logic largely lifted from AbstractSpawner#tick()

        BaseSpawner spawner = te.getSpawner();

        int spawnRange = 4;
        int maxNearbyEntities = 16;

        CompoundTag nbt = ((BaseSpawnerAccess) spawner).getNextSpawnData().getEntityToSpawn();
        Optional<EntityType<?>> optional = EntityType.by(nbt);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos pos = te.getBlockPos();
        ListTag listnbt = nbt.getList("Pos", Tag.TAG_DOUBLE);
        int size = listnbt.size();
        Level level = nonNullLevel();
        double x = size >= 1 ? listnbt.getDouble(0) : (double)pos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
        double y = size >= 2 ? listnbt.getDouble(1) : (double)(pos.getY() + level.random.nextInt(3) - 1);
        double z = size >= 3 ? listnbt.getDouble(2) : (double)pos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
        if (level.noCollision(optional.get().getAABB(x, y, z))) {
            ServerLevel serverworld = (ServerLevel) level;
            Entity entity = EntityType.loadEntityRecursive(nbt, level, (e1) -> {
                e1.moveTo(x, y, z, e1.getYRot(), e1.getXRot());
                return e1;
            });
            if (entity == null) {
                return false;
            }

            int entityCount = level.getEntitiesOfClass(entity.getClass(), (new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).inflate(spawnRange)).size();
            if (entityCount >= maxNearbyEntities) {
                return false;
            }

            entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mobentity) {
                if (nbt.size() == 1 && nbt.contains("id", Tag.TAG_STRING)) {
                    EventHooks.onFinalizeSpawn(mobentity, serverworld, serverworld.getCurrentDifficultyAt(getPosition()), MobSpawnType.SPAWNER, null, null);
                    // note: "pneumaticcraft:defender" tag is added in TileEntityVacuumTrap.Listener.onMobSpawn()
                    if (level.getDifficulty() == Difficulty.HARD) {
                        getRandomEffects(level.random).forEach(effect -> mobentity.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, 2)));
                    }
                }
            }

            if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                return false;
            }

            level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
            if (entity instanceof Mob) {
                ((Mob)entity).spawnAnim();
            }
        }
        return true;
    }

    private List<MobEffect> getRandomEffects(RandomSource rand) {
        List<MobEffect> l = new ArrayList<>();
        int n = rand.nextInt(100);
        if (n > 50) l.add(MobEffects.FIRE_RESISTANCE);
        if (n > 75) l.add(MobEffects.MOVEMENT_SPEED);
        if (n > 82) l.add(MobEffects.DAMAGE_BOOST);
        if (n > 92) l.add(MobEffects.REGENERATION);
        if (n > 96) l.add(MobEffects.INVISIBILITY);
        return l;
    }

    private void extractSpawnerCore() {
        nonNullLevel().getBlockEntity(worldPosition.below(), BlockEntityType.MOB_SPAWNER).ifPresent(te -> {
            ItemStack spawnerCore = new ItemStack(ModItems.SPAWNER_CORE.get());
            ISpawnerCoreStats stats = PneumaticRegistry.getInstance().getItemRegistry().getSpawnerCoreStats(spawnerCore);
            Entity e = getCachedEntity(te);
            if (e != null && stats != null) {
                stats.addAmount(e.getType(), 100);
                stats.serialize(spawnerCore);
                Level level = nonNullLevel();
                ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, spawnerCore);
                level.addFreshEntity(item);
                level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 0.5f);
                level.setBlock(worldPosition.below(), ModBlocks.EMPTY_SPAWNER.get().defaultBlockState(), Block.UPDATE_ALL);
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, worldPosition, Block.getId(Blocks.SPAWNER.defaultBlockState()));
            }
        });
    }

    private float getTargetSpeed() {
        if (getPressure() > getMinWorkingPressure()) return 0f;

        return nonNullLevel().getBlockEntity(worldPosition.below(), BlockEntityType.MOB_SPAWNER).map(spawner -> {
            int players = 0;
            int matches = 0;
            Entity e0 = getCachedEntity(spawner);
            if (e0 == null) return 0f;
            List<LivingEntity> l = nonNullLevel().getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(MAX_ENTITY_RANGE), e -> true);
            for (LivingEntity e : l) {
                if (e instanceof Player && !(e instanceof FakePlayer)) players++;
                if (e.getType() == e0.getType()) matches++;
            }
            int n = players > 0 ? Math.min(10, matches + spawnFailures) : 10;
            return 1f - (n / 10f);
        }).orElse(0f);
    }

    public Entity getCachedEntity(SpawnerBlockEntity spawner) {
        if (this.cachedEntity == null) {
            this.cachedEntity = EntityType.loadEntityRecursive(((BaseSpawnerAccess)spawner.getSpawner()).getNextSpawnData().getEntityToSpawn(), this.nonNullLevel(), Function.identity());
        }
        return this.cachedEntity;
    }

    public float getRotationDegrees() {
        return rotationDegrees;
    }

    public float getPrevRotationDegrees() {
        return prevRotationDegrees;
    }

    public float getProgress() {
        return progress;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putFloat("progress", progress);
        tag.putByte("mode", (byte) mode.ordinal());
        tag.putFloat("targetSpeed", targetSpeed);
        tag.putInt("spawnFailures", spawnFailures);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        progress = tag.getFloat("progress");
        mode = Mode.values()[tag.getByte("mode")];
        targetSpeed = tag.getFloat("targetSpeed");
        spawnFailures = tag.getInt("spawnFailures");
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis().isHorizontal();
    }

    @Override
    public float getMinWorkingPressure() {
        return mode == Mode.RUNNING ? -0.5f : 0f;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new SpawnerExtractorMenu(windowId, inv, worldPosition);
    }

    public void updateMode() {
        BlockState below = nonNullLevel().getBlockState(worldPosition.below());
        if (below.getBlock() instanceof SpawnerBlock) {
            mode = Mode.RUNNING;
        } else {
            mode = Mode.FINISHED;
        }
    }
}
