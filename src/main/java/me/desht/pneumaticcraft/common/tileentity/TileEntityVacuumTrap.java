package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumTrap;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore.SpawnerCoreItemHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityVacuumTrap extends TileEntityPneumaticBase implements IMinWorkingPressure, INamedContainerProvider {
    public enum Problems implements ITranslatableEnum {
        OK,
        NO_CORE,
        CORE_FULL,
        TRAP_CLOSED;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.problems.vacuum_trap." + this.toString().toLowerCase();
        }
    }

    private final SpawnerCoreItemHandler inv = new SpawnerCoreItemHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inv);

    private final List<MobEntity> targetEntities = new ArrayList<>();

    @DescSynced
    private boolean isCoreLoaded;
    @DescSynced
    public Problems problem = Problems.OK;

    public TileEntityVacuumTrap() {
        super(ModTileEntities.VACUUM_TRAP.get(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.VOLUME_VACUUM_TRAP, 4);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            isCoreLoaded = inv.getStats() != null;

            if (isOpen() && isCoreLoaded && inv.getStats().getUnused() > 0 && getPressure() <= getMinWorkingPressure()) {
                if ((world.getGameTime() & 0xf) == 0) {
                    scanForEntities();
                }
                Vector3d trapVec = Vector3d.copyCentered(pos);
                for (MobEntity e : targetEntities) {
                    if (!e.isAlive() || e.getTags().contains(TileEntitySpawnerExtractor.DEFENDER_TAG)) continue;
                    if (e.getDistanceSq(trapVec) < 2) {
                        absorbEntity(e);
                        addAir((int) (PneumaticValues.USAGE_VACUUM_TRAP * e.getHealth()));
                    } else {
                        e.getNavigator().tryMoveToXYZ(trapVec.getX(), trapVec.getY(), trapVec.getZ(), 1.0);
                    }
                }
            }
            if (!isCoreLoaded)
                problem = Problems.NO_CORE;
            else if (inv.getStats().getUnused() == 0)
                problem = Problems.CORE_FULL;
            else if (!isOpen())
                problem = Problems.TRAP_CLOSED;
            else
                problem = Problems.OK;
        } else {
            if (isOpen() && isCoreLoaded && world.rand.nextBoolean()) {
                ClientUtils.emitParticles(world, pos, ParticleTypes.PORTAL);
            }
        }
    }

    private void absorbEntity(MobEntity e) {
        int amount = e.getAttackTarget() instanceof PlayerEntity ? 2 + e.world.rand.nextInt(3) : 1;
        if (inv.getStats().addAmount(e.getType(), amount)) {
            e.remove();
            inv.getStats().serialize(inv.getStackInSlot(0));
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 1f, 2f);
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).spawnParticle(ParticleTypes.CLOUD, e.getPosX(), e.getPosY() + 0.5, e.getPosZ(), 5, 0, 1, 0, 0);
            }
        }
    }

    private void scanForEntities() {
        targetEntities.clear();

        AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(3 + getUpgrades(EnumUpgrade.RANGE));
        targetEntities.addAll(world.getEntitiesWithinAABB(MobEntity.class, aabb, this::isApplicable));
    }

    private boolean isApplicable(LivingEntity e) {
        return e.isNonBoss()
                && !(e instanceof EntityDrone)
                && !(e instanceof TameableEntity && ((TameableEntity) e).isTamed())
                && !PNCConfig.Common.General.vacuumTrapBlacklist.contains(e.getType().getRegistryName());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inv;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == Direction.DOWN || side.getAxis() == getRotation().getAxis();
    }

    @Override
    public float getMinWorkingPressure() {
        return -0.5f;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerVacuumTrap(windowId, inv, getPos());
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        inv.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", inv.serializeNBT());
        return tag;
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        if (!isOpen()) {
            // if closed, spawner core stays inside the trap when broken
            for (int i = 0; i < drops.size(); i++) {
                if (drops.get(i).getItem() instanceof ItemSpawnerCore) {
                    drops.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        if (!isOpen()) {
            // if closed, spawner core stays inside the trap when broken
            blockEntityTag.put("Items", inv.serializeNBT());
        }
    }

    public boolean isOpen() {
        return getBlockState().getBlock() == ModBlocks.VACUUM_TRAP.get() && getBlockState().get(BlockStateProperties.OPEN);
    }
}
