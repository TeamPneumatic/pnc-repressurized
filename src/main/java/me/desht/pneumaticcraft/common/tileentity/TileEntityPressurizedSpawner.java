package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.RangeLines;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPressurizedSpawner;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketRenderRangeLines;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPressurizedSpawner extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControlled, INamedContainerProvider, IRangeLineShower {
    public static final int BASE_SPAWN_INTERVAL = 200;

    private final ItemSpawnerCore.SpawnerCoreItemHandler inventory = new ItemSpawnerCore.SpawnerCoreItemHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
    @DescSynced
    public TileEntityVacuumTrap.Problems problem = TileEntityVacuumTrap.Problems.OK;
    @GuiSynced
    private int redstoneMode;
    private int counter = BASE_SPAWN_INTERVAL;
    @DescSynced
    private boolean running;
    public final RangeLines rangeLines = new RangeLines(0x805A3C92);
    private int prevRange;

    public TileEntityPressurizedSpawner() {
        super(ModTileEntities.PRESSURIZED_SPAWNER.get(), PneumaticValues.DANGER_PRESSURE_TIER_TWO, PneumaticValues.MAX_PRESSURE_TIER_TWO, PneumaticValues.VOLUME_PRESSURIZED_SPAWNER, 4);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            ItemSpawnerCore.SpawnerCoreStats stats = inventory.getStats();
            running = false;
            problem = TileEntityVacuumTrap.Problems.OK;
            if (stats == null) {
                problem = TileEntityVacuumTrap.Problems.NO_CORE;
            } else if (getPressure() > getMinWorkingPressure() && redstoneAllows()) {
                running = true;
                if (--counter <= 0) {
                    if (!trySpawnSomething(stats)) {
                        ((ServerWorld) world).spawnParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 5, 0, 0, 0, 0);
                    }
                    addAir((int) (-PneumaticValues.USAGE_PRESSURIZED_SPAWNER * getSpeedUsageMultiplierFromUpgrades()));
                    counter = getSpawnInterval();
                }
            }
        } else {
            if (running) {
                double x = (double)pos.getX() + world.rand.nextDouble();
                double y = (double)pos.getY() + world.rand.nextDouble();
                double z = (double)pos.getZ() + world.rand.nextDouble();
                world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
                world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
            }
            int range = getRange();
            if (prevRange != range || prevRange == 0) {
                prevRange = range;
                if (!firstRun) rangeLines.startRendering(range);
            }
            rangeLines.tick(world.rand);
        }
    }

    private boolean trySpawnSomething(ItemSpawnerCore.SpawnerCoreStats stats) {
        EntityType<?> type = stats.pickEntity(true);
        if (type != null && world instanceof ServerWorld) {
            ServerWorld serverworld = (ServerWorld)world;
            int spawnRange = getRange();
            int maxNearbyEntities = 16;
            double x = (double)pos.getX() + (serverworld.rand.nextDouble() - world.rand.nextDouble()) * (double)spawnRange + 0.5D;
            double y = pos.getY() + serverworld.rand.nextInt(3) - 1;
            double z = (double)pos.getZ() + (serverworld.rand.nextDouble() - world.rand.nextDouble()) * (double)spawnRange + 0.5D;
            if (serverworld.hasNoCollisions(type.getBoundingBoxWithSizeApplied(x, y, z))) {
                Entity entity = type.create(serverworld);
                if (entity == null) return false;
                int entityCount = serverworld.getEntitiesWithinAABB(MobEntity.class, (new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).grow(spawnRange)).size();
                if (entityCount >= maxNearbyEntities) return false;
                entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
                if (entity instanceof MobEntity) {
                    MobEntity mobentity = (MobEntity) entity;
                    if (!ForgeEventFactory.doSpecialSpawn(mobentity, world, (float)entity.getPosX(), (float)entity.getPosY(), (float)entity.getPosZ(), null, SpawnReason.SPAWNER)) {
                        mobentity.onInitialSpawn(serverworld, world.getDifficultyForLocation(entity.getPosition()), SpawnReason.SPAWNER, null, null);
                    }
                }
                if (!serverworld.func_242106_g(entity)) return false;
                world.playEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, pos, 0);
                if (entity instanceof MobEntity) {
                    ((MobEntity)entity).spawnExplosionParticle();
                }
                return true;
            }
        }
        return false;
    }

    public int getSpawnInterval() {
        return (int)(BASE_SPAWN_INTERVAL / getSpeedMultiplierFromUpgrades());
    }

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
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerPressurizedSpawner(windowId, inv, getPos());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putByte(NBTKeys.NBT_REDSTONE_MODE, (byte) redstoneMode);
        tag.put("Inventory", inventory.serializeNBT());

        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        redstoneMode = tag.getByte(NBTKeys.NBT_REDSTONE_MODE);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public void showRangeLines() {
        if (getWorld().isRemote) {
            rangeLines.startRendering(getRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), getWorld(), TileEntityConstants.PACKET_UPDATE_DISTANCE + getRange());
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return rangeLines == null || !rangeLines.shouldRender() ? super.getRenderBoundingBox() : new AxisAlignedBB(getPos()).grow(getRange());
    }

    private int getRange() {
        return 2 + getUpgrades(EnumUpgrade.RANGE);
    }
}
