package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumTrap;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
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
    private final VacuumTrapItemHandler inv = new VacuumTrapItemHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inv);

    private ItemSpawnerCore.SpawnerCoreStats coreStats;
    private final List<LivingEntity> targetEntities = new ArrayList<>();

    @DescSynced
    private boolean isCoreLoaded;

    public TileEntityVacuumTrap() {
        super(ModTileEntities.VACUUM_TRAP.get(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.VOLUME_VACUUM_TRAP, 4);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            isCoreLoaded = coreStats != null;

            if (isOpen() && isCoreLoaded && coreStats.getUnused() > 0 && getPressure() < getMinWorkingPressure()) {
                if ((world.getGameTime() & 0xf) == 0) {
                    scanForEntities();
                }
                Vector3d trapVec = Vector3d.copyCentered(pos);
                for (LivingEntity e : targetEntities) {
                    if (!e.isAlive()) continue;
                    if (e.getDistanceSq(trapVec) < 2) {
                        absorbEntity(e);
                        addAir((int) (10 * e.getHealth()));
                    } else {
                        Vector3d vec = trapVec.subtract(e.getPositionVec()).normalize().scale(0.15);
                        e.move(MoverType.SELF, vec);
                    }
                }
            }
        } else {
            if (isOpen() && isCoreLoaded && world.rand.nextBoolean()) {
                ClientUtils.emitParticles(world, pos, ParticleTypes.PORTAL);
            }
        }
    }

    private void absorbEntity(LivingEntity e) {
        int amount = e instanceof MobEntity && ((MobEntity) e).getAttackTarget() instanceof PlayerEntity ? 2 + e.world.rand.nextInt(3) : 1;
        if (coreStats.addAmount(e.getType(), amount)) {
            e.remove();
            ItemSpawnerCore.SpawnerCoreStats.serialize(coreStats, inv.getStackInSlot(0));
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 1f, 2f);
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).spawnParticle(ParticleTypes.CLOUD, e.getPosX(), e.getPosY() + 0.5, e.getPosZ(), 5, 0, 1, 0, 0);
            }
        }
    }

    private void scanForEntities() {
        targetEntities.clear();

        AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(3 + getUpgrades(EnumUpgrade.RANGE));
        targetEntities.addAll(world.getEntitiesWithinAABB(LivingEntity.class, aabb, this::isApplicable));
    }

    private boolean isApplicable(LivingEntity e) {
        // TODO a more configurable filter is needed here
        return e.isNonBoss() && !(e instanceof PlayerEntity || e instanceof EntityDrone);
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
        readSpawnerCoreStats();
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

    private void readSpawnerCoreStats() {
        coreStats = inv.getStackInSlot(0).isEmpty() ? null : ItemSpawnerCore.SpawnerCoreStats.forItemStack(inv.getStackInSlot(0));
    }

    private class VacuumTrapItemHandler extends BaseItemStackHandler {
        public VacuumTrapItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 0) {
                readSpawnerCoreStats();
            }
        }
    }
}
