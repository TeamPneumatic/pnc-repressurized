package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerSpawnerExtractor;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore.SpawnerCoreStats;
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


        if (!world.isRemote) {
            int defenderChance = world.getDifficulty() == Difficulty.EASY ? 40 : 20;
            if (mode == Mode.RUNNING) {
                addAir(PneumaticValues.USAGE_SPAWNER_EXTRACTOR);
                if (progress >= 1f) {
                    extractSpawnerCore();
                } else if (currentSpeed > 0.1f && world.rand.nextInt(defenderChance) == 0) {
                    // spawn defending entities; each entity nearby will slow down the extraction process
                    PneumaticCraftUtils.getTileEntityAt(world, pos.down(), MobSpawnerTileEntity.class).ifPresent(te -> {
                        if (!trySpawnDefender(te)) {
                            spawnFailures++;
                        }
                    });
                }
            }
            if ((world.getGameTime() & 0xf) == 3) {
                targetSpeed = getTargetSpeed();
            }
            if ((world.getGameTime() & 0x3f) == 3) {
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

        AbstractSpawner spawner = te.getSpawnerBaseLogic();

        int spawnRange = 4;
        int maxNearbyEntities = 16;

        CompoundNBT nbt = spawner.spawnData.getNbt();
        Optional<EntityType<?>> optional = EntityType.readEntityType(nbt);
        if (!optional.isPresent()) {
            return false;
        }
        BlockPos pos = te.getPos();
        ListNBT listnbt = nbt.getList("Pos", Constants.NBT.TAG_DOUBLE);
        int size = listnbt.size();
        double x = size >= 1 ? listnbt.getDouble(0) : (double)pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)spawnRange + 0.5D;
        double y = size >= 2 ? listnbt.getDouble(1) : (double)(pos.getY() + world.rand.nextInt(3) - 1);
        double z = size >= 3 ? listnbt.getDouble(2) : (double)pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)spawnRange + 0.5D;
        if (world.hasNoCollisions(optional.get().getBoundingBoxWithSizeApplied(x, y, z))) {
            ServerWorld serverworld = (ServerWorld)world;
            Entity entity = EntityType.loadEntityAndExecute(nbt, world, (e1) -> {
                e1.setLocationAndAngles(x, y, z, e1.rotationYaw, e1.rotationPitch);
                return e1;
            });
            if (entity == null) {
                return false;
            }

            int entityCount = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).grow(spawnRange)).size();
            if (entityCount >= maxNearbyEntities) {
                return false;
            }

            entity.setLocationAndAngles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), world.rand.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof MobEntity) {
                MobEntity mobentity = (MobEntity)entity;
                if (spawner.spawnData.getNbt().size() == 1 && spawner.spawnData.getNbt().contains("id", Constants.NBT.TAG_STRING)) {
                    if (!ForgeEventFactory.doSpecialSpawn(mobentity, world, (float)entity.getPosX(), (float)entity.getPosY(), (float)entity.getPosZ(), spawner, SpawnReason.SPAWNER)) {
                        mobentity.onInitialSpawn(serverworld, world.getDifficultyForLocation(entity.getPosition()), SpawnReason.SPAWNER, null, null);
                        // note: "pneumaticcraft:defender" tag is added in TileEntityVacuumTrap.Listener.onMobSpawn()
                        if (world.getDifficulty() == Difficulty.HARD) {
                            getRandomEffects(world.rand).forEach(effect -> mobentity.addPotionEffect(new EffectInstance(effect, Integer.MAX_VALUE, 2)));
                        }
                    }
                }
            }

            if (!serverworld.func_242106_g(entity)) {
                return false;
            }

            world.playEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, pos, 0);
            if (entity instanceof MobEntity) {
                ((MobEntity)entity).spawnExplosionParticle();
            }
        }
        return true;
    }

    private List<Effect> getRandomEffects(Random rand) {
        List<Effect> l = new ArrayList<>();
        int n = rand.nextInt(100);
        if (n > 50) l.add(Effects.FIRE_RESISTANCE);
        if (n > 75) l.add(Effects.SPEED);
        if (n > 82) l.add(Effects.STRENGTH);
        if (n > 92) l.add(Effects.REGENERATION);
        if (n > 96) l.add(Effects.INVISIBILITY);
        return l;
    }

    private void extractSpawnerCore() {
        PneumaticCraftUtils.getTileEntityAt(world, pos.down(), MobSpawnerTileEntity.class).ifPresent(te -> {
            ItemStack spawnerCore = new ItemStack(ModItems.SPAWNER_CORE.get());
            SpawnerCoreStats stats = SpawnerCoreStats.forItemStack(spawnerCore);
            Entity e = getCachedEntity(te);
            if (e != null && stats != null) {
                stats.addAmount(e.getType(), 100);
                stats.serialize(spawnerCore);
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                item.setItem(spawnerCore);
                world.addEntity(item);
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 0.5f);
                world.setBlockState(pos.down(), ModBlocks.EMPTY_SPAWNER.get().getDefaultState(), Constants.BlockFlags.DEFAULT);
                world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(Blocks.SPAWNER.getDefaultState()));
            }
        });
    }

    private float getTargetSpeed() {
        if (getPressure() > getMinWorkingPressure()) return 0f;

        return PneumaticCraftUtils.getTileEntityAt(world, pos.down(), MobSpawnerTileEntity.class).map(te -> {
            int players = 0;
            int matches = 0;
            Entity e0 = getCachedEntity(te);
            if (e0 == null) return 0f;
            List<LivingEntity> l = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(pos).grow(MAX_ENTITY_RANGE), e -> true);
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
            this.cachedEntity = EntityType.loadEntityAndExecute(te.getSpawnerBaseLogic().spawnData.getNbt(), this.getWorld(), Function.identity());
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
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putFloat("progress", progress);
        tag.putByte("mode", (byte) mode.ordinal());
        tag.putFloat("targetSpeed", targetSpeed);
        tag.putInt("spawnFailures", spawnFailures);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

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
        return new ContainerSpawnerExtractor(windowId, inv, pos);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }

    public void updateMode() {
        BlockState below = world.getBlockState(pos.down());
        if (below.getBlock() instanceof SpawnerBlock) {
            mode = Mode.RUNNING;
        } else {
            mode = Mode.FINISHED;
        }
    }
}
