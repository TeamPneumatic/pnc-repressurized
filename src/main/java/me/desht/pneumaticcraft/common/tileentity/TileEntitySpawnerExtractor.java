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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerSpawnerExtractor;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class TileEntitySpawnerExtractor extends TileEntityPneumaticBase implements IMinWorkingPressure, INamedContainerProvider {

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

    public TileEntitySpawnerExtractor() {
        super(ModTileEntities.SPAWNER_EXTRACTOR.get(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.VOLUME_SPAWNER_EXTRACTOR, 4);
    }

    @Override
    public void tick() {
        super.tick();

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
            case RUNNING:
                float incr = currentSpeed / 1200f;   // at max speed, 1200 ticks to completion
                progress = Math.min(1f, progress + incr);
                break;
            case FINISHED:
                progress = 1f;
                targetSpeed = 0f;
                rotationDegrees = prevRotationDegrees;
                break;
        }


        if (!level.isClientSide) {
            int defenderChance = level.getDifficulty() == Difficulty.EASY ? 40 : 20;
            if (mode == Mode.RUNNING) {
                addAir(PneumaticValues.USAGE_SPAWNER_EXTRACTOR);
                if (progress >= 1f) {
                    extractSpawnerCore();
                } else if (currentSpeed > 0.1f && level.random.nextInt(defenderChance) == 0) {
                    // spawn defending entities; each entity nearby will slow down the extraction process
                    PneumaticCraftUtils.getTileEntityAt(level, worldPosition.below(), MobSpawnerTileEntity.class).ifPresent(te -> {
                        if (!trySpawnDefender(te)) {
                            spawnFailures++;
                        }
                    });
                }
            }
            if ((level.getGameTime() & 0xf) == 3) {
                targetSpeed = getTargetSpeed();
            }
            if ((level.getGameTime() & 0x3f) == 3) {
                spawnFailures = Math.max(0, spawnFailures - 1);
            }
        } else {
            prevRotationDegrees = rotationDegrees;
            // at full speed, 1 rotation per second
            rotationDegrees += currentSpeed * 18f;
        }
    }

    private boolean trySpawnDefender(MobSpawnerTileEntity te) {
        // logic largely lifted from AbstractSpawner#tick()

        AbstractSpawner spawner = te.getSpawner();

        int spawnRange = 4;
        int maxNearbyEntities = 16;

        CompoundNBT nbt = spawner.nextSpawnData.getTag();
        Optional<EntityType<?>> optional = EntityType.by(nbt);
        if (!optional.isPresent()) {
            return false;
        }
        BlockPos pos = te.getBlockPos();
        ListNBT listnbt = nbt.getList("Pos", Constants.NBT.TAG_DOUBLE);
        int size = listnbt.size();
        double x = size >= 1 ? listnbt.getDouble(0) : (double)pos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
        double y = size >= 2 ? listnbt.getDouble(1) : (double)(pos.getY() + level.random.nextInt(3) - 1);
        double z = size >= 3 ? listnbt.getDouble(2) : (double)pos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * (double)spawnRange + 0.5D;
        if (level.noCollision(optional.get().getAABB(x, y, z))) {
            ServerWorld serverworld = (ServerWorld)level;
            Entity entity = EntityType.loadEntityRecursive(nbt, level, (e1) -> {
                e1.moveTo(x, y, z, e1.yRot, e1.xRot);
                return e1;
            });
            if (entity == null) {
                return false;
            }

            int entityCount = level.getEntitiesOfClass(entity.getClass(), (new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).inflate(spawnRange)).size();
            if (entityCount >= maxNearbyEntities) {
                return false;
            }

            entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof MobEntity) {
                MobEntity mobentity = (MobEntity)entity;
                if (spawner.nextSpawnData.getTag().size() == 1 && spawner.nextSpawnData.getTag().contains("id", Constants.NBT.TAG_STRING)) {
                    if (!ForgeEventFactory.doSpecialSpawn(mobentity, level, (float)entity.getX(), (float)entity.getY(), (float)entity.getZ(), spawner, SpawnReason.SPAWNER)) {
                        mobentity.finalizeSpawn(serverworld, level.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.SPAWNER, null, null);
                        // note: "pneumaticcraft:defender" tag is added in TileEntityVacuumTrap.Listener.onMobSpawn()
                        if (level.getDifficulty() == Difficulty.HARD) {
                            getRandomEffects(level.random).forEach(effect -> mobentity.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, 2)));
                        }
                    }
                }
            }

            if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                return false;
            }

            level.levelEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, pos, 0);
            if (entity instanceof MobEntity) {
                ((MobEntity)entity).spawnAnim();
            }
        }
        return true;
    }

    private List<Effect> getRandomEffects(Random rand) {
        List<Effect> l = new ArrayList<>();
        int n = rand.nextInt(100);
        if (n > 50) l.add(Effects.FIRE_RESISTANCE);
        if (n > 75) l.add(Effects.MOVEMENT_SPEED);
        if (n > 82) l.add(Effects.DAMAGE_BOOST);
        if (n > 92) l.add(Effects.REGENERATION);
        if (n > 96) l.add(Effects.INVISIBILITY);
        return l;
    }

    private void extractSpawnerCore() {
        PneumaticCraftUtils.getTileEntityAt(level, worldPosition.below(), MobSpawnerTileEntity.class).ifPresent(te -> {
            ItemStack spawnerCore = new ItemStack(ModItems.SPAWNER_CORE.get());
            ISpawnerCoreStats stats = PneumaticRegistry.getInstance().getItemRegistry().getSpawnerCoreStats(spawnerCore);
            Entity e = getCachedEntity(te);
            if (e != null && stats != null) {
                stats.addAmount(e.getType(), 100);
                stats.serialize(spawnerCore);
                ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5);
                item.setItem(spawnerCore);
                level.addFreshEntity(item);
                level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 0.5f);
                level.setBlock(worldPosition.below(), ModBlocks.EMPTY_SPAWNER.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
                level.levelEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, worldPosition, Block.getId(Blocks.SPAWNER.defaultBlockState()));
            }
        });
    }

    private float getTargetSpeed() {
        if (getPressure() > getMinWorkingPressure()) return 0f;

        return PneumaticCraftUtils.getTileEntityAt(level, worldPosition.below(), MobSpawnerTileEntity.class).map(te -> {
            int players = 0;
            int matches = 0;
            Entity e0 = getCachedEntity(te);
            if (e0 == null) return 0f;
            List<LivingEntity> l = level.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(worldPosition).inflate(MAX_ENTITY_RANGE), e -> true);
            for (LivingEntity e : l) {
                if (e instanceof PlayerEntity && !(e instanceof FakePlayer)) players++;
                if (e.getType() == e0.getType()) matches++;
            }
            int n = players > 0 ? Math.min(10, matches + spawnFailures) : 10;
            return 1f - (n / 10f);
        }).orElse(0f);
    }

    public Entity getCachedEntity(MobSpawnerTileEntity te) {
        if (this.cachedEntity == null) {
            this.cachedEntity = EntityType.loadEntityRecursive(te.getSpawner().nextSpawnData.getTag(), this.getLevel(), Function.identity());
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        tag.putFloat("progress", progress);
        tag.putByte("mode", (byte) mode.ordinal());
        tag.putFloat("targetSpeed", targetSpeed);
        tag.putInt("spawnFailures", spawnFailures);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        progress = tag.getFloat("progress");
        mode = Mode.values()[tag.getByte("mode")];
        targetSpeed = tag.getFloat("targetSpeed");
        spawnFailures = tag.getInt("spawnFailures");
    }

    @Override
    public IItemHandler getPrimaryInventory() {
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
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerSpawnerExtractor(windowId, inv, worldPosition);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1);
    }

    public void updateMode() {
        BlockState below = level.getBlockState(worldPosition.below());
        if (below.getBlock() instanceof SpawnerBlock) {
            mode = Mode.RUNNING;
        } else {
            mode = Mode.FINISHED;
        }
    }
}
